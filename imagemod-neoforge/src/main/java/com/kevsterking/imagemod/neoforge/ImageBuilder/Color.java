package com.kevsterking.imagemod.neoforge.ImageBuilder;

public class Color {

  public int red;
  public int green;
  public int blue;
  public int alpha;

  public Color(java.awt.Color color) {
    this.red = color.getRed();
    this.green = color.getGreen();
    this.blue = color.getBlue();
    this.alpha = color.getAlpha();
  }

  public Color(int red, int green, int blue, int alpha) {
    this.red = red;
    this.green = green;
    this.blue = blue;
    this.alpha = alpha;
  }

  public static int similarity(Color a, Color b) {
    double colorFactor = (double) Math.min(a.alpha, b.alpha) / 255;
    int dr     = Math.abs(a.red - b.red);
    int db     = Math.abs(a.blue - b.blue);
    int dg     = Math.abs(a.green - b.green);
    int dalpha = Math.abs(a.alpha - b.alpha) * 3;
    return (int) ((1.0 - colorFactor) * dalpha + colorFactor * (db + dg + dr));
  }

}
