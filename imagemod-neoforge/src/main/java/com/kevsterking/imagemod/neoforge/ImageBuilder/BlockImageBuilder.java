package com.kevsterking.imagemod.neoforge.ImageBuilder;

import com.kevsterking.imagemod.neoforge.ImagemodClient;
import com.kevsterking.imagemod.neoforge.WorldTransformer.WorldTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.function.Consumer;

public class BlockImageBuilder extends Thread {

  private final ArrayList<BlockColor> block_colors;
  private final Image image;
  private final Consumer<Exception> on_error;
  private final Consumer<WorldTransform> on_success;

  private final WorldTransform transform;

  public BlockImageBuilder(
    Image image,
    ArrayList<BlockColor> block_colors,
    Level level,
    BlockPos position,
    Direction direction_x,
    Direction direction_y,
    Consumer<Exception> on_error,
    Consumer<WorldTransform> on_success
  ) {
    this.block_colors = block_colors;
    this.image = image;
    this.on_error = on_error;
    this.on_success = on_success;
    this.transform = new WorldTransform(
      level,
      position,
      direction_x, direction_y, Direction.NORTH, // Z direction doesnt matter as we are making a 2D image
      image.width, image.height, 1
    );
  }

  private void report_error(Exception e) {
    if (this.on_error != null) {
      this.on_error.accept(e);
    }
  }

  private void report_success(WorldTransform transform) {
    this.on_success.accept(transform);
  }

  public void run() {
    ImagemodClient.LOGGER.info("Running command");
    BlockImageBuilderWorker[] workers = new BlockImageBuilderWorker[this.image.height];
    // Create workers on each row of the image
    for (int y = 0; y < this.image.height; y++) {
      workers[y] = new BlockImageBuilderWorker(
        this.transform,
        this.image,
        this.block_colors,
        y
      );
      workers[y].run();
    }
    // Join all workers
//    for (int y = 0; y < this.image.height; y++) {
//      try {
//        workers[y].join();
//      } catch (InterruptedException e) {
//        this.report_error(e);
//      }
//    }
    // Report back
    ImagemodClient.LOGGER.info("Placing");
    this.report_success(this.transform);
    ImagemodClient.LOGGER.info("Placing done");
  }
}