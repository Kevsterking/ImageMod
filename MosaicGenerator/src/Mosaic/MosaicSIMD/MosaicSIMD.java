package Mosaic.MosaicSIMD;

import Mosaic.ImageUtil;
import Mosaic.MosaicGenerator;
import jdk.incubator.vector.*;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MosaicSIMD extends MosaicGenerator<SIMDColorVector, BufferedImage, BufferedImage, BufferedImage> {

  protected BufferedImage get_empty_result(final int tile_cols, final int tile_rows) {
    return new BufferedImage(tile_cols * 16, tile_rows * 16, BufferedImage.TYPE_4BYTE_ABGR);
  }

  protected SIMDColorVector[] get_empty_type(int size) {
    return new SIMDColorVector[size];
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

  private static FloatVector ONES = FloatVector.broadcast(FloatVector.SPECIES_MAX, 1);

  protected long compare(SIMDColorVector tile, SIMDColorVector image) {
    float ret = 0;
    float mval = 1.0f / 510.0f;
    for (int i = 0; i < tile.a.length; i++) {
      FloatVector cf = tile.a[i].add(image.a[i]).mul(mval);
      FloatVector da = image.a[i].sub(tile.a[i]).abs().mul((short)3);
      FloatVector db = image.b[i].sub(tile.b[i]).abs();
      FloatVector dg = image.g[i].sub(tile.g[i]).abs();
      FloatVector dr = image.r[i].sub(tile.r[i]).abs();
      ret += ONES.sub(cf).mul(da).add(cf.mul(db.add(dg).add(dr))).reduceLanes(VectorOperators.ADD);
    }
    return (long) ret;
  }

  public static SIMDColorVector get_tile_colors(int[] in) {
    SIMDColorVector ret = new SIMDColorVector();
    VectorSpecies<Float> species = FloatVector.SPECIES_MAX;
    final int spec_len = species.length();
    float[] a = new float[in.length];
    float[] b = new float[in.length];
    float[] g = new float[in.length];
    float[] r = new float[in.length];
    ret.a = new FloatVector[in.length / spec_len];
    ret.b = new FloatVector[in.length / spec_len];
    ret.g = new FloatVector[in.length / spec_len];
    ret.r = new FloatVector[in.length / spec_len];
    for (int p = 0; p < in.length; p++) {
      Color c = new Color(in[p], true);
      a[p] = (float) c.getAlpha();
      b[p] = (float) c.getBlue();
      g[p] = (float) c.getGreen();
      r[p] = (float) c.getRed();
    }
    for (int i = 0; i * spec_len < a.length; i++) {
      ret.a[i] = FloatVector.fromArray(species, a, i);
      ret.b[i] = FloatVector.fromArray(species, b, i);
      ret.g[i] = FloatVector.fromArray(species, g, i);
      ret.r[i] = FloatVector.fromArray(species, r, i);
    }
    return ret;
  }

  protected SIMDColorVector[][] get_source_type(BufferedImage src, final int tile_cols, final int tile_rows) {
    BufferedImage img = ImageUtil.load(src, tile_cols * this.tile_size, tile_rows * this.tile_size);
    SIMDColorVector[][] ret = new SIMDColorVector[tile_cols][tile_rows];
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
        ret[x][y] = MosaicSIMD.get_tile_colors(tmp);
      }
    }
    return ret;
  }

  protected SIMDColorVector get_tile_type(BufferedImage tile, final int tile_size) {
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
    return MosaicSIMD.get_tile_colors(ret);
  }

}
