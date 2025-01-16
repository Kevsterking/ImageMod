package com.kevsterking.imagemod.neoforge.commands;

import com.kevsterking.imagemod.neoforge.ImageBuilder.*;
import com.kevsterking.imagemod.neoforge.ImagemodClient;
import com.kevsterking.imagemod.neoforge.WorldTransformer.WorldTransform;
import com.kevsterking.imagemod.neoforge.util.DirectoryArgument;
import com.kevsterking.imagemod.neoforge.util.ImageFileArgument;
import com.kevsterking.imagemod.neoforge.util.PathArgument;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CoralBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.data.ModelData;

public class ImageCommand {

  private static final PathArgument src_arg = new ImageFileArgument();
  private static final PathArgument dir_arg = new DirectoryArgument();

  private static final ArrayList<BlockColor> image_blocks = new ArrayList<>();
  private static final Stack<WorldTransform> undo_stack = new Stack<>();
  private static final Stack<WorldTransform> redo_stack = new Stack<>();

  // Filter out unwanted blocks based on properties that
  // create unwanted effects
  // true -> keep block, false -> don't use block
  private static boolean filter_block(Block block) {
    BlockState state = block.defaultBlockState();
    VoxelShape vs = state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
    return (Block.isShapeFullBlock(vs) &&
      !block.hasDynamicShape() &&
      state.getLightEmission(EmptyBlockGetter.INSTANCE, BlockPos.ZERO) == 0 &&
      !(block instanceof CoralBlock) &&
      !(block instanceof LeavesBlock));
  }

  private static ResourceLocation get_resource_location(Block block) {
    // Black magic, don't question it
    BlockState state = block.defaultBlockState();
    ModelResourceLocation mrl = BlockModelShaper.stateToModelLocation(state);
    TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getModel(mrl).getParticleIcon(ModelData.EMPTY);
    ResourceLocation block_id = sprite.atlasLocation();
    String path = sprite.contents().name().getPath();
    return ResourceLocation.fromNamespaceAndPath(block_id.getNamespace(), "textures/" + path + ".png");
  }

  // Load textured resource for block into ImageBlock
  private static BlockColor get_block_color(Block block, Direction dir) throws Exception {
    BlockState state = block.defaultBlockState();
    ResourceManager resource_manager = Minecraft.getInstance().getResourceManager();
    ResourceLocation texture_id = get_resource_location(block);
    if (resource_manager.getResource(texture_id).isEmpty()) {
      throw new Exception("No resource available for block");
    }
    BufferedImage block_texture = ImageIO.read(resource_manager.getResource(texture_id).get().open());
    return new BlockColor(state, ImageUtil.load_color(block_texture));
  }

  // Update our locally stored block list
  // Filter out bad block types and assign
  public static void update_block_list() {
    ImagemodClient.LOGGER.info("Loading block color list");
    image_blocks.clear();
    for (Block block : BuiltInRegistries.BLOCK.stream().toList()) {
      if (filter_block(block)) {
        try {
          BlockColor block_color = get_block_color(block, Direction.NORTH);
          image_blocks.add(block_color);
          ImagemodClient.LOGGER.debug(
            "{} - SUCCESS! (r:{} g:{} b:{} a:{})",
            block.getName().getString(),
            block_color.color.red,
            block_color.color.green,
            block_color.color.blue,
            block_color.color.alpha
          );
        } catch (Exception e) {
          ImagemodClient.LOGGER.debug("{} - rejected: {}", block.getName().getString(), e.getMessage());
        }
      } else {
        ImagemodClient.LOGGER.debug("{} - rejected: filter-block", block.getName().getString());
      }
    }
    ImagemodClient.LOGGER.info("Block color list loading complete");
  }

  private static int create_execute(
    CommandContext<CommandSourceStack> ctx
  ) throws CommandSyntaxException {
    // Load arguments
    int input_width = 0, input_height = 0;
    boolean width_set = false, height_set = false;
    try {
      input_width = IntegerArgumentType.getInteger(ctx, "width");
      width_set = true;
    } catch (Exception e) {}
    try {
      input_height = IntegerArgumentType.getInteger(ctx, "height");
      height_set = true;
    } catch (Exception e) {}
    // Load image
    Path path = PathArgument.get_path(ctx, "src");
    Image image = null;
    BufferedImage buffered_image;
    try {
      buffered_image = ImageIO.read(path.toFile());
      if (width_set && height_set) {
        image = ImageUtil.load(buffered_image, input_width, input_height);
      } else if (width_set) {
        image = ImageUtil.load_width(buffered_image, input_width);
      } else if (height_set) {
        image = ImageUtil.load_height(buffered_image, input_height);
      } else {
        ImagemodClient.LOGGER.error("EROOOOOOORORRR: No width or height set");
      }
    } catch (IOException e) {
      ImagemodClient.LOGGER.error("EROOOOOOORORRR: IOException {}", e.getMessage());
    }
    if (image == null) return -1;
    // World in which the command was sent
    CommandSourceStack source = ctx.getSource();
    Entity entity = source.getEntity();
    if (entity == null) return -1;
    Level level = source.getUnsidedLevel();
    // Get relative directions and positions
    // to entity for placing image blocks at the
    // right place
    Direction direction_view = entity.getDirection();
    Direction direction_right = direction_view.getClockWise();
    BlockPos position = entity.blockPosition().relative(direction_view, 2);
    final int w = image.width, h = image.height;
    // Create image
    new BlockImageBuilder(
      image,
      ImageCommand.image_blocks,
      level,
      position,
      direction_right,
      Direction.UP,
      (e) -> {
        source.sendFailure(Component.literal(e.getMessage()));
      },
      (transform) -> {
        source.sendSuccess(() -> Component.literal(String.format("Successfully created (%dx%d) image", w, h)), false);
        transform.perform_action();
        ImageCommand.undo_stack.push(transform);
      }
    ).run();

    return 1;

  }

  // Reload block list
  private static int reload_execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
    ImageCommand.update_block_list();
    ctx.getSource().sendSuccess(() -> Component.literal("Reloaded block list"), false);
    return 1;
  }

  // Undo an image create
  private static int undo_execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
    if (ImageCommand.undo_stack.empty()) {
      ctx.getSource().sendFailure(Component.literal("Undo stack is empty"));
      return -1;
    }
    WorldTransform action = ImageCommand.undo_stack.pop();;
    action.revert_action();
    ImageCommand.redo_stack.push(action);
    ctx.getSource().sendSuccess(() -> Component.literal("Successfully reverted last image creation"), false);
    return 1;
  }

  // Redo an undone image create
  private static int redo_execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
    if (ImageCommand.redo_stack.empty()) {
      ctx.getSource().sendFailure(Component.literal("Redo stack is empty"));
      return -1;
    }
    WorldTransform action = ImageCommand.redo_stack.pop();
    action.perform_action();
    ImageCommand.undo_stack.push(action);
    ctx.getSource().sendSuccess(() -> Component.literal("Successfully recreated image"), false);
    return 1;
  }

  // Set the directory where the image create command looks for image files
  private static int set_directory_execute(
          CommandContext<CommandSourceStack> ctx
  ) {
    Path dir = DirectoryArgument.get_path(ctx, "dir");
    try {
      PathArgument.set_root_directory(String.valueOf(dir));
      ctx.getSource().sendSuccess(() -> Component.literal("Successfully set image directory to \"" + dir.toString() + "\""), false);
      return 1;
    } catch (NotDirectoryException e) {
      ctx.getSource().sendFailure(Component.literal("Provided path is not a directory"));
    } catch (FileNotFoundException e) {
      ctx.getSource().sendFailure(Component.literal("Provided path could not be found"));
    }
    return -1;
  }

  // Register command structure
  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

    ImagemodClient.LOGGER.info("Registering Commands");

    // update block list when we are registering command
    ImageCommand.update_block_list();

    // image command
    LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("image");

    // create option
    LiteralArgumentBuilder<CommandSourceStack> createLiteral = Commands.literal("create");

    LiteralArgumentBuilder<CommandSourceStack> wLiteral  	= Commands.literal("-width");
    LiteralArgumentBuilder<CommandSourceStack> hLiteral  	= Commands.literal("-height");
    LiteralArgumentBuilder<CommandSourceStack> whInfoLiteral = Commands.literal("~ ~");

    RequiredArgumentBuilder<CommandSourceStack, Path> srcArgument = Commands.argument("src", src_arg);

    RequiredArgumentBuilder<CommandSourceStack, Integer> wArgument      = Commands.argument("width", IntegerArgumentType.integer());
    RequiredArgumentBuilder<CommandSourceStack, Integer> wArgumentFinal = Commands.argument("width", IntegerArgumentType.integer()).executes(ImageCommand::create_execute);
    RequiredArgumentBuilder<CommandSourceStack, Integer> hArgumentFinal = Commands.argument("height", IntegerArgumentType.integer()).executes(ImageCommand::create_execute);

    wLiteral.then(wArgumentFinal);
    hLiteral.then(hArgumentFinal);
    wArgument.then(hArgumentFinal);

    srcArgument.then(wLiteral).then(hLiteral).then(whInfoLiteral).then(wArgument);
    createLiteral.then(srcArgument);

    // Reload option
    LiteralArgumentBuilder<CommandSourceStack> reload = Commands.literal("reload").executes(ImageCommand::reload_execute);

    // Undo
    LiteralArgumentBuilder<CommandSourceStack> undoLiteral = Commands.literal("undo").executes(ImageCommand::undo_execute);

    // Redo
    LiteralArgumentBuilder<CommandSourceStack> redoLiteral = Commands.literal("redo").executes(ImageCommand::redo_execute);;

    // SetDirectory
    LiteralArgumentBuilder<CommandSourceStack> setDirectoryLiteral = Commands.literal("setDirectory");
    RequiredArgumentBuilder<CommandSourceStack, Path> directoryArgument  = Commands.argument("dir", dir_arg).executes(ImageCommand::set_directory_execute);

    setDirectoryLiteral.then(directoryArgument);

    // Root relations
    root.then(createLiteral).then(setDirectoryLiteral).then(reload).then(undoLiteral).then(redoLiteral);
    dispatcher.register(root);

    ImagemodClient.LOGGER.info("Commands Registered");

  }

}
