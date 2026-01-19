package com.kevsterking.imagemod.core.mosaic;

import com.kevsterking.imagemod.core.transform.WorldStructure;

public class MosaicIntColThreadWorker extends Thread {

  private WorldStructure res;
  private com.kevsterking.imagemod.core.mosaic.MosaicIntColThread operation;
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
