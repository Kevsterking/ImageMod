package com.kevsterking.imagemod.core;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.function.Function;

public interface ImageModCommandInterface<C, E> {
  Entity get_entity(C ctx);
  Level get_level(C ctx);
  void send_error(C ctx, String message);
  void send_feedback(C ctx, String message);
  <A> A get_arg(C ctx, String name, Class<A> argtype);
  ImageModCommandException<E> exception(Function<Object, String> gen_exception);
}
