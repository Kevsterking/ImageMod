package com.kevsterking.imagemod.fabric;

import com.kevsterking.imagemod.core.ImageModCommandInterface;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ImageModCommandInterfaceFabric implements ImageModCommandInterface<FabricClientCommandSource> {

  @Override
  public Entity get_entity(FabricClientCommandSource ctx) {
    CommandContext<FabricClientCommandSource> aaa;
    return ctx.getEntity();
    ClientCommandManager.argument().executes()
  }

  @Override
  public Level get_level(FabricClientCommandSource ctx) {
    return ctx.getWorld();
  }

  @Override
  public void send_error(@NotNull FabricClientCommandSource ctx, String message) {
    ctx.sendError(Component.literal(message));
  }

  @Override
  public void send_feedback(@NotNull FabricClientCommandSource ctx, String message) {
    ctx.sendFeedback(Component.literal(message));
  }

  @Override
  public LiteralArgumentBuilder<FabricClientCommandSource> command_literal(String name) {
    return ClientCommandManager.literal(name);
  }

  @Override
  public <A> RequiredArgumentBuilder<FabricClientCommandSource, A> command_argument(String name, ArgumentType<A> arg) {
    return ClientCommandManager.argument(name, arg);
  }

  @Override
  public DynamicCommandExceptionType exception(Function<Object, String> gen_exception) {
    return new DynamicCommandExceptionType(str -> Component.literal(gen_exception.apply(str)));
  }

}
