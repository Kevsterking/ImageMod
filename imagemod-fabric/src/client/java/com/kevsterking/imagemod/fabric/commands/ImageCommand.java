package com.kevsterking.imagemod.fabric.commands;

import com.kevsterking.imagemod.fabric.ImageBuilder.ImageBlock;
import com.kevsterking.imagemod.fabric.ImageBuilder.Mosaic.MosaicIntColThread;
import com.kevsterking.imagemod.fabric.ImageMod;
import com.kevsterking.imagemod.fabric.ImageModClient;
import com.kevsterking.imagemod.fabric.WorldTransformer.WorldTransform;
import com.kevsterking.imagemod.fabric.util.DirectoryArgument;
import com.kevsterking.imagemod.fabric.util.ImageFileArgument;
import com.kevsterking.imagemod.fabric.util.PathArgument;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.world.World;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class ImageCommand {

  private static final PathArgument src_arg = new ImageFileArgument();
  private static final PathArgument dir_arg = new DirectoryArgument();
  private static final MosaicIntColThread image_builder = new MosaicIntColThread();

  private static final ArrayList<ImageBlock> image_blocks = new ArrayList<>();
  private static final Stack<WorldTransform> undo_stack = new Stack<>();
  private static final Stack<WorldTransform> redo_stack = new Stack<>();

  private static final Set<Block> block_blacklist = new HashSet<>();

  static {
    block_blacklist.add(Blocks.CARTOGRAPHY_TABLE);
    block_blacklist.add(Blocks.DRIED_KELP_BLOCK);
    block_blacklist.add(Blocks.SCULK_SHRIEKER);
    block_blacklist.add(Blocks.ICE);
    block_blacklist.add(Blocks.BLUE_ICE);
    block_blacklist.add(Blocks.FROSTED_ICE);
    block_blacklist.add(Blocks.PACKED_ICE);
  }

  // Filter out unwanted blocks based on properties that
  // create unwanted effects
  // true -> keep block, false -> don't use block
  private static boolean filter_block(Block block) throws Exception {
    BlockState state = block.getDefaultState();
    VoxelShape vs = state.getCollisionShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
    if (!Block.isShapeFullCube(vs)) throw new Exception("Is not full block");
    if (block.hasDynamicBounds()) throw new Exception("Has dynamic shape");
    if (state.getLuminance() != 0) throw new Exception("Emits light");
    if (state.contains(Properties.FACING) || state.contains(Properties.HORIZONTAL_FACING)) throw new Exception("Directional block");
    if (block instanceof CoralBlock) throw new Exception("Is coral block");
    if (block instanceof LeavesBlock) throw new Exception("Is leaves block");
    if (block_blacklist.contains(block)) throw new Exception("Is blacklisted");
    return true;
  }

  // Update our locally stored block list
  // Filter out bad block types and assign
  public static void update_block_list() {
    ImageModClient.LOGGER.info("Loading block list");
    for (Block block : Registries.BLOCK.stream().toList()) {
      try {
        if (filter_block(block)) {
          image_blocks.add(ImageBlock.get(block));
          ImageModClient.LOGGER.debug("{} - ACCEPTED", block.getName().getString());
        }
      } catch (Exception e) {
        ImageModClient.LOGGER.debug("{} - REJECTED: {}", block.getName().getString(), e.getMessage());
      }
    }
    ImageModClient.LOGGER.info("Block list loading complete.");
    ImageBlock[] blocks = new ImageBlock[image_blocks.size()];
    for (int i = 0; i < image_blocks.size(); i++) {
      blocks[i] = image_blocks.get(i);
    }
    image_builder.set_tiles(blocks, 16);
  }

  private static int create_execute(
    CommandContext<FabricClientCommandSource> ctx
  ) throws CommandSyntaxException {
    // Load arguments
    int input_width = 0, input_height = 0;
    boolean width_set = false, height_set = false;
    try {
      input_width = IntegerArgumentType.getInteger(ctx, "width");
      width_set = true;
    } catch (Exception ignored) {}
    try {
      input_height = IntegerArgumentType.getInteger(ctx, "height");
      height_set = true;
    } catch (Exception ignored){}
    // Load image
    Path path = PathArgument.get_path(ctx, "src");
    BufferedImage image = null;
    try {
      image = ImageIO.read(path.toFile());
    } catch (IOException e) {
      throw new SimpleCommandExceptionType(Text.literal("Could not load image")).create();
    }
    if (image == null) {
      throw new SimpleCommandExceptionType(Text.literal("Could not load image")).create();
    }
    int w, h;
    if (width_set && height_set) {
      w = input_width;
      h = input_height;
    } else if (width_set) {
      w = input_width;
      h = (int) (input_width * ((double)image.getHeight() / image.getWidth()));
    } else if (height_set) {
      h = input_height;
      w = (int) (input_height * ((double)image.getWidth() / image.getHeight()));
    } else {
      throw new SimpleCommandExceptionType(Text.literal("No width or height set")).create();
    }
    // World in which the command was sent
    FabricClientCommandSource source = ctx.getSource();
    Entity entity = source.getEntity();
    if (entity == null) {
      throw new SimpleCommandExceptionType(Text.literal("Source is not an entity")).create();
    }
    ClientWorld level = source.getWorld();
    // Get relative directions and positions
    // to entity for placing image blocks at the
    // right place
    Direction direction_view = entity.getFacing();
    Direction direction_right = direction_view.rotateYClockwise();
    BlockPos position = entity.getBlockPos().offset(direction_view, 2);
    // Create image
    try {
      ImageCommand.undo_stack.push(
        WorldTransform.place(
          image_builder.generate(image, w, h),
          level,
          position,
          direction_right,
          Direction.UP,
          direction_view
        )
      );
    } catch (Exception e) {
      throw new SimpleCommandExceptionType(Text.literal("Failed: " + e.getMessage())).create();
    }
    return 1;
  }

  // Reload block list
  private static int reload_execute(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
    ImageCommand.update_block_list();
    ctx.getSource().sendFeedback(Text.literal("Reloaded block list"));
    return 1;
  }

  // Undo an image create
  private static int undo_execute(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
    if (ImageCommand.undo_stack.empty()) {
      ctx.getSource().sendError(Text.literal("Undo stack is empty"));
      return -1;
    }
    WorldTransform action = ImageCommand.undo_stack.pop();;
    action.revert_transform();
    ImageCommand.redo_stack.push(action);
    ctx.getSource().sendFeedback(Text.literal("Successfully reverted last image creation"));
    return 1;
  }

  // Redo an undone image create
  private static int redo_execute(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
    if (ImageCommand.redo_stack.empty()) {
      ctx.getSource().sendError(Text.literal("Redo stack is empty"));
      return -1;
    }
    WorldTransform action = ImageCommand.redo_stack.pop();
    action.perform_transform();
    ImageCommand.undo_stack.push(action);
    ctx.getSource().sendFeedback(Text.literal("Successfully recreated image"));
    return 1;
  }

  // Set the directory where the image create command looks for image files
  private static int set_directory_execute(
          CommandContext<FabricClientCommandSource> ctx
  ) {
    Path dir = DirectoryArgument.get_path(ctx, "dir");
    try {
      PathArgument.set_root_directory(String.valueOf(dir));
      ctx.getSource().sendFeedback(Text.literal("Successfully set image directory to \"" + dir.toString() + "\""));
      return 1;
    } catch (NotDirectoryException e) {
      ctx.getSource().sendError(Text.literal("Provided path is not a directory"));
    } catch (FileNotFoundException e) {
      ctx.getSource().sendError(Text.literal("Provided path could not be found"));
    }
    return -1;
  }

  // Register command structure
  public static void register(
    CommandDispatcher<FabricClientCommandSource> ctx,
    CommandRegistryAccess access
  ) {

    ImageModClient.LOGGER.info("Registering Commands...");

    // update block list when we are registering command
    ImageCommand.update_block_list();

    // image command
    LiteralArgumentBuilder<FabricClientCommandSource> root = ClientCommandManager.literal("image");

    // create option
    LiteralArgumentBuilder<FabricClientCommandSource> createLiteral = ClientCommandManager.literal("create");

    LiteralArgumentBuilder<FabricClientCommandSource> wLiteral  	= ClientCommandManager.literal("-width");
    LiteralArgumentBuilder<FabricClientCommandSource> hLiteral  	= ClientCommandManager.literal("-height");
    LiteralArgumentBuilder<FabricClientCommandSource> whInfoLiteral = ClientCommandManager.literal("~ ~");

    RequiredArgumentBuilder<FabricClientCommandSource, Path> srcArgument = ClientCommandManager.argument("src", src_arg);

    RequiredArgumentBuilder<FabricClientCommandSource, Integer> wArgument      = ClientCommandManager.argument("width", IntegerArgumentType.integer());
    RequiredArgumentBuilder<FabricClientCommandSource, Integer> wArgumentFinal = ClientCommandManager.argument("width", IntegerArgumentType.integer()).executes(ImageCommand::create_execute);
    RequiredArgumentBuilder<FabricClientCommandSource, Integer> hArgumentFinal = ClientCommandManager.argument("height", IntegerArgumentType.integer()).executes(ImageCommand::create_execute);

    wLiteral.then(wArgumentFinal);
    hLiteral.then(hArgumentFinal);
    wArgument.then(hArgumentFinal);

    srcArgument.then(wLiteral).then(hLiteral).then(whInfoLiteral).then(wArgument);
    createLiteral.then(srcArgument);

    // Reload option
    LiteralArgumentBuilder<FabricClientCommandSource> reload = ClientCommandManager.literal("reload").executes(ImageCommand::reload_execute);

    // Undo
    LiteralArgumentBuilder<FabricClientCommandSource> undoLiteral = ClientCommandManager.literal("undo").executes(ImageCommand::undo_execute);

    // Redo
    LiteralArgumentBuilder<FabricClientCommandSource> redoLiteral = ClientCommandManager.literal("redo").executes(ImageCommand::redo_execute);;

    // SetDirectory
    LiteralArgumentBuilder<FabricClientCommandSource> setDirectoryLiteral = ClientCommandManager.literal("setDirectory");
    RequiredArgumentBuilder<FabricClientCommandSource, Path> directoryArgument  = ClientCommandManager.argument("dir", dir_arg).executes(ImageCommand::set_directory_execute);

    setDirectoryLiteral.then(directoryArgument);

    // Root relations
    root.then(createLiteral).then(setDirectoryLiteral).then(reload).then(undoLiteral).then(redoLiteral);
    ctx.register(root);

    ImageModClient.LOGGER.info("Commands Registered.");

  }


}
