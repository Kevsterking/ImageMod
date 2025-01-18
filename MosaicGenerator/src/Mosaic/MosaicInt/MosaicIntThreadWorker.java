package Mosaic.MosaicInt;

import java.awt.image.BufferedImage;

public class MosaicIntThreadWorker extends Thread {

  private BufferedImage res;
  private MosaicIntThread operation;
  private int cols, row;

  public void run() {
    for (int i = 0; i < this.cols; i++) {
      this.operation.generate_at(this.res, i, this.row);
    }
  }

  public void start_task(BufferedImage res, MosaicIntThread operation, int cols, int row) {
    this.res = res;
    this.operation = operation;
    this.cols = cols;
    this.row = row;
    this.start();
  }

}
