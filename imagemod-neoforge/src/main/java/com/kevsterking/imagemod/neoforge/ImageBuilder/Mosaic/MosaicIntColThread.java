package Mosaic.MosaicInt;

import Mosaic.IntColorVector;
import Mosaic.ImageUtil;
import Mosaic.MosaicGenerator;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MosaicIntColThread extends MosaicGenerator<IntColorVector, BufferedImage, BufferedImage, BufferedImage> {

  protected BufferedImage get_empty_result(final int tile_cols, final int tile_rows) {
    return new BufferedImage(tile_cols * 16, tile_rows * 16, BufferedImage.TYPE_4BYTE_ABGR);
  }

  protected IntColorVector[] get_empty_type(int size) {
    return new IntColorVector[size];
  }

  protected void set_result(BufferedImage res, BufferedImage tile, final int x, final int y) {
    Graphics g = res.getGraphics();
    g.drawImage(tile, x * 16, y * 16, 16, 16, null);
    g.dispose();
  }

  protected void generate_tiles(BufferedImage result, final int tile_cols, final int tile_rows) {
    MosaicIntColThreadWorker[] worker = new MosaicIntColThreadWorker[tile_rows];
    for (int y = 0; y < tile_rows; y++) {
      MosaicIntColThreadWorker w = new MosaicIntColThreadWorker();
      worker[y] = w;
      w.start_task(result, this, tile_cols, y);
    }
    for (int y = 0; y < tile_rows; y++) {
      try {
        worker[y].join();
      } catch(InterruptedException e) {
        System.out.println("Failed to join threads");
      }
    }
  }

  protected long compare(IntColorVector tile, IntColorVector image) {
    long ret = 0;
    for (int i = 0; i < tile.a.length; i+=4) {
      double cf = (double) (tile.a[i] + image.a[i]) * (0.00196078f);
      int da = Math.abs(tile.a[i] - image.a[i]);
      int db = Math.abs(tile.b[i] - image.b[i]);
      int dg = Math.abs(tile.g[i] - image.g[i]);
      int dr = Math.abs(tile.r[i] - image.r[i]);
      double dc = Math.sqrt(db*db+dg*dg+dr*dr+da*da);
      //long ds = (long) ((1.0 - cf) * da + cf * dc);
      ret += (long) dc * dc;
    }
    return ret;
  }

  public static IntColorVector get_tile_colors(int[] in) {
    IntColorVector ret = new IntColorVector();
    ret.a = new int[in.length];
    ret.b = new int[in.length];
    ret.g = new int[in.length];
    ret.r = new int[in.length];
    for (int p = 0; p < in.length; p++) {
      Color c = new Color(in[p], true);
      ret.a[p] = c.getAlpha();
      ret.b[p] = c.getBlue();
      ret.g[p] = c.getGreen();
      ret.r[p] = c.getRed();
    }
    return ret;
  }

  protected IntColorVector[][] get_source_type(BufferedImage src, final int tile_cols, final int tile_rows) {
    BufferedImage img = ImageUtil.load(src, tile_cols * this.tile_size, tile_rows * this.tile_size);
    IntColorVector[][] ret = new IntColorVector[tile_cols][tile_rows];
    int[] tmp = new int[this.tile_size*this.tile_size];
    for (int x = 0; x < tile_cols; x++) {
      for (int y = 0; y < tile_rows; y++) {
        img.getRGB(
          x * this.tile_size,
          y * this.tile_size,
          this.tile_size,
          this.tile_size,
          tmp,
          0,
          this.tile_size
        );
        ret[x][y] = MosaicIntCol.get_tile_colors(tmp);
      }
    }
    return ret;
  }

  protected IntColorVector get_tile_type(BufferedImage tile, final int tile_size) {
    int[] ret = new int[this.tile_size*this.tile_size];
    BufferedImage img = ImageUtil.load(tile, tile_size, tile_size);
    img.getRGB(
      0,
      0,
      this.tile_size,
      this.tile_size,
      ret,
      0,
      this.tile_size
    );
    return MosaicIntCol.get_tile_colors(ret);
  }

}
