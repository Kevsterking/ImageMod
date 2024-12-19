package com.kevsterking.util;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class PathArgument implements ArgumentType<Path> {

    private static final Collection<String> EXAMPLES = Arrays.asList("\"" + System.getProperty("user.home") + "/Downloads\"");
    private static final SimpleCommandExceptionType PATH_NOT_FOUND = new SimpleCommandExceptionType(new LiteralMessage("Path not found"));

    public static Path root_directory = get_default_path();

    // Get the default path
    private static Path get_default_path() {
        try {
            return PathArgument.get_path(System.getProperty("user.home") + "/Downloads/");
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    // Get a directory that fails with exception if not a directory
    public static Path get_directory(Path path) throws FileNotFoundException, NotDirectoryException {
        Path dir = PathArgument.get_path(path);
        if (!dir.toFile().isDirectory()) throw new NotDirectoryException(path.toFile().getPath());
        return dir;
    }
    public static Path get_directory(String str) throws FileNotFoundException, NotDirectoryException {
        return PathArgument.get_directory(Paths.get(str));
    }

    // Get a path that fails with exception if does not exist
    public static Path get_path(Path path) throws FileNotFoundException {
        Path ret = PathArgument.root_directory != null ? PathArgument.root_directory.resolve(path) : path;
        if (ret.toFile().exists()) return ret;
        if (path.toFile().exists()) return path;
        throw new FileNotFoundException();
    }
    public static Path get_path(String str) throws FileNotFoundException {
    	return PathArgument.get_path(Paths.get(str));
    }
    public static Path get_path(final CommandContext<FabricClientCommandSource> context, final String name) {
        return context.getArgument(name, Path.class);
    }

    // Set the root directory
    public static void set_root_directory(String path) throws NotDirectoryException, FileNotFoundException {
        PathArgument.root_directory = get_directory(path);
    }

    // Get the path for file given input
    @Override
    public Path parse(StringReader reader) throws CommandSyntaxException {
        try {
            return PathArgument.get_path(StringArgumentType.string().parse(reader));
        } catch (FileNotFoundException e) {
            throw PathArgument.PATH_NOT_FOUND.createWithContext(reader);
        }
    }

    // Get short form string for path given root path of class
    private static String get_formatted_path(Path path) {
        Path p = path.startsWith(PathArgument.root_directory) ?
            PathArgument.root_directory.relativize(path) :
            path;
        return p.toString().replace("\\", "/") +
            (p.toFile().isDirectory() ? "/" : "");
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
            String directory = parsed.substring(0, i);
            String query = parsed.substring(i);
            Files.list(PathArgument.get_directory(directory))
                .filter(
                    path -> path.getFileName().toString().indexOf(query) == 0
                )
                .forEach(
                    path -> {
                        String path_str = PathArgument.get_formatted_path(path);
                        builder.suggest(
                            path_str.indexOf(' ') >= 0 ||
                            path_str.indexOf(':') >= 0 ?
                            "\"" + path_str + "\"" : path_str
                        );
                    }
                );
        } catch (Exception ignored) {}
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return PathArgument.EXAMPLES;
    }

}
