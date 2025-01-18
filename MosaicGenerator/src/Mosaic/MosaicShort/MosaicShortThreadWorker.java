package Mosaic.MosaicShort;

import java.awt.image.BufferedImage;

public class MosaicShortThreadWorker extends Thread {

  private BufferedImage res;
  private MosaicShortThread operation;
  private int cols, row;

  public void run() {
    for (int i = 0; i < this.cols; i++) {
      this.operation.generate_at(this.res, i, this.row);
    }
  }

  public void start_task(BufferedImage res, MosaicShortThread operation, int cols, int row) {
    this.res = res;
    this.operation = operation;
    this.cols = cols;
    this.row = row;
    this.start();
  }

}
