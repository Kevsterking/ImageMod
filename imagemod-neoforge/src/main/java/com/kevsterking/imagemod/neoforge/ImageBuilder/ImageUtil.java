package com.kevsterking.imagemod.neoforge.ImageBuilder;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageUtil {

  private static Image format_image(BufferedImage input_image, final int width, final int height) {
    // Needs reformat
    if (
      input_image.getHeight() != height ||
      input_image.getWidth() != width ||
      input_image.getType() != BufferedImage.TYPE_4BYTE_ABGR
    ) {
      BufferedImage formatted = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
      Graphics2D g = formatted.createGraphics();
      // Smooths out the transformation
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      // Draw the original image onto the new image, scaling it in the process
      g.drawImage(input_image, 0, 0, width, height, null);
      g.dispose();
      return new Image(formatted);
    }
    return new Image(input_image);
  }

  // Load image average color
  public static Color load_color(BufferedImage img) {
    return format_image(img, 1, 1).get_color(0, 0);
  }

  // Load image from path and requested width / height
  public static Image load(BufferedImage img, final int width, final int height) {
    return ImageUtil.format_image(img, width, height);
  }

  // Load image from path and requested width - keep aspect
  public static Image load_width(BufferedImage img, final int width) {
    return ImageUtil.format_image(img, width, (width * img.getHeight()) / img.getWidth());
  }

  // Load image from path and requested height - keep aspect
  public static Image load_height(BufferedImage img, final int height) {
    return ImageUtil.format_image(img, (height * img.getWidth()) / img.getHeight(), height);
  }

}
