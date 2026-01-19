package com.kevsterking.imagemod.core.commands;

import com.kevsterking.imagemod.core.ImageBlock;
import com.kevsterking.imagemod.core.ImageMod;
import com.kevsterking.imagemod.core.ImageModInterface;
import com.kevsterking.imagemod.core.argument.CommandArgumentDirectory;
import com.kevsterking.imagemod.core.argument.CommandArgumentImage;
import com.kevsterking.imagemod.core.argument.CommandArgumentPath;
import com.kevsterking.imagemod.core.mosaic.MosaicIntColThread;
import com.kevsterking.imagemod.core.transform.WorldStructure;
import com.kevsterking.imagemod.core.transform.WorldTransformCreation;
import com.kevsterking.imagemod.core.transform.WorldTransformer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.ArrayList;

public class ImageCommand<T extends SharedSuggestionProvider> {

  public static final SimpleCommandExceptionType MULTIPLAYER_NOT_SUPPORTED = new SimpleCommandExceptionType(Component.literal("Multiplayer not supported yet..."));
  private static final SimpleCommandExceptionType NOT_SINGLEPLAYER = new SimpleCommandExceptionType(Component.literal("Multiplayer image creation is not supported yet"));
  private static final SimpleCommandExceptionType NO_DIMENSIONS = new SimpleCommandExceptionType(Component.literal("You must specify at least width or height"));
  private static final DynamicCommandExceptionType IMAGE_LOAD_FAILED = new DynamicCommandExceptionType(str -> Component.literal("Image load failed: " + str));

  private final CommandArgumentPath arg_image = new CommandArgumentImage();
  private final CommandArgumentPath arg_set_directory = new CommandArgumentDirectory();

  private final ArrayList<ImageBlock> image_blocks = new ArrayList<>();
  private final MosaicIntColThread image_builder = new MosaicIntColThread();
  private WorldTransformer world_transformer;

  private final ImageModInterface<T> mod_interface;

  public ImageCommand(ImageModInterface<T> mod_interface) {
    this.mod_interface = mod_interface;
    try {
      this.arg_set_directory.set_root_directory(System.getProperty("user.home"));
    } catch (Exception ignored) {}
  }

  // Update block list
  public void update_block_list() {
    ImageMod.LOGGER.info("Loading block list");
    for (Block block : BuiltInRegistries.BLOCK) {
      try {
        if (ImageBlock.filter_block(block)) {
          this.image_blocks.add(ImageBlock.get(block));
          ImageMod.LOGGER.debug("{} - ACCEPTED", block.getName().getString());
        }
      } catch (Exception e) {
        ImageMod.LOGGER.debug("{} - REJECTED: {}", block.getName().getString(), e.getMessage());
      }
    }
    this.image_blocks.add(ImageBlock.get_air());
    ImageBlock[] blocks = new ImageBlock[this.image_blocks.size()];
    for (int i = 0; i < this.image_blocks.size(); i++) {
      blocks[i] = this.image_blocks.get(i);
    }
    this.image_builder.set_tiles(blocks, 16);
    ImageMod.LOGGER.info("Block list loading complete.");
  }

  private BufferedImage load_image(Path path) throws CommandSyntaxException {
    try {
      BufferedImage img = ImageIO.read(path.toFile());
      if (img == null) throw new IOException("ImageIO returned null");
      return img;
    } catch (IOException e) {
      throw IMAGE_LOAD_FAILED.create(e.getMessage());
    }
  }

  private Dimension get_dimension(
          CommandContext<T> ctx,
          BufferedImage img
  ) throws CommandSyntaxException {
    int w=0,h=0;
    try {
      w = IntegerArgumentType.getInteger(ctx, "width");
    } catch(Exception ignore) {}
    try {
      h = IntegerArgumentType.getInteger(ctx, "height");
    } catch(Exception ignore) {}
    if (w == 0 && h == 0) throw NO_DIMENSIONS.create();
    if (w != 0 && h != 0) return new Dimension(w, h);
    double ratio = (double) img.getWidth() / img.getHeight();
    return (w != 0) ? new Dimension(w, (int) (w / ratio)) : new Dimension((int) (h * ratio), h);
  }

  private int create_execute(
    CommandContext<T> ctx
  ) throws CommandSyntaxException {
    WorldTransformCreation creation = new WorldTransformCreation();
    T source = ctx.getSource();
    if (!Minecraft.getInstance().isSingleplayer()) throw NOT_SINGLEPLAYER.create();
    Entity entity = this.mod_interface.ctx_get_entity(source);
    creation.level = this.mod_interface.ctx_get_level(source);
    creation.direction_z = entity.getDirection();
    creation.direction_x = creation.direction_z.getClockWise();
    creation.direction_y = Direction.UP;
    creation.position = entity.blockPosition().relative(creation.direction_z, 2);
    BufferedImage image = load_image(ctx.getArgument("src", Path.class));
    Dimension dim = get_dimension(ctx, image);
    // Generate block image
    image_builder.generate_async(image, dim.width, dim.height, (WorldStructure structure) -> {
      try {
        world_transformer.place_async(structure, creation, (Void v) -> {
          this.mod_interface.ctx_send_feedback(source, Component.literal(String.format("Successfully created %dx%d image", dim.width, dim.height)));
          return null;
        });
      } catch (Exception e) {
        this.mod_interface.ctx_send_error(source, Component.literal("Failed to place image: " + e.getMessage()));
        return null;
      }
      return null;
    });
    return 1;
  }

  // Reload block list
  private int reload_execute(
          CommandContext<T> ctx
  ) throws CommandSyntaxException {
    this.update_block_list();
    this.mod_interface.ctx_send_feedback(ctx.getSource(), Component.literal("Reloaded block list"));
    return 1;
  }

  // Undo an image create
  private int undo_execute(
          CommandContext<T> ctx
  ) throws CommandSyntaxException {
    world_transformer.undo_async((Exception e) -> {
      if (e != null) {
        this.mod_interface.ctx_send_error(ctx.getSource(), Component.literal("Failed: " + e.getMessage()));
        return null;
      }
      this.mod_interface.ctx_send_feedback(ctx.getSource(), Component.literal("Successfully reverted last WorldTransform"));
      return null;
    });
    return 1;
  }

  // Redo an undone image create
  private int redo_execute(
          CommandContext<T> ctx
  ) throws CommandSyntaxException {
    this.world_transformer.redo_async((Exception e) -> {
      if (e != null) {
        this.mod_interface.ctx_send_error(ctx.getSource(), Component.literal("Failed: " + e.getMessage()));
        return null;
      }
      this.mod_interface.ctx_send_feedback(ctx.getSource(), Component.literal("Successful redo of last undone WorldTransform"));
      return null;
    });
    return 1;
  }

  // Set the directory where the image create command looks for image files
  private int set_directory_execute(
    CommandContext<T> ctx
  ) {
    Path dir = ctx.getArgument("dir", Path.class);
    try {
      this.arg_image.set_root_directory(dir.toString());
      this.mod_interface.ctx_send_feedback(ctx.getSource(), Component.literal("Successfully set image directory to \"" + dir + "\""));
      return 1;
    } catch (NotDirectoryException e) {
      this.mod_interface.ctx_send_error(ctx.getSource(), Component.literal("Provided path is not a directory"));
    } catch (FileNotFoundException e) {
      this.mod_interface.ctx_send_error(ctx.getSource(), Component.literal("Provided path could not be found"));
    }
    return 0;
  }

  // Register command structure
  public void register(
    CommandDispatcher<T> ctx,
    CommandBuildContext ignored
  ) {
    ImageMod.LOGGER.info("Registering Commands...");
    this.world_transformer = new WorldTransformer();
    // update block list when we are registering command
    this.update_block_list();
    // image command
    LiteralArgumentBuilder<T> root = this.mod_interface.command_literal("image");
    // create option
    LiteralArgumentBuilder<T> create_literal = this.mod_interface.command_literal("create");
    LiteralArgumentBuilder<T> w_literal = this.mod_interface.command_literal("-width");
    LiteralArgumentBuilder<T> h_literal = this.mod_interface.command_literal("-height");
    LiteralArgumentBuilder<T> wh_info_Literal = this.mod_interface.command_literal("~ ~");
    RequiredArgumentBuilder<T, Path> src_arg = this.mod_interface.command_argument("src", arg_image);
    RequiredArgumentBuilder<T, Integer> w_arg = this.mod_interface.command_argument("width", IntegerArgumentType.integer());
    RequiredArgumentBuilder<T, Integer> w_arg_final = this.mod_interface.command_argument("width", IntegerArgumentType.integer()).executes(this::create_execute);
    RequiredArgumentBuilder<T, Integer> h_arg_final = this.mod_interface.command_argument("height", IntegerArgumentType.integer()).executes(this::create_execute);
    //
    w_literal.then(w_arg_final);
    h_literal.then(h_arg_final);
    w_arg.then(h_arg_final);
    src_arg.then(w_literal).then(h_literal).then(wh_info_Literal).then(w_arg);
    create_literal.then(src_arg);
    // Reload option
    LiteralArgumentBuilder<T> reload = this.mod_interface.command_literal("reload").executes(this::reload_execute);
    // Undo
    LiteralArgumentBuilder<T> undoLiteral = this.mod_interface.command_literal("undo").executes(this::undo_execute);
    // Redo
    LiteralArgumentBuilder<T> redoLiteral = this.mod_interface.command_literal("redo").executes(this::redo_execute);;
    // SetDirectory
    LiteralArgumentBuilder<T> setDirectoryLiteral = this.mod_interface.command_literal("setDirectory");
    RequiredArgumentBuilder<T, Path> directoryArgument = this.mod_interface.command_argument("dir", arg_set_directory).executes(this::set_directory_execute);
    setDirectoryLiteral.then(directoryArgument);
    // Root relations
    root.then(create_literal).then(setDirectoryLiteral).then(reload).then(undoLiteral).then(redoLiteral);
    ctx.register(root);
    ImageMod.LOGGER.info("Commands Registered.");
  }

}
