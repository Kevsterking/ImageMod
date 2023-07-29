package ImageMod.util;

import com.google.gson.JsonObject;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;

import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class PathArgument implements ArgumentType<Path> {

    private static final Collection<String> EXAMPLES = Arrays.asList("\"" + System.getProperty("user.home") + "\"");
    private static final SimpleCommandExceptionType PATH_NOT_FOUND = new SimpleCommandExceptionType(new LiteralMessage("Path not found"));

    public static Path rootDir;

    static {
        try {
            rootDir = PathArgument.getPath(System.getProperty("user.home") + "/Downloads");
        } catch(FileNotFoundException e) {
            try {
                rootDir = PathArgument.getPath(System.getProperty("user.home"));
            } catch (FileNotFoundException ex) {
                rootDir = null;
                ex.printStackTrace();
            }
        }
    }

    public PathArgument() {
        try {
            this.rootDir = this.getPath(System.getProperty("user.home"));
        } catch (Exception e) {
            System.out.println("Exception in PathArgument");
            e.printStackTrace();
        }
    }

    public static Path getPath(Path path) throws FileNotFoundException {

        if (PathArgument.rootDir != null) {
            try {
                Path ret = PathArgument.rootDir.resolve(path);
                if (ret.toFile().exists()) {
                    return ret;
                }
            } catch (Exception e) {
                System.out.println("Exception in PathArgument");
                e.printStackTrace();
            }
        }

        try {
            if (path.toFile().exists()) {
                return path;
            }
        } catch (Exception e) {
            System.out.println("Exception in PathArgument");
            e.printStackTrace();
        }

        throw new FileNotFoundException();
    }
    public static Path getPath(String str) throws FileNotFoundException {

    	try {
            return PathArgument.getPath(Paths.get(str));
        } catch (Exception e) {
            System.out.println("Exception in PathArgument");
            e.printStackTrace();
        }

        throw new FileNotFoundException();
    }

    public static Path getDirectory(Path path) throws FileNotFoundException, NotDirectoryException {
        Path dir = PathArgument.getPath(path);
        if (!dir.toFile().isDirectory()) {
            throw new NotDirectoryException(path.toFile().getPath());
        }
        return dir;
    }
    public static Path getDirectory(String str) throws FileNotFoundException, NotDirectoryException {

        Path ret = null;

        try {
            ret = PathArgument.getPath(str);
        } catch (Exception e) {
            System.out.println("Exception in PathArgument");
            e.printStackTrace();
        }

        return PathArgument.getDirectory(ret);
    }

    public static void setRootDirectory(Path path) throws FileNotFoundException, NotDirectoryException {
        PathArgument.rootDir = PathArgument.getDirectory(path);
    }
    public static void setRootDirectory(String dir) throws FileNotFoundException, NotDirectoryException {
        PathArgument.rootDir = PathArgument.getDirectory(dir);
    }

    @Override
    public Path parse(StringReader reader) throws CommandSyntaxException {

        String str = StringArgumentType.string().parse(reader);
        Path ret;

        try {
            ret = PathArgument.getPath(str);
        } catch (FileNotFoundException e) {
            throw PathArgument.PATH_NOT_FOUND.createWithContext(reader);
        }

        return ret;
    }

    public static Path getPath(final CommandContext<CommandSourceStack> context, final String name) {
        return context.getArgument(name, Path.class);
    }

    private String getFormattedPathString(Path path) {

    	String ret;
        
    	Path placeholder;
    	Path relative = PathArgument.rootDir.relativize(path);

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
        } catch (CommandSyntaxException e) {
            System.out.println("Exception in PathArgument");
            e.printStackTrace();
        }

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
        catch (Exception e) {
            System.out.println("Exception in PathArgument");
            e.printStackTrace();
        }

        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return PathArgument.EXAMPLES;
    }

}
