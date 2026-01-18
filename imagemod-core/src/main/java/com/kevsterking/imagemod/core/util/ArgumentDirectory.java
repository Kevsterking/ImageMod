package com.kevsterking.imagemod.core.util;

import java.nio.file.Files;
import java.nio.file.Path;

public class ArgumentDirectory extends ArgumentPath {
  @Override
  public boolean filter(Path path) {
    try {
      if (Files.isDirectory(path)) return true;
    } catch (Exception ignored) {}
    return false;
  }
}
