package com.kevsterking.util;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import java.nio.file.Path;

public class DirectoryArgument extends PathArgument {

    private static final SimpleCommandExceptionType NOT_DIRECTORY  = new SimpleCommandExceptionType(new LiteralMessage("Path not found"));

    @Override
    public Path parse(StringReader reader) throws CommandSyntaxException {

        Path ret = super.parse(reader);

        if (!ret.toFile().isDirectory()) {
            throw DirectoryArgument.NOT_DIRECTORY.createWithContext(reader);
        }

        return ret;
    }

}
