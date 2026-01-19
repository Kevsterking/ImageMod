package com.kevsterking.imagemod.core.commands;

import com.kevsterking.imagemod.core.ImageModCommandFunction;
import com.kevsterking.imagemod.core.transform.WorldStructure;
import com.kevsterking.imagemod.core.transform.WorldTransformCreation;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

public class ImageCreateFunction<C, E extends Exception> implements ImageModCommandFunction<C, E> {

  private final ImageCommandContext<C, E> cc;

  public ImageCreateFunction(ImageCommandContext<C, E> cc) {
    this.cc = cc;
  }

  private BufferedImage load_image(Path path) throws E {
    try {
      BufferedImage img = ImageIO.read(path.toFile());
      if (img == null) throw new IOException("ImageIO returned null");
      return img;
    } catch (IOException e) {
      throw cc.IMAGE_LOAD_FAILED.create(e.getMessage());
    }
  }

  private Dimension get_dimension(C ctx, BufferedImage img) throws E {
    int w=0,h=0;
    try {
      w = cc.mod_interface.get_arg(ctx, "width", int.class);
    } catch(Exception ignore) {}
    try {
      h = cc.mod_interface.get_arg(ctx, "width", int.class);
    } catch(Exception ignore) {}
    if (w == 0 && h == 0) throw cc.NO_DIMENSIONS.create(null);
    if (w != 0 && h != 0) return new Dimension(w, h);
    double ratio = (double) img.getWidth() / img.getHeight();
    return (w != 0) ? new Dimension(w, (int) (w / ratio)) : new Dimension((int) (h * ratio), h);
  }

  @Override
  public int execute(C ctx) throws E {
    WorldTransformCreation creation = new WorldTransformCreation();
    if (!Minecraft.getInstance().isSingleplayer()) throw cc.NOT_SINGLEPLAYER.create(null);
    Entity entity = cc.mod_interface.get_entity(ctx);
    creation.level = cc.mod_interface.get_level(ctx);
    creation.direction_z = entity.getDirection();
    creation.direction_x = creation.direction_z.getClockWise();
    creation.direction_y = Direction.UP;
    creation.position = entity.blockPosition().relative(creation.direction_z, 2);
    BufferedImage image = this.load_image(cc.mod_interface.get_arg(ctx, "src", Path.class));
    Dimension dim = this.get_dimension(ctx, image);
    // Generate block image
    cc.image_builder.generate_async(image, dim.width, dim.height, (WorldStructure structure) -> {
      try {
        cc.world_transformer.place_async(structure, creation, (Void v) -> {
          cc.mod_interface.send_feedback(ctx, String.format("Successfully created %dx%d image", dim.width, dim.height));
          return null;
        });
      } catch (Exception e) {
        cc.mod_interface.send_error(ctx, "Failed to place image: " + e.getMessage());
        return null;
      }
      return null;
    });
    return 1;
  }

}
