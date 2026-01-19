package com.kevsterking.imagemod.neoforge;

import com.kevsterking.imagemod.core.ImageModInterface;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ImageModNeoForgeImplementation implements ImageModInterface<CommandSourceStack> {

  @Override
  public Entity ctx_get_entity(CommandSourceStack ctx) {
    return ctx.getEntity();
  }

  @Override
  public Level ctx_get_level(CommandSourceStack ctx) {
    return ctx.getLevel();
  }

  @Override
  public void ctx_send_error(CommandSourceStack ctx, Component message) {
    ctx.sendFailure(message);
  }

  @Override
  public void ctx_send_feedback(CommandSourceStack ctx, Component message) {
    ctx.sendSuccess(() -> message, false);
  }

  @Override
  public LiteralArgumentBuilder<CommandSourceStack> command_literal(String str) {
    return Commands.literal(str);
  }

  @Override
  public <A> RequiredArgumentBuilder<CommandSourceStack, A> command_argument(String name, ArgumentType<A> arg) {
    return Commands.argument(name, arg);
  }

}
