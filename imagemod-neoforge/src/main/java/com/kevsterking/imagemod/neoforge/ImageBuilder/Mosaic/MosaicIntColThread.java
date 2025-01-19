package com.kevsterking.imagemod.neoforge.ImageBuilder.Mosaic;

import com.kevsterking.imagemod.neoforge.ImageBuilder.ImageBlock;
import com.kevsterking.imagemod.neoforge.ImageBuilder.ImageUtil;
import com.kevsterking.imagemod.neoforge.ImageMod;
import com.kevsterking.imagemod.neoforge.WorldTransformer.WorldStructure;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MosaicIntColThread extends MosaicGenerator<IntColorVector, BufferedImage, ImageBlock, WorldStructure> {

  protected WorldStructure get_empty_result(final int tile_cols, final int tile_rows) {
    return new WorldStructure(tile_cols, tile_rows, 1);
  }

  protected IntColorVector[] get_empty_type(int size) {
    return new IntColorVector[size];
  }

  protected void set_result(WorldStructure res, ImageBlock image_block, final int x, final int y) {
    res.structure[x][this.tile_rows-y-1][0] = image_block.state;
  }

  protected void generate_tiles(WorldStructure result, final int tile_cols, final int tile_rows) {
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
        ImageMod.LOGGER.error("Failed to join threads: {}", e.getMessage());
      }
    }
  }

  protected long compare(IntColorVector tile, IntColorVector image) {
    long ret = 0;
    for (int i = 0; i < tile.a.length; i+=4) {
      int da = Math.abs(tile.a[i] - image.a[i]);
      int db = Math.abs(tile.b[i] - image.b[i]);
      int dg = Math.abs(tile.g[i] - image.g[i]);
      int dr = Math.abs(tile.r[i] - image.r[i]);
      double dc = Math.sqrt(db*db+dg*dg+dr*dr+da*da);
      ret += (long) (dc * dc);
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
        ret[x][y] = MosaicIntColThread.get_tile_colors(tmp);
      }
    }
    return ret;
  }

  protected IntColorVector get_tile_type(ImageBlock image_block, final int tile_size) {
    int[] ret = new int[tile_size*tile_size];
    ImageUtil.load(image_block.image, tile_size, tile_size).getRGB(
      0,
      0,
      this.tile_size,
      this.tile_size,
      ret,
      0,
      this.tile_size
    );
    return MosaicIntColThread.get_tile_colors(ret);
  }

}
