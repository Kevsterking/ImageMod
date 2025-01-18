package com.kevsterking.imagemod.neoforge.WorldTransformer;

import net.minecraft.world.level.block.state.BlockState;

public class WorldStructure {
  public final BlockState[][][] structure;
  public WorldStructure(final int width, final int height, final int depth) {
    this.structure = new BlockState[width][height][depth];
  }
}
