package com.kevsterking.imagemod.neoforge.ImageBuilder.Mosaic;


import com.kevsterking.imagemod.neoforge.WorldTransformer.WorldStructure;

public class MosaicIntColThreadWorker extends Thread {

  private WorldStructure res;
  private MosaicIntColThread operation;
  private int cols, row;

  public void run() {
    for (int i = 0; i < this.cols; i++) {
      this.operation.generate_at(this.res, i, this.row);
    }
  }

  public void start_task(WorldStructure res, MosaicIntColThread operation, int cols, int row) {
    this.res = res;
    this.operation = operation;
    this.cols = cols;
    this.row = row;
    this.start();
  }

}
