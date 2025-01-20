package com.kevsterking.imagemod.fabric.commands;

import com.kevsterking.imagemod.fabric.ImageBuilder.ImageBlock;
import com.kevsterking.imagemod.fabric.ImageBuilder.Mosaic.MosaicIntColThread;
import com.kevsterking.imagemod.fabric.ImageModClient;
import com.kevsterking.imagemod.fabric.WorldTransformer.WorldStructure;
import com.kevsterking.imagemod.fabric.WorldTransformer.WorldTransformer;
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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.ArrayList;

public class ImageCommand {

  private final PathArgument image_argument = new ImageFileArgument();
  private final PathArgument directory_argument = new DirectoryArgument();

  private final ArrayList<ImageBlock> image_blocks = new ArrayList<>();
  private final MosaicIntColThread image_builder = new MosaicIntColThread();
  private WorldTransformer world_transformer;

  // Update block list
  public void update_block_list() {
    ImageModClient.LOGGER.info("Loading block list");
    for (Block block : Registries.BLOCK.stream().toList()) {
      try {
        if (ImageBlock.filter_block(block)) {
          this.image_blocks.add(ImageBlock.get(block));
          ImageModClient.LOGGER.debug("{} - ACCEPTED", block.getName().getString());
        }
      } catch (Exception e) {
        ImageModClient.LOGGER.debug("{} - REJECTED: {}", block.getName().getString(), e.getMessage());
      }
    }
    this.image_blocks.add(ImageBlock.get_air());
    ImageBlock[] blocks = new ImageBlock[this.image_blocks.size()];
    for (int i = 0; i < this.image_blocks.size(); i++) {
      blocks[i] = this.image_blocks.get(i);
    }
    this.image_builder.set_tiles(blocks, 16);
    ImageModClient.LOGGER.info("Block list loading complete.");
  }

  private int create_execute(
    CommandContext<FabricClientCommandSource> ctx
  ) throws CommandSyntaxException {
    // World in which the command was sent
    MinecraftClient client = MinecraftClient.getInstance();
		FabricClientCommandSource source = ctx.getSource();
    if (client.getServer() == null) {
      source.sendError(Text.literal("Failed: Multiplayer image creation coming soon..."));
      return 0;
    }
    ClientWorld level = source.getWorld();
    Entity entity = source.getEntity();
    if (entity == null) {
      source.sendError(Text.literal("Failed: Source is not an entity"));
      return 0;
    }
    Direction direction_view = entity.getFacing();
    Direction direction_right = direction_view.rotateYClockwise();
    BlockPos position = entity.getBlockPos().offset(direction_view, 2);
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
    Path path = this.image_argument.get_path(ctx, "src");
    BufferedImage image = null;
    try {
      image = ImageIO.read(path.toFile());
    } catch (IOException e) {
      throw new SimpleCommandExceptionType(Text.literal("Failed: Could not load image.")).create();
    }
    if (image == null) {
      throw new SimpleCommandExceptionType(Text.literal("Failed: Could not load image.")).create();
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
      throw new SimpleCommandExceptionType(Text.literal("Failed: No width or height set.")).create();
    }
    // Create image
    image_builder.generate_async(image, w, h, (WorldStructure structure) -> {
      try {
        world_transformer.place_async(
          level,
          structure,
          position,
          direction_right,
          Direction.UP,
          direction_view,
          (Void v) -> {
            source.sendFeedback(Text.literal(String.format("Successfully created %dx%d image", w, h)));
            return null;
          }
        );
      } catch (Exception e) {
        source.sendError(Text.literal("Failed: " + e.getMessage()));
        return null;
      }
      return null;
    });
    return 1;
  }

  // Reload block list
  private int reload_execute(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
    this.update_block_list();
    ctx.getSource().sendFeedback(Text.literal("Reloaded block list"));
    return 1;
  }

  // Undo an image create
  private int undo_execute(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
    world_transformer.undo_async((Exception e) -> {
      if (e != null) {
        ctx.getSource().sendError(Text.literal("Failed: " + e.getMessage()));
        return null;
      }
      ctx.getSource().sendFeedback(Text.literal("Successfully reverted last WorldTransform"));
      return null;
    });
    return 1;
  }

  // Redo an undone image create
  private int redo_execute(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
    this.world_transformer.redo_async((Exception e) -> {
      if (e != null) {
        ctx.getSource().sendError(Text.literal("Failed: " + e.getMessage()));
        return null;
      }
      ctx.getSource().sendFeedback(Text.literal("Successful redo of last undone WorldTransform"));
      return null;
    });
    return 1;
  }

  // Set the directory where the image create command looks for image files
  private int set_directory_execute(
    CommandContext<FabricClientCommandSource> ctx
  ) {
    Path dir = this.directory_argument.get_path(ctx, "dir");
    try {
      this.image_argument.set_root_directory(dir.toString());
      ctx.getSource().sendFeedback(Text.literal("Successfully set image directory to \"" + dir + "\""));
      return 1;
    } catch (NotDirectoryException e) {
      ctx.getSource().sendError(Text.literal("Provided path is not a directory"));
    } catch (FileNotFoundException e) {
      ctx.getSource().sendError(Text.literal("Provided path could not be found"));
    }
    return 0;
  }

  // Register command structure
  public void register(
    CommandDispatcher<FabricClientCommandSource> ctx,
    CommandRegistryAccess access
  ) {

    ImageModClient.LOGGER.info("Registering Commands...");

    this.world_transformer = new WorldTransformer();

    // update block list when we are registering command
    this.update_block_list();

    // image command
    LiteralArgumentBuilder<FabricClientCommandSource> root = ClientCommandManager.literal("image");

    // create option
    LiteralArgumentBuilder<FabricClientCommandSource> createLiteral = ClientCommandManager.literal("create");

    LiteralArgumentBuilder<FabricClientCommandSource> wLiteral  	= ClientCommandManager.literal("-width");
    LiteralArgumentBuilder<FabricClientCommandSource> hLiteral  	= ClientCommandManager.literal("-height");
    LiteralArgumentBuilder<FabricClientCommandSource> whInfoLiteral = ClientCommandManager.literal("~ ~");

    RequiredArgumentBuilder<FabricClientCommandSource, Path> srcArgument = ClientCommandManager.argument("src", image_argument);

    RequiredArgumentBuilder<FabricClientCommandSource, Integer> wArgument      = ClientCommandManager.argument("width", IntegerArgumentType.integer());
    RequiredArgumentBuilder<FabricClientCommandSource, Integer> wArgumentFinal = ClientCommandManager.argument("width", IntegerArgumentType.integer()).executes(this::create_execute);
    RequiredArgumentBuilder<FabricClientCommandSource, Integer> hArgumentFinal = ClientCommandManager.argument("height", IntegerArgumentType.integer()).executes(this::create_execute);

    wLiteral.then(wArgumentFinal);
    hLiteral.then(hArgumentFinal);
    wArgument.then(hArgumentFinal);

    srcArgument.then(wLiteral).then(hLiteral).then(whInfoLiteral).then(wArgument);
    createLiteral.then(srcArgument);

    // Reload option
    LiteralArgumentBuilder<FabricClientCommandSource> reload = ClientCommandManager.literal("reload").executes(this::reload_execute);

    // Undo
    LiteralArgumentBuilder<FabricClientCommandSource> undoLiteral = ClientCommandManager.literal("undo").executes(this::undo_execute);

    // Redo
    LiteralArgumentBuilder<FabricClientCommandSource> redoLiteral = ClientCommandManager.literal("redo").executes(this::redo_execute);;

    // SetDirectory
    LiteralArgumentBuilder<FabricClientCommandSource> setDirectoryLiteral = ClientCommandManager.literal("setDirectory");
    RequiredArgumentBuilder<FabricClientCommandSource, Path> directoryArgument  = ClientCommandManager.argument("dir", directory_argument).executes(this::set_directory_execute);

    setDirectoryLiteral.then(directoryArgument);

    // Root relations
    root.then(createLiteral).then(setDirectoryLiteral).then(reload).then(undoLiteral).then(redoLiteral);
    ctx.register(root);

    ImageModClient.LOGGER.info("Commands Registered.");

  }


}
