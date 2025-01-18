package Mosaic.MosaicShort;

import Mosaic.ImageUtil;
import Mosaic.MosaicGenerator;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MosaicShort extends MosaicGenerator<short[], BufferedImage, BufferedImage, BufferedImage> {

  protected BufferedImage get_empty_result(final int tile_cols, final int tile_rows) {
    return new BufferedImage(tile_cols * 16, tile_rows * 16, BufferedImage.TYPE_4BYTE_ABGR);
  }

  protected short[][] get_empty_type(int size) {
    return new short[size][this.tile_size*this.tile_size*4];
  }

  protected void set_result(BufferedImage res, BufferedImage tile, final int x, final int y) {
    Graphics g = res.getGraphics();
    g.drawImage(tile, x * 16, y * 16, 16, 16, null);
    g.dispose();
  }

  protected void generate_tiles(BufferedImage result, final int tile_cols, final int tile_rows) {
    for (int y = 0; y < tile_rows; y++) {
      for (int x = 0; x < tile_cols; x++) {
        this.generate_at(result, x, y);
      }
    }
  }

  protected long compare(short[] tile, short[] image) {
    long ret = 0;
    for (int i = 0; i < tile.length; i+=4) {
      double colorFactor = ((double) (tile[i] + image[i])) / (2*255);
      int dr = Math.abs(tile[i+3] - image[i+3]);
      int dg = Math.abs(tile[i+2] - image[i+2]);
      int db = Math.abs(tile[i+1] - image[i+1]);
      int dalpha = Math.abs(tile[i] - image[i] * 3);
      ret += (long) ((1.0 - colorFactor) * dalpha + colorFactor * (db + dg + dr));
    }
    return ret;
  }

  public static short[] get_tile_colors(int[] in) {
    short[] ret = new short[4*in.length];
    for (int p = 0; p < in.length; p++) {
      Color c = new Color(in[p], true);
      ret[4*p] = (short) c.getAlpha();
      ret[4*p+1] = (short) c.getBlue();
      ret[4*p+2] = (short) c.getGreen();
      ret[4*p+3] = (short) c.getRed();
    }
    return ret;
  }

  protected short[][][] get_source_type(BufferedImage src, final int tile_cols, final int tile_rows) {
    BufferedImage img = ImageUtil.load(src, tile_cols * this.tile_size, tile_rows * this.tile_size);
    short[][][] ret = new short[tile_cols][tile_rows][this.tile_size*this.tile_size*4];
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
        ret[x][y] = MosaicShort.get_tile_colors(tmp);
      }
    }
    return ret;
  }

  protected short[] get_tile_type(BufferedImage tile, final int tile_size) {
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
    return MosaicShort.get_tile_colors(ret);
  }

}
