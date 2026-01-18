package com.kevsterking.imagemod.core.util;

import java.nio.file.Files;
import java.nio.file.Path;

public class ArgumentImage extends ArgumentPath {
    @Override
    public boolean filter(Path path) {
        try {
            String type = Files.probeContentType(path);
            if (
                (type != null && type.split("/")[0].equals("image")) ||
                Files.isDirectory(path)
            )  {
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }
}
