package com.kevsterking.imagemod.core.argument;

import com.kevsterking.imagemod.core.util.ArgumentPath;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class CommandArgumentPath extends ArgumentPath implements ArgumentType<Path> {

  private static final Collection<String> EXAMPLES = Arrays.asList(
    "\"" + System.getProperty("user.home") + "/Downloads\"",
    "\"" + System.getProperty("user.home") + "/Desktop\""
  );

  private static final SimpleCommandExceptionType PATH_NOT_FOUND =
    new SimpleCommandExceptionType(new LiteralMessage("Path not found"));

  @Override
  // Get the path for file given input
  public Path parse(StringReader reader) throws CommandSyntaxException {
    String input = StringArgumentType.string().parse(reader);
    try {
      return this.get_path(input);
    } catch (FileNotFoundException e) {
      throw PATH_NOT_FOUND.createWithContext(reader);
    }
  }

  // Make sure string can be parsed as an argument string correctly
  private String format_argument(String str) {
    try {
      if (
        StringArgumentType
          .string()
          .parse(new StringReader(str))
          .compareTo(str) == 0
      ) return str;
    } catch (Exception ignore) {}
    return "\"" + str + "\"";
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
    CommandContext<S> context,
    SuggestionsBuilder builder
  ) {
    String str = ArgumentPath.parse_string(builder.getRemaining());
    try {
      this.get_suggestions(str).forEach(path -> {
        builder.suggest(this.format_argument(path));
      });
    } catch (Exception ignored) {}
    return builder.buildFuture();
  }

  @Override
  public Collection<String> getExamples() {
    return CommandArgumentPath.EXAMPLES;
  }

}
