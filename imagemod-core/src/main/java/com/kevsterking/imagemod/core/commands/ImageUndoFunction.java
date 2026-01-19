package com.kevsterking.imagemod.core.commands;

import com.kevsterking.imagemod.core.ImageModCommandFunction;

public class ImageUndoFunction<C, E extends Exception> implements ImageModCommandFunction<C, E> {

  private final ImageCommandContext<C, E> cc;

  public ImageUndoFunction(ImageCommandContext<C, E> cc) {
    this.cc = cc;
  }

  @Override
  public int execute(C ctx) throws E {
    cc.world_transformer.undo_async((Exception e) -> {
      if (e != null) {
        cc.mod_interface.send_error(ctx, "Failed: " + e.getMessage());
        return null;
      }
      cc.mod_interface.send_feedback(ctx, "Successfully reverted last WorldTransform");
      return null;
    });
    return 1;
  }
}
