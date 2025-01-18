package Mosaic.MosaicSIMD;

import Mosaic.ImageUtil;
import Mosaic.MosaicGenerator;
import jdk.incubator.vector.*;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MosaicSIMD extends MosaicGenerator<ByteVector, BufferedImage, BufferedImage, BufferedImage> {

  protected BufferedImage get_empty_result(final int tile_cols, final int tile_rows) {
    return new BufferedImage(tile_cols * this.tile_size, tile_rows * this.tile_size, BufferedImage.TYPE_4BYTE_ABGR);
  }

  protected ByteVector[] get_empty_type(int size) {
    return new ByteVector[size];
  }

  protected void set_result(BufferedImage res, BufferedImage tile, final int x, final int y) {
    Graphics g = res.getGraphics();
    g.drawImage(tile, x * this.tile_size, y * this.tile_size, this.tile_size, this.tile_size, null);
    g.dispose();
  }

  protected long compare(ByteVector tile, ByteVector image) {

    long ret = 0;

    tile.


  }

  private static byte[] get_tile_colors(int[] in) {
    byte[] ret = new byte[4*in.length];
    for (int p = 0; p < in.length; p++) {
      Color c = new Color(in[p]);
      ret[4*p] = (byte) c.getAlpha();
      ret[4*p+1] = (byte) c.getBlue();
      ret[4*p+2] = (byte) c.getGreen();
      ret[4*p+3] = (byte) c.getRed();
    }
    return ret;
  }

  protected ByteVector[][] get_source_type(BufferedImage src, final int tile_cols, final int tile_rows) {
    BufferedImage img = ImageUtil.load(src, tile_cols * this.tile_size, tile_rows * this.tile_size);
    ByteVector[][] ret = new ByteVector[tile_cols][tile_rows];
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
        byte[] barr = MosaicSIMD.get_tile_colors(tmp);
        ret[x][y] = ByteVector.fromArray(ByteVector.SPECIES_PREFERRED, MosaicSIMD.get_tile_colors(tmp), 0);
      }
    }
    return ret;
  }

  protected ByteVector get_tile_type(BufferedImage tile, final int tile_size) {
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
    return ByteVector.fromArray(ByteVector.SPECIES_PREFERRED, MosaicSIMD.get_tile_colors(ret), 0);
  }

}
