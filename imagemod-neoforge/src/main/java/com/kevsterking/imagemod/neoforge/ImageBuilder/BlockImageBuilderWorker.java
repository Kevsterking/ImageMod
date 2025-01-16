package com.kevsterking.imagemod.neoforge.ImageBuilder;

import com.kevsterking.imagemod.neoforge.ImagemodClient;
import com.kevsterking.imagemod.neoforge.WorldTransformer.WorldTransform;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;

public class BlockImageBuilderWorker extends Thread {

  private final WorldTransform transform;
  private final Image image;
  private final ArrayList<BlockColor> block_colors;
  private final int task_row;

  // Create threaded worker
  public BlockImageBuilderWorker(
    WorldTransform transform,
    Image image,
    ArrayList<BlockColor> block_colors,
    final int task_row
  ) {
    this.transform = transform;
    this.image = image;
    this.block_colors = block_colors;
    this.task_row = task_row;
  }

  public BlockColor get_best_block(Color color) {
    // Set record to be air by default
    BlockColor ret = BlockColor.AIR;
    int record = Color.similarity(ret.color, color);
    // Go through list of blocks and try to find better fit
    for (BlockColor block_color : this.block_colors) {
      int diff = Color.similarity(block_color.color, color);
      if (diff < record) {
        record = diff;
        ret = block_color;
      }
    }
    return ret;
  }

  public void run() {
    for (int x = 0; x < image.width; x++) {
      Color pixel_color = image.get_color(x, this.task_row);
      BlockColor best_fit = this.get_best_block(pixel_color);
      this.transform.structure[x][image.height-1-this.task_row][0] = best_fit.block_state;
    }
  }

}
