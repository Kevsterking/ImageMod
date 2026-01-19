package com.kevsterking.imagemod.core.commands;

import com.kevsterking.imagemod.core.ImageModCommandFunction;

import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;

public class ImageSetDirectoryFunction<C, E extends Exception> implements ImageModCommandFunction<C, E> {

  private final ImageCommandContext<C, E> cc;

  public ImageSetDirectoryFunction(ImageCommandContext<C, E> cc) {
    this.cc = cc;
  }

  @Override
  public int execute(C ctx) throws E {
    Path dir = cc.mod_interface.get_arg(ctx, "dir", Path.class);
    try {
      cc.arg_image.set_root_directory(dir.toString());
      cc.mod_interface.send_feedback(ctx, "Successfully set image directory to \"" + dir + "\"");
      return 1;
    } catch (NotDirectoryException e) {
      cc.mod_interface.send_error(ctx, "Provided path is not a directory");
    } catch (FileNotFoundException e) {
      cc.mod_interface.send_error(ctx, "Provided path could not be found");
    }
    return 0;
  }
}
