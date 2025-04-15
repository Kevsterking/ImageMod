package com.kevsterking.imagemod.neoforge.WorldTransformer;

import net.minecraft.world.level.block.state.BlockState;

public class WorldStructure {
  public final BlockState[][][] structure;
  public final int width, height, depth;
  public WorldStructure(final int width, final int height, final int depth) {
    this.structure = new BlockState[width][height][depth];
    this.width = width;
    this.height = height;
    this.depth = depth;
  }
}
