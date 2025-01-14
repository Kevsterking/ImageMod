package com.kevsterking.imagemod.core.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class PathArgument {

  public static Path ROOT_DIR = Path.of(System.getProperty("user.home") + "/Downloads");

  private static String get_path_string(Path path) {
    return path.toString()
      .replace("\\", "/") +
      (path.toFile().isDirectory() ? "/" : "");
  }

  // Get short form string for path given root path of class
  private static String format_path(Path path) {
    if (path.startsWith(PathArgument.ROOT_DIR))
      path = PathArgument.ROOT_DIR.relativize(path);
    return get_path_string(path);
  }

  // Additional filter configurable to type of PathAgument Used
  public boolean filter(Path path) {
    return true;
  }

  // Helper method, get suggested paths from
  // some root directory with a string query
  private String[] get_suggestions_in(Path root, String query) throws IOException {
    try (Stream<Path> paths = Files.list(root)) {
      return (String[]) paths.filter(
          path -> path.getFileName().toString().indexOf(query) == 0
        )
        .filter(this::filter)
        .map(PathArgument::format_path).toArray();
    }
  }

  // Get a list of suggestions given a path query
  // Ex. somefolder/image1. -> somefolder/image1.jpg
  public String[] get_suggestions(String path) throws IOException {
    int i = path.lastIndexOf("/") + 1;
    return get_suggestions_in(Path.of(path.substring(0, i)), path.substring(i));
  }

}
