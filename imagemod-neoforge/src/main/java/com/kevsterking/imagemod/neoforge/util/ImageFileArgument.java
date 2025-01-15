package com.kevsterking.imagemod.neoforge.util;

import java.nio.file.Files;
import java.nio.file.Path;

public class ImageFileArgument extends PathArgument {
    @Override
    public boolean filter_path(Path path) {
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
