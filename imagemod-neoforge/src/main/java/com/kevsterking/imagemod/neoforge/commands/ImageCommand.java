package com.kevsterking.imagemod.neoforge.commands;

import com.kevsterking.imagemod.neoforge.ImageBuilder.ImageBlock;
import com.kevsterking.imagemod.neoforge.ImageBuilder.Mosaic.MosaicIntColThread;
import com.kevsterking.imagemod.neoforge.ImageModClient;
import com.kevsterking.imagemod.neoforge.WorldTransformer.WorldStructure;
import com.kevsterking.imagemod.neoforge.WorldTransformer.WorldTransformer;
import com.kevsterking.imagemod.neoforge.util.CommandArgumentDirectory;
import com.kevsterking.imagemod.neoforge.util.CommandArgumentImage;
import com.kevsterking.imagemod.neoforge.util.CommandArgumentPath;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.ArrayList;

public class ImageCommand {

  private final CommandArgumentPath arg_image = new CommandArgumentImage();
  private final CommandArgumentPath arg_set_directory = new CommandArgumentDirectory();

  private final ArrayList<ImageBlock> image_blocks = new ArrayList<>();
  private final MosaicIntColThread image_builder = new MosaicIntColThread();
  private WorldTransformer world_transformer;

  public ImageCommand() {
    try {
      this.arg_set_directory.set_root_directory(System.getProperty("user.home"));
    } catch (Exception ignored) {}
  }

  // Update block list
  public void update_block_list() {
    ImageModClient.LOGGER.info("Loading block list");
    for (Block block : BuiltInRegistries.BLOCK.stream().toList()) {
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
    CommandContext<CommandSourceStack> ctx
  ) throws CommandSyntaxException {
    // World in which the command was sent
    Minecraft client = Minecraft.getInstance();
		CommandSourceStack source = ctx.getSource();
    if (client.getSingleplayerServer() == null) {
      source.sendFailure(Component.literal("Failed: Multiplayer image creation coming soon..."));
      return 0;
    }
    Level level = source.getUnsidedLevel();
    Entity entity = source.getEntity();
    if (entity == null) {
      source.sendFailure(Component.literal("Failed: Source is not an entity"));
      return 0;
    }
    Direction direction_view = entity.getDirection();
    Direction direction_right = direction_view.getClockWise();
    BlockPos position = entity.blockPosition().relative(direction_view, 2);
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
    Path path = this.arg_image.get_path(ctx, "src");
    BufferedImage image = null;
    try {
      image = ImageIO.read(path.toFile());
    } catch (IOException e) {
      throw new SimpleCommandExceptionType(Component.literal("Failed: Could not load image.")).create();
    }
    if (image == null) {
      throw new SimpleCommandExceptionType(Component.literal("Failed: Could not load image.")).create();
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
      throw new SimpleCommandExceptionType(Component.literal("Failed: No width or height set.")).create();
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
            source.sendSuccess(() -> Component.literal(String.format("Successfully created %dx%d image", w, h)), false);
            return null;
          }
        );
      } catch (Exception e) {
        source.sendFailure(Component.literal("Failed: " + e.getMessage()));
        return null;
      }
      return null;
    });
    return 1;
  }

  // Reload block list
  private int reload_execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
    this.update_block_list();
    ctx.getSource().sendSuccess(() -> Component.literal("Reloaded block list"), false);
    return 1;
  }

  // Undo an image create
  private int undo_execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
    world_transformer.undo_async((Exception e) -> {
      if (e != null) {
        ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
        return null;
      }
      ctx.getSource().sendSuccess(() -> Component.literal("Successfully reverted last WorldTransform"), false);
      return null;
    });
    return 1;
  }

  // Redo an undone image create
  private int redo_execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
    this.world_transformer.redo_async((Exception e) -> {
      if (e != null) {
        ctx.getSource().sendFailure(Component.literal("Failed: " + e.getMessage()));
        return null;
      }
      ctx.getSource().sendSuccess(() -> Component.literal("Successful redo of last undone WorldTransform"), false);
      return null;
    });
    return 1;
  }

  // Set the directory where the image create command looks for image files
  private int set_directory_execute(CommandContext<CommandSourceStack> ctx) {
    Path dir = this.arg_set_directory.get_path(ctx, "dir");
    try {
      this.arg_image.set_root_directory(dir.toString());
      ctx.getSource().sendSuccess(() -> Component.literal("Successfully set image directory to \"" + dir + "\""), false);
      return 1;
    } catch (NotDirectoryException e) {
      ctx.getSource().sendFailure(Component.literal("Provided path is not a directory"));
    } catch (FileNotFoundException e) {
      ctx.getSource().sendFailure(Component.literal("Provided path could not be found"));
    }
    return 0;
  }

  // Register command structure
  public void register(CommandDispatcher<CommandSourceStack> ctx) {

    ImageModClient.LOGGER.info("Registering Commands...");

    this.world_transformer = new WorldTransformer();

    // update block list when we are registering command
    this.update_block_list();

    // image command
    LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("image");

    // create option
    LiteralArgumentBuilder<CommandSourceStack> createLiteral = Commands.literal("create");

    LiteralArgumentBuilder<CommandSourceStack> wLiteral  	= Commands.literal("-width");
    LiteralArgumentBuilder<CommandSourceStack> hLiteral  	= Commands.literal("-height");
    LiteralArgumentBuilder<CommandSourceStack> whInfoLiteral = Commands.literal("~ ~");

    RequiredArgumentBuilder<CommandSourceStack, Path> srcArgument = Commands.argument("src", arg_image);

    RequiredArgumentBuilder<CommandSourceStack, Integer> wArgument      = Commands.argument("width", IntegerArgumentType.integer());
    RequiredArgumentBuilder<CommandSourceStack, Integer> wArgumentFinal = Commands.argument("width", IntegerArgumentType.integer()).executes(this::create_execute);
    RequiredArgumentBuilder<CommandSourceStack, Integer> hArgumentFinal = Commands.argument("height", IntegerArgumentType.integer()).executes(this::create_execute);

    wLiteral.then(wArgumentFinal);
    hLiteral.then(hArgumentFinal);
    wArgument.then(hArgumentFinal);

    srcArgument.then(wLiteral).then(hLiteral).then(whInfoLiteral).then(wArgument);
    createLiteral.then(srcArgument);

    // Reload option
    LiteralArgumentBuilder<CommandSourceStack> reload = Commands.literal("reload").executes(this::reload_execute);

    // Undo
    LiteralArgumentBuilder<CommandSourceStack> undoLiteral = Commands.literal("undo").executes(this::undo_execute);

    // Redo
    LiteralArgumentBuilder<CommandSourceStack> redoLiteral = Commands.literal("redo").executes(this::redo_execute);;

    // SetDirectory
    LiteralArgumentBuilder<CommandSourceStack> setDirectoryLiteral = Commands.literal("setDirectory");
    RequiredArgumentBuilder<CommandSourceStack, Path> directoryArgument  = Commands.argument("dir", arg_set_directory).executes(this::set_directory_execute);

    setDirectoryLiteral.then(directoryArgument);

    // Root relations
    root.then(createLiteral).then(setDirectoryLiteral).then(reload).then(undoLiteral).then(redoLiteral);
    ctx.register(root);

    ImageModClient.LOGGER.info("Commands Registered.");

  }


}
