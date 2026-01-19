package com.kevsterking.imagemod.neoforge;

import com.kevsterking.imagemod.core.ImageModCommandInterface;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ImageModNeoForgeImplementation implements ImageModCommandInterface<@NotNull CommandSourceStack> {

  @Override
  public Entity get_entity(CommandSourceStack ctx) {
    return ctx.getEntity();
  }

  @Override
  public Level get_level(CommandSourceStack ctx) {
    return ctx.getLevel();
  }

  @Override
  public void send_error(CommandSourceStack ctx, String message) {
    ctx.sendFailure(Component.literal(message));
  }

  @Override
  public void send_feedback(CommandSourceStack ctx, String message) {
    ctx.sendSuccess(() -> Component.literal(message), false);
  }

  @Override
  public LiteralArgumentBuilder<CommandSourceStack> command_literal(String str) {
    return Commands.literal(str);
  }

  @Override
  public <A> RequiredArgumentBuilder<CommandSourceStack, A> command_argument(String name, ArgumentType<A> arg) {
    return Commands.argument(name, arg);
  }

  @Override
  public DynamicCommandExceptionType exception(Function<Object, String> gen_exception) {
    return new DynamicCommandExceptionType(str -> Component.literal(gen_exception.apply(str)));
  }

}
