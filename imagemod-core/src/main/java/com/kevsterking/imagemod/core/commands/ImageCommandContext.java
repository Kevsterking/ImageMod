package com.kevsterking.imagemod.core.commands;

import com.kevsterking.imagemod.core.util.ImageBlock;
import com.kevsterking.imagemod.core.ImageModCommandException;
import com.kevsterking.imagemod.core.ImageModCommandInterface;
import com.kevsterking.imagemod.core.argument.CommandArgumentDirectory;
import com.kevsterking.imagemod.core.argument.CommandArgumentImage;
import com.kevsterking.imagemod.core.argument.CommandArgumentPath;
import com.kevsterking.imagemod.core.mosaic.MosaicIntColThread;
import com.kevsterking.imagemod.core.transform.WorldTransformer;

import java.util.ArrayList;

public class ImageCommandContext<C, E extends Exception>  {

  public final ImageModCommandException<E> NO_DIMENSIONS;
  public final ImageModCommandException<E> IMAGE_LOAD_FAILED;
  public final ImageModCommandException<E> NOT_SINGLEPLAYER;
  public final ImageModCommandException<E> MULTIPLAYER_NOT_SUPPORTED;

  public final CommandArgumentPath arg_image = new CommandArgumentImage();
  public final CommandArgumentPath arg_set_directory = new CommandArgumentDirectory();

  public final ArrayList<ImageBlock> image_blocks = new ArrayList<>();
  public final MosaicIntColThread image_builder = new MosaicIntColThread();
  public WorldTransformer world_transformer;

  public final ImageModCommandInterface<C, E> mod_interface;

  public ImageCommandContext(ImageModCommandInterface<C, E> mod_interface) {
    this.mod_interface = mod_interface;
    this.NO_DIMENSIONS = mod_interface.exception(str -> "You must specify at least width or height");
    this.IMAGE_LOAD_FAILED = mod_interface.exception(str -> "Image load failed: " + str);
    this.NOT_SINGLEPLAYER = mod_interface.exception(str -> "Multiplayer image creation is not supported yet");
    this.MULTIPLAYER_NOT_SUPPORTED = mod_interface.exception(str -> "Multiplayer not supported yet...");
    // Should probably do this directly in the arg?
    try {
      this.arg_set_directory.set_root_directory(System.getProperty("user.home"));
    } catch (Exception ignored) {}
  }

}
