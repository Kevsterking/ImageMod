package com.kevsterking.imagemod.core;

public interface ImageModCommandFunction<C, E extends Exception> {
  int execute(C ctx) throws E;
}
