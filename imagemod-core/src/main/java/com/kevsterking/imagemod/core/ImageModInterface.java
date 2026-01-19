package com.kevsterking.imagemod.core;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public interface ImageModInterface<T extends SharedSuggestionProvider> {
  Entity ctx_get_entity(T ctx);
  Level ctx_get_level(T ctx);
  void ctx_send_error(T ctx, Component message);
  void ctx_send_feedback(T ctx, Component message);
  LiteralArgumentBuilder<T> command_literal(String str);
  <A> RequiredArgumentBuilder<T, A> command_argument(String name, ArgumentType<A> arg);
}
