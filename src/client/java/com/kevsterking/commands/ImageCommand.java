package com.kevsterking.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageCommand {

    public static final String MOD_ID = "imagemod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static int create_execute(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
        ctx.getSource().sendFeedback(Text.literal("UNIMPLEMENTED CREATE"));
        return 1;
    }

    private static int reload_execute(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
        ctx.getSource().sendFeedback(Text.literal("UNIMPLEMENTED RELOAD"));
        return 1;
    }

    private static int undo_execute(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
        ctx.getSource().sendFeedback(Text.literal("UNIMPLEMENTED UNDO"));
        return 1;
    }

    private static int redo_execute(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
        ctx.getSource().sendFeedback(Text.literal("UNIMPLEMENTED REDO"));
        return 1;
    }

    private static int set_directory_execute(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
        ctx.getSource().sendFeedback(Text.literal("UNIMPLEMENTED SETDIRECTORY"));
        return 1;
    }

    public static void register(
            CommandDispatcher<FabricClientCommandSource> dispatcher,
            CommandRegistryAccess access)
    {
        LOGGER.info("Registering Commands");

        // image command
        LiteralArgumentBuilder<FabricClientCommandSource> root = ClientCommandManager.literal("image");

        // create option
        LiteralArgumentBuilder<FabricClientCommandSource> createLiteral = ClientCommandManager.literal("create");

        LiteralArgumentBuilder<FabricClientCommandSource> wLiteral  	= ClientCommandManager.literal("-width");
        LiteralArgumentBuilder<FabricClientCommandSource> hLiteral  	= ClientCommandManager.literal("-height");
        LiteralArgumentBuilder<FabricClientCommandSource> whInfoLiteral = ClientCommandManager.literal("~ ~");

        RequiredArgumentBuilder<FabricClientCommandSource, Integer> srcArgument = ClientCommandManager.argument("src", IntegerArgumentType.integer());

        RequiredArgumentBuilder<FabricClientCommandSource, Integer> wArgument      = ClientCommandManager.argument("width", IntegerArgumentType.integer());
        RequiredArgumentBuilder<FabricClientCommandSource, Integer> wArgumentFinal = ClientCommandManager.argument("width", IntegerArgumentType.integer()).executes(ImageCommand::create_execute);
        RequiredArgumentBuilder<FabricClientCommandSource, Integer> hArgumentFinal = ClientCommandManager.argument("height", IntegerArgumentType.integer()).executes(ImageCommand::create_execute);

        wLiteral.then(wArgumentFinal);
        hLiteral.then(hArgumentFinal);
        wArgument.then(hArgumentFinal);

        srcArgument.then(wLiteral).then(hLiteral).then(whInfoLiteral).then(wArgument);
        createLiteral.then(srcArgument);

        // Reload option
        LiteralArgumentBuilder<FabricClientCommandSource> reload = ClientCommandManager.literal("reload").executes(ImageCommand::reload_execute);

        // Undo
        LiteralArgumentBuilder<FabricClientCommandSource> undoLiteral = ClientCommandManager.literal("undo").executes(ImageCommand::undo_execute);

        // Redo
        LiteralArgumentBuilder<FabricClientCommandSource> redoLiteral = ClientCommandManager.literal("redo").executes(ImageCommand::redo_execute);;

        // SetDirectory
        LiteralArgumentBuilder<FabricClientCommandSource> setDirectoryLiteral = ClientCommandManager.literal("setDirectory");
        RequiredArgumentBuilder<FabricClientCommandSource, Integer> directoryArgument  = ClientCommandManager.argument("dir", IntegerArgumentType.integer()).executes(ImageCommand::set_directory_execute);

        setDirectoryLiteral.then(directoryArgument);

        // Root relations
        root.then(createLiteral).then(setDirectoryLiteral).then(reload).then(undoLiteral).then(redoLiteral);
        dispatcher.register(root);

        LOGGER.info("Commands Registered");
    }

}
