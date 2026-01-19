package com.kevsterking.imagemod.core.commands;

import com.kevsterking.imagemod.core.*;
import com.mojang.brigadier.arguments.IntegerArgumentType;

public class ImageCommand<C, E extends Exception> {

  private final ImageCommandContext<C, E> cc;

  private final ImageCreateFunction<C, E> cmd_create;
  private final ImageUndoFunction<C, E> cmd_undo;
  private final ImageRedoFunction<C, E> cmd_redo;
  private final ImageReloadFunction<C, E> cmd_reload;
  private final ImageSetDirectoryFunction<C, E> cmd_set_directory;

  public ImageCommand(ImageModCommandInterface<C, E> mod_interface) {
    this.cc = new ImageCommandContext<>(mod_interface);
    this.cmd_create = new ImageCreateFunction<>(cc);
    this.cmd_undo = new ImageUndoFunction<>(cc);
    this.cmd_redo = new ImageRedoFunction<>(cc);
    this.cmd_reload = new ImageReloadFunction<>(cc);
    this.cmd_set_directory = new ImageSetDirectoryFunction<>(cc);
  }

  public ImageModCommand get_command(C ctx) {
    // Helper Commands
    ImageModCommand src = ImageModCommand.arg("src", cc.arg_image);
    ImageModCommand w = ImageModCommand.arg("width", IntegerArgumentType.integer());
    ImageModCommand h = ImageModCommand.arg("height",IntegerArgumentType.integer());
    // Image command
    return ImageModCommand.literal("image").next(
      ImageModCommand.literal("setDirectory").next(
        ImageModCommand.arg("dir", cc.arg_set_directory).executes(this.cmd_set_directory)
      ),
      ImageModCommand.literal("create").next(
        src.next(
          ImageModCommand.literal("-width").next(w).executes(this.cmd_create),
          ImageModCommand.literal("-height").next(h).executes(this.cmd_create),
          ImageModCommand.literal("~ ~").next(w).next(h).executes(this.cmd_create)
        )
      ),
      ImageModCommand.literal("undo").executes(this.cmd_undo),
      ImageModCommand.literal("redo").executes(this.cmd_redo),
      ImageModCommand.literal("reload").executes(this.cmd_reload)
    );
  }

}
