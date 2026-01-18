package com.kevsterking.imagemod.neoforge.util;

import java.nio.file.Files;
import java.nio.file.Path;

public class CommandArgumentImage extends CommandArgumentPath {
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
