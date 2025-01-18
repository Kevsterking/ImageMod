package Mosaic.MosaicHistogram;

import Mosaic.ImageUtil;
import Mosaic.IntColorVector;
import Mosaic.MosaicGenerator;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MosaicHisto extends MosaicGenerator<ColorHistogram, BufferedImage, BufferedImage, BufferedImage> {

  protected BufferedImage get_empty_result(final int tile_cols, final int tile_rows) {
    return new BufferedImage(tile_cols * 16, tile_rows * 16, BufferedImage.TYPE_4BYTE_ABGR);
  }

  protected ColorHistogram[] get_empty_type(int size) {
    return new ColorHistogram[size];
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

  protected long compare(ColorHistogram tile, ColorHistogram image) {
    double ret = 0;
    for (byte a = 0; a < tile.c.length; a++) {
      for (byte b = 0; b < tile.c.length; b++) {
        for (byte g = 0; g < tile.c.length; g++) {
          for (byte r = 0; r < tile.c.length; r++) {
            byte d = (byte) Math.min(tile.c[a][b][g][r], image.c[a][b][g][r]);
            ret += d * d;
          }
        }
      }
    }
    return (long) (1000000000f / ret);
  }

  public static ColorHistogram get_tile_colors(int[] in) {
    byte bins = 6;
    ColorHistogram ret = new ColorHistogram();
    ret.c = new byte[bins][bins][bins][bins];
    for (int j : in) {
      Color c = new Color(j, true);
      byte ab = (byte) (c.getAlpha() * bins / 256);
      byte bb = (byte) (c.getAlpha() * bins / 256);
      byte gb = (byte) (c.getAlpha() * bins / 256);
      byte rb = (byte) (c.getAlpha() * bins / 256);
      ret.c[ab][bb][gb][rb]++;
    }
    return ret;
  }

  protected ColorHistogram[][] get_source_type(BufferedImage src, final int tile_cols, final int tile_rows) {
    BufferedImage img = ImageUtil.load(src, tile_cols * this.tile_size, tile_rows * this.tile_size);
    ColorHistogram[][] ret = new ColorHistogram[tile_cols][tile_rows];
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
        ret[x][y] = MosaicHisto.get_tile_colors(tmp);
      }
    }
    return ret;
  }

  protected ColorHistogram get_tile_type(BufferedImage tile, final int tile_size) {
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
    return MosaicHisto.get_tile_colors(ret);
  }

}
