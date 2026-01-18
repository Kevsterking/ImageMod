package com.kevsterking.imagemod.core.util;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ArgumentPath {

  public Path ROOT_DIR = Path.of(System.getProperty("user.home") + "/Downloads");

  // Parse string either "<--between quotes-->" or until first empty space
  public static String parse_string(String str) {
    if (str == null) return "";
    Matcher m = Pattern.compile("^\"([^\"]*)\"|^([^ ]*)").matcher(str);
    return m.find() ? (m.group(1) != null ? m.group(1) : m.group(2)) : "";
  }

  // Additional filter configurable to type of PathAgument Used
  public boolean filter(Path path) {
    return true;
  }

  // Get short form string for path given root path of class
  public String format_path(Path path) {
    return (
      path.startsWith(this.ROOT_DIR) ? this.ROOT_DIR.relativize(path) : path).toString()
      .replace("\\", "/") + (path.toFile().isDirectory() ? "/" : ""
    );
  }
  // Get short form string for path given root path of class
  public String format_path(String str) {
    return this.format_path(Path.of(str));
  }

  public Path get_path(Path path) throws FileNotFoundException {
    Path ret = this.ROOT_DIR != null ? this.ROOT_DIR.resolve(path) : path;
    if (ret.toFile().exists()) return ret;
    if (path.toFile().exists()) return path;
    throw new FileNotFoundException();
  }
  public Path get_path(String str) throws FileNotFoundException {
    return this.get_path(Paths.get(this.format_path(str)));
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

  // Set root directory
  public void set_root_directory(Path path) throws NotDirectoryException, FileNotFoundException {
    this.ROOT_DIR = this.get_directory(path);
  }
  public void set_root_directory(String str) throws NotDirectoryException, FileNotFoundException {
    this.set_root_directory(this.get_directory(str));
  }

  // Get a list of suggestions given a path query
  // Ex. somefolder/image1. -> somefolder/image1.jpg
    // Helper method, get suggested paths from
  // some root directory with a string query
  public List<String> get_suggestions(String str) {
    int i = Math.max(str.lastIndexOf('/'), str.lastIndexOf('\\'));
    String dir = (i == -1) ? "" : str.substring(0, i + 1).replaceAll("\"", "");
    String query = (i == -1) ? str : str.substring(i + 1);
    try (Stream<Path> paths = Files.list(this.get_directory(dir)).filter(this::filter)) {
      return paths.filter(
        path -> path.getFileName().toString().indexOf(query) == 0
      ).filter(this::filter).map(this::format_path).toList();
    } catch (Exception ignore) {}
    return List.of();
  }

}
