package com.kevsterking.imagemod.neoforge.ImageBuilder;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BlockColor {

  public BlockState block_state;
  public Color color;

  public static final BlockColor AIR = new BlockColor(Blocks.AIR.defaultBlockState(), new Color(0,0,0,0));

  public BlockColor(BlockState block_state, Color color) {
    this.block_state = block_state;
    this.color = color;
  }

}