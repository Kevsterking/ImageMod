package com.kevsterking.imagemod.core.argument;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import java.nio.file.Files;
import java.nio.file.Path;

public class CommandArgumentDirectory extends CommandArgumentPath {

    private static final SimpleCommandExceptionType NOT_DIRECTORY = new SimpleCommandExceptionType(new LiteralMessage("Path not found"));

    @Override
    public boolean filter(Path path) {
        try {
            if (Files.isDirectory(path))  {
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    @Override
    public Path parse(StringReader reader) throws CommandSyntaxException {
        Path ret = super.parse(reader);
        if (!ret.toFile().isDirectory()) {
            throw CommandArgumentDirectory.NOT_DIRECTORY.createWithContext(reader);
        }
        return ret;
    }

}
