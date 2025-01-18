package com.kevsterking.imagemod.neoforge.ImageBuilder.Mosaic;

public abstract class MosaicGenerator<TYPE, SRC, TILE, RES> {

  protected TILE[] tiles;
  protected TYPE[] tile_type;
  protected TYPE[][] source_type;
  protected int tile_size, tile_cols, tile_rows;

  abstract protected RES get_empty_result(final int tile_cols, final int tile_rows);
  abstract protected TYPE[] get_empty_type(final int size);
  abstract protected long compare(TYPE tile, TYPE image);
  abstract protected TYPE[][] get_source_type(SRC src, final int width, final int height);
  abstract protected TYPE get_tile_type(TILE tile, final int tile_size);
  abstract protected void set_result(RES res, TILE tile, final int x, final int y);
  abstract protected void generate_tiles(RES res, final int tile_rows, final int tile_cols);

  public void set_tiles(TILE[] tiles, final int tile_size) {
    this.tiles = tiles;
    this.tile_size = tile_size;
    this.tile_type = this.get_empty_type(tiles.length);
    for (int i = 0; i < tiles.length; i++) {
      this.tile_type[i] = this.get_tile_type(tiles[i], tile_size);
    }
  }

  public void generate_at(RES res, final int tile_x, final int tile_y) {
    this.set_result(res, this.find_best(tile_x, tile_y), tile_x, tile_y);
  }

  public RES generate(SRC src, final int tile_cols, final int tile_rows) {
    RES ret = this.get_empty_result(tile_cols, tile_rows);
    this.source_type = this.get_source_type(src, tile_cols, tile_rows);
    this.generate_tiles(ret, tile_cols, tile_rows);
    return ret;
  }

  private TILE find_best(final int tile_x, final int tile_y) {
    TILE record_tile = null;
    long record = Long.MAX_VALUE;
    for (int i = 0; i < tiles.length; i++) {
      long score = this.compare(this.tile_type[i], this.source_type[tile_x][tile_y]);
      if (score < record) {
        record_tile = this.tiles[i];
        record = score;
      }
    }
    return record_tile;
  }

}
