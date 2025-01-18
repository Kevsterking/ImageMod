package Mosaic.MosaicInt;

import Mosaic.ImageUtil;
import Mosaic.MosaicGenerator;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MosaicGeneratorInt extends MosaicGenerator<int[], BufferedImage, BufferedImage, BufferedImage> {

  protected BufferedImage get_empty_result(final int tile_cols, final int tile_rows) {
    return new BufferedImage(tile_cols * this.tile_size, tile_rows * this.tile_size, BufferedImage.TYPE_4BYTE_ABGR);
  }

  protected int[][] get_empty_type(int size) {
    return new int[size][this.tile_size*this.tile_size*4];
  }

  protected void set_result(BufferedImage res, BufferedImage tile, final int x, final int y) {
    Graphics g = res.getGraphics();
    g.drawImage(tile, x * this.tile_size, y * this.tile_size, this.tile_size, this.tile_size, null);
    g.dispose();
  }

  public static double[] rgb_to_lab(int r, int g, int b) {
    double[] xyz = rgb_to_xyz(r, g, b);
    return xyz_to_lab(xyz);
  }

  private static double[] rgb_to_xyz(int r, int g, int b) {
    double var_R = r / 255.0;
    double var_G = g / 255.0;
    double var_B = b / 255.0;
    var_R = (var_R > 0.04045) ? Math.pow((var_R + 0.055) / 1.055, 2.4) : var_R / 12.92;
    var_G = (var_G > 0.04045) ? Math.pow((var_G + 0.055) / 1.055, 2.4) : var_G / 12.92;
    var_B = (var_B > 0.04045) ? Math.pow((var_B + 0.055) / 1.055, 2.4) : var_B / 12.92;
    double X = var_R * 0.4124 + var_G * 0.3576 + var_B * 0.1805;
    double Y = var_R * 0.2126 + var_G * 0.7152 + var_B * 0.0722;
    double Z = var_R * 0.0193 + var_G * 0.1192 + var_B * 0.9505;
    return new double[]{X, Y, Z};
  }

  public static double delta_e(int r1, int g1, int b1, int a1, int r2, int g2, int b2, int a2) {
    double[] lab1 = rgb_to_lab(r1, g1, b1);
    double[] lab2 = rgb_to_lab(r2, g2, b2);
    double deltaL = lab1[0] - lab2[0];
    double deltaA = lab1[1] - lab2[1];
    double deltaB = lab1[2] - lab2[2];
    // Simplified Delta E calculation
    double deltaEColor = Math.sqrt(deltaL * deltaL + deltaA * deltaA + deltaB * deltaB);
    // Alpha difference calculation
    double deltaAlpha = Math.abs(a1 - a2) / 255.0; // Normalize alpha to [0, 1]
    // Blend Delta E with alpha difference. Here, we weight color difference with average transparency.
    double weight = ((double)Math.min(a1, a2)) / 255.0; // Using min for emphasis on least transparency
    return (deltaEColor * weight) + (deltaAlpha * (1 - weight));
  }

  private static double[] xyz_to_lab(double[] xyz) {
    double x = xyz[0] / 95.047; // Observer= 2°, Illuminant= D65
    double y = xyz[1] / 100.000;
    double z = xyz[2] / 108.883;
    x = (x > 0.008856) ? Math.cbrt(x) : (7.787 * x + 16.0 / 116.0);
    y = (y > 0.008856) ? Math.cbrt(y) : (7.787 * y + 16.0 / 116.0);
    z = (z > 0.008856) ? Math.cbrt(z) : (7.787 * z + 16.0 / 116.0);
    double L = (116 * y) - 16;
    double a = 500 * (x - y);
    double b = 200 * (y - z);
    return new double[]{L, a, b};
  }

  protected long compare(int[] tile, int[] image) {
    long ret = 0;
    for (int i = 0; i < tile.length; i+=4) {
//      double colorFactor = ((double) (tile[i] + image[i]) / (2*255));
//      int dr     = Math.abs(tile[i+3] - image[i+3]);
//      int dg     = Math.abs(tile[i+2] - image[i+2]);
//      int db     = Math.abs(tile[i+1] - image[i+1]);
//      int dalpha = Math.abs(tile[i] - image[i]) * 3;
//      long cs = (long) ((1.0 - colorFactor) * dalpha + colorFactor * (db + dg + dr));
//      System.out.printf(
//        "%d %d %d %d - %d %d %d %d - %d\n",
//        tile[i+3], tile[i+2], tile[i+1], tile[i],
//        image[i+3], image[i+2], image[i+1], image[i],
//        cs
//      );
      ret += (long) delta_e(tile[i], tile[i+1], tile[i+2], tile[i+3], image[i], image[i+1], image[i+2], image[i+3]);
    }
    return ret;
  }

  private static int[] get_tile_colors(int[] in) {
    int[] ret = new int[4*in.length];
    for (int p = 0; p < in.length; p++) {
      Color c = new Color(in[p]);
      ret[4*p] = c.getAlpha();
      ret[4*p+1] = c.getBlue();
      ret[4*p+2] = c.getGreen();
      ret[4*p+3] = c.getRed();
    }
    return ret;
  }

  protected int[][][] get_source_type(BufferedImage src, final int tile_cols, final int tile_rows) {
    BufferedImage img = ImageUtil.load(src, tile_cols * this.tile_size, tile_rows * this.tile_size);
    int[][][] ret = new int[tile_cols][tile_rows][this.tile_size*this.tile_size*4];
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
        ret[x][y] = MosaicGeneratorInt.get_tile_colors(tmp);
      }
    }
    return ret;
  }

  protected int[] get_tile_type(BufferedImage tile, final int tile_size) {
    int[] ret = new int[this.tile_size*this.tile_size];
    tile.getRGB(
      0,
      0,
      this.tile_size,
      this.tile_size,
      ret,
      0,
      this.tile_size
    );
    return MosaicGeneratorInt.get_tile_colors(ret);
  }

}
