package com.kevsterking.imagemod.core.commands;

import com.kevsterking.imagemod.core.util.ImageBlock;
import com.kevsterking.imagemod.core.ImageModCore;
import com.kevsterking.imagemod.core.ImageModCommandFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

public class ImageReloadFunction<C, E extends Exception> implements ImageModCommandFunction<C, E> {

  private final ImageCommandContext<C, E> cc;

  public ImageReloadFunction(ImageCommandContext<C, E> cc) {
    this.cc = cc;
  }

  // Update block list
  public void update_block_list() {
    ImageModCore.LOGGER.info("Loading block list");
    for (Block block : BuiltInRegistries.BLOCK) {
      try {
        if (ImageBlock.filter_block(block)) {
          cc.image_blocks.add(ImageBlock.get(block));
          ImageModCore.LOGGER.debug("{} - ACCEPTED", block.getName().getString());
        }
      } catch (Exception e) {
        ImageModCore.LOGGER.debug("{} - REJECTED: {}", block.getName().getString(), e.getMessage());
      }
    }
    cc.image_blocks.add(ImageBlock.get_air());
    ImageBlock[] blocks = new ImageBlock[cc.image_blocks.size()];
    for (int i = 0; i < cc.image_blocks.size(); i++) {
      blocks[i] = cc.image_blocks.get(i);
    }
    cc.image_builder.set_tiles(blocks, 16);
    ImageModCore.LOGGER.info("Block list loading complete.");
  }

  @Override
  public int execute(C ctx) throws E {
    this.update_block_list();
    cc.mod_interface.send_feedback(ctx, "Reloaded block list");
    return 1;
  }
}
