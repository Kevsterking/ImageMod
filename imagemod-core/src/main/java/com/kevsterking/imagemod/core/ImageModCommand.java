package com.kevsterking.imagemod.core;

import com.mojang.brigadier.arguments.ArgumentType;

import java.util.Arrays;
import java.util.List;

public class ImageModCommand {

  public final String name;
  public final ArgumentType<?> argument;
  public List<ImageModCommand> children;

  public ImageModCommandFunction<?,?> command_function;

  private ImageModCommand(
          String name,
          ArgumentType<?> argument,
          ImageModCommandFunction<?,?> command_function,
          List<ImageModCommand> children
  ) {
    this.name = name;
    this.argument = argument;
    this.command_function = command_function;
    this.children = children;
  }

  public final ImageModCommand next(ImageModCommand... sub) {
    return new ImageModCommand(this.name, this.argument, this.command_function, Arrays.stream(sub).toList());
  }

  public static ImageModCommand literal(String name) {
    return new ImageModCommand(name, null, null, null);
  }

  public static ImageModCommand arg(String name, ArgumentType<?> type) {
    return new ImageModCommand(name, type, null, null);
  }

  public ImageModCommand executes(ImageModCommandFunction<?,?> command_function) {
    return new ImageModCommand(this.name, this.argument, command_function, this.children);
  }

}