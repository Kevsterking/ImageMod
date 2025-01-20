package com.kevsterking.imagemod.neoforge.util;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class PathArgument implements ArgumentType<Path> {

  private static final Collection<String> EXAMPLES = Arrays.asList(
          "\"" + System.getProperty("user.home") + "/Downloads\"",
          "\"" + System.getProperty("user.home") + "/Desktop\""
  );
  private static final SimpleCommandExceptionType PATH_NOT_FOUND = new SimpleCommandExceptionType(new LiteralMessage("Path not found"));

  public static Path root_directory = get_default_path();

  public static String clean_path_string(String path) {
    return path.replace("\\", "/");
  }

  // Get the default path
  private static Path get_default_path() {
    return Paths.get(System.getProperty("user.home") + "/Downloads/");
  }

  // Get a directory that fails with exception if not a directory
  public Path get_directory(Path path) throws FileNotFoundException, NotDirectoryException {
    Path dir = this.get_path(path);
    if (!dir.toFile().isDirectory()) throw new NotDirectoryException(path.toFile().getPath());
    return dir;
  }
  public Path get_directory(String str) throws FileNotFoundException, NotDirectoryException {
    return this.get_directory(Paths.get(str));
  }

  // Get a path that fails with exception if it does not exist
  public Path get_path(Path path) throws FileNotFoundException {
    Path ret = PathArgument.root_directory != null ? PathArgument.root_directory.resolve(path) : path;
    if (ret.toFile().exists()) return ret;
    if (path.toFile().exists()) return path;
    throw new FileNotFoundException();
  }
  public Path get_path(String str) throws FileNotFoundException {
    return this.get_path(Paths.get(PathArgument.clean_path_string(str)));
  }
  public Path get_path(final CommandContext<CommandSourceStack> context, final String name) {
    return context.getArgument(name, Path.class);
  }

  // Set the root directory
  public void set_root_directory(String path) throws NotDirectoryException, FileNotFoundException {
    PathArgument.root_directory = this.get_directory(path);
  }

  // Get the path for file given input
  @Override
  public Path parse(StringReader reader) throws CommandSyntaxException {
    try {
      return this.get_path(StringArgumentType.string().parse(reader));
    } catch (FileNotFoundException e) {
      throw PathArgument.PATH_NOT_FOUND.createWithContext(reader);
    }
  }

  // Get short form string for path given root path of class
  private String get_formatted_path(Path path) {
    Path p = path.startsWith(PathArgument.root_directory) ?
      PathArgument.root_directory.relativize(path) :
      path;
    return PathArgument.clean_path_string(p.toString()) +
      (path.toFile().isDirectory() ? "/" : "");
  }

  // Filter unwanted path suggestions
  public boolean filter_path(Path path) {
    return true;
  }

  // Make sure string can be parsed as an argument string correctly
  private String argument_format(Path path) {
    String path_str = this.get_formatted_path(path);
    try {
      if (
        StringArgumentType
          .string()
          .parse(new StringReader(path_str))
          .compareTo(path_str) != 0
      ) {
        throw new Exception();
      }
      return path_str;
    } catch (Exception e) {
      return "\"" + path_str + "\"";
    }
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
          CommandContext<S> context,
          SuggestionsBuilder builder
  ) {
    StringReader stringReader = new StringReader(builder.getInput());
    stringReader.setCursor(builder.getStart());
    try {
      String parsed = StringArgumentType.string().parse(stringReader);
      int i = parsed.lastIndexOf("/") + 1;
      String query = parsed.substring(i);
      Path directory = this.get_directory(parsed.substring(0, i));
      try (Stream<Path> paths = Files.list(directory)) {
        paths
          .filter(path -> path.getFileName().toString().indexOf(query) == 0)
          .filter(this::filter_path)
          .forEach(path -> builder.suggest(this.argument_format(path)));
      }
    } catch (Exception ignored) {}
    return builder.buildFuture();
  }

  @Override
  public Collection<String> getExamples() {
    return PathArgument.EXAMPLES;
  }
}
