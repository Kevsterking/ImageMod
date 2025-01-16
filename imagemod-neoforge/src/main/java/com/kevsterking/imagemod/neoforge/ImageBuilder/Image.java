package com.kevsterking.imagemod.neoforge.ImageBuilder;

import java.awt.image.BufferedImage;

public class Image {

  public BufferedImage image;
  public int width, height;

  public Image(BufferedImage image) {
    this.image = image;
    this.width = image.getWidth();
    this.height = image.getHeight();
  }

  public Color get_color(final int x, final int y) {
    return new Color(new java.awt.Color(image.getRGB(x, y), true));
  }

}
