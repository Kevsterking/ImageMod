package com.kevsterking.imagemod.fabric;

import com.kevsterking.imagemod.core.ImageModInterface;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ImageModFabricImplementation implements ImageModInterface<@NotNull FabricClientCommandSource> {

  @Override
  public Entity ctx_get_entity(FabricClientCommandSource ctx) {
    return ctx.getEntity();
  }

  @Override
  public Level ctx_get_level(FabricClientCommandSource ctx) {
    return ctx.getWorld();
  }

  @Override
  public void ctx_send_error(FabricClientCommandSource ctx, Component message) {
    ctx.sendError(message);
  }

  @Override
  public void ctx_send_feedback(FabricClientCommandSource ctx, Component message) {
    ctx.sendFeedback(message);
  }

  @Override
  public LiteralArgumentBuilder<FabricClientCommandSource> command_literal(String name) {
    return ClientCommandManager.literal(name);
  }

  @Override
  public <A> RequiredArgumentBuilder<FabricClientCommandSource, A> command_argument(String name, ArgumentType<A> arg) {
    return ClientCommandManager.argument(name, arg);
  }
}
