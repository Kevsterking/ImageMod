package com.kevsterking.imagemod.util;

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
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class PathArgument implements ArgumentType<Path> {

    private static Collection<String> EXAMPLES = Arrays.asList("\"" + System.getProperty("user.home") + "\"");
    private static SimpleCommandExceptionType PATH_NOT_FOUND = new SimpleCommandExceptionType(new LiteralMessage("Path not found"));

    public Path rootDir = null;

    public PathArgument() {
        try {
            this.rootDir = this.getPath(System.getProperty("user.home"));
        } catch (Exception e) {}
    }

    public Path getPath(Path path) throws FileNotFoundException {

        if (this.rootDir != null) {
            try {
                Path ret = this.rootDir.resolve(path);
                if (ret.toFile().exists()) {
                    return ret;
                }
            } catch (Exception e) {}
        }

        try {
            if (path.toFile().exists()) {
                return path;
            }
        } catch (Exception e) {}

        throw new FileNotFoundException();
    }
    public Path getPath(String str) throws FileNotFoundException {
        
    	try {
            return this.getPath(Paths.get(str));
        } catch (Exception e) {}

        throw new FileNotFoundException();
    }

    public Path getDirectory(Path path) throws FileNotFoundException, NotDirectoryException {
        Path dir = this.getPath(path);
        if (!dir.toFile().isDirectory()) {
            throw new NotDirectoryException(path.toFile().getPath());
        }
        return dir;
    }
    public Path getDirectory(String str) throws FileNotFoundException, NotDirectoryException {

        Path ret = null;

        try {
            ret = this.getPath(str);
        } catch (Exception e) {}

        return this.getDirectory(ret);
    }

    public void setRootDirectory(Path path) throws FileNotFoundException, NotDirectoryException {
        this.rootDir = this.getDirectory(path);
    }
    public void setRootDirectory(String dir) throws FileNotFoundException, NotDirectoryException {
        this.rootDir = this.getDirectory(dir);
    }

    @Override
    public Path parse(StringReader reader) throws CommandSyntaxException {

        String str = StringArgumentType.string().parse(reader);
        Path ret;

        try {
            ret = this.getPath(str);
        } catch (FileNotFoundException e) {
            throw PathArgument.PATH_NOT_FOUND.createWithContext(reader);
        }

        return ret;
    }

    public static Path getPath(final CommandContext<CommandSourceStack> context, final String name) {
        return context.getArgument(name, Path.class);
    }

    private String getFormattedPathString(Path path) {

    	String ret = "";
        
    	Path placeholder;
    	Path relative = this.rootDir.relativize(path);

        if (!relative.toString().contains("..")) {
            placeholder = relative;
        } else {
        	placeholder = path;
        }
        
        if (path.toFile().isDirectory()) {
            ret = placeholder.toString() + "/";
        } else  {
            ret = placeholder.toString();
        }

        ret = ret.replace("\\", "/");
        return ret;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {

        StringReader stringReader = new StringReader(builder.getInput());
        stringReader.setCursor(builder.getStart());

        String totalString  = null;
        String pathStr      = null;
        String searchString = null;

        try {
            totalString = StringArgumentType.string().parse(stringReader);
        } catch (CommandSyntaxException e) {}

        if (totalString != null) {
            int dirIndex = totalString.lastIndexOf("/") + 1;
            if (dirIndex >= 0) {
                pathStr      = totalString.substring(0, dirIndex);
                searchString = totalString.substring(dirIndex);
            }
        }

        try {
            Path path = this.getPath(pathStr);
            int i = 0;
            for (String str : path.toFile().list()) {
                if (str.indexOf(searchString) == 0) {
                    if (++i > 100) break;
                    String suggestion = this.getFormattedPathString(path.resolve(str));
                    builder.suggest("\"" + suggestion + "\"");
                }
            }
        }
        catch (FileNotFoundException e) {}
        catch (Exception e) {}

        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return PathArgument.EXAMPLES;
    }

}
