package com.kevsterking.imagemod.core.mosaic;

import java.util.function.Function;

public class MosaicGeneratorWorker<TYPE, SRC, TILE, RES> extends Thread {

  private final MosaicGenerator<TYPE, SRC, TILE, RES> generator;
  private int tile_rows, tile_cols;
  private SRC src;
  private Function<RES, Void> callback;

  protected MosaicGeneratorWorker(MosaicGenerator<TYPE, SRC, TILE, RES> generator) {
    this.generator = generator;
  }

  public void run() {
    callback.apply(this.generator.generate(src, tile_rows, tile_cols));
  }

  public void generate(
    SRC src,
    final int tile_rows, final int tile_cols,
    Function<RES,Void> callback
  ) {
    this.src = src;
    this.tile_cols = tile_cols;
    this.tile_rows = tile_rows;
    this.callback = callback;
    this.start();
  }

}
