
import Mosaic.IntColorVector;
import Mosaic.MosaicGenerator;
import Mosaic.MosaicHistogram.ColorHistogram;
import Mosaic.MosaicHistogram.MosaicHisto;
import Mosaic.MosaicInt.MosaicIntColThread;
import Mosaic.MosaicSIMD.MosaicSIMD;
import Mosaic.MosaicSIMD.SIMDColorVector;
import Mosaic.ShortColorVector;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class Main {

  public static BufferedImage get_tile(File tile_image) throws Exception {
    BufferedImage image;
    try {
      image = ImageIO.read(tile_image);
    } catch (IOException e) {
      throw new Exception(tile_image.getName() + " - could not read as Image");
    }
    if (image == null) {
      throw new Exception(tile_image.getName()+" - is null");
    }
    if (image.getWidth() != 16 || image.getHeight() != 16) {
      throw new Exception(tile_image.getName()+" - width and/or height not 16");
    }
    return image;
  }

  public static void generate_collage(ArrayList<BufferedImage> images, File output) throws IOException {
    int size = images.size();
    int wh = (int) Math.ceil(Math.sqrt(size));
    BufferedImage combined = new BufferedImage(wh*16, wh*16, BufferedImage.TYPE_INT_ARGB);
    // paint both images, preserving the alpha channels
    Graphics g = combined.getGraphics();
    for (int y = 0; y < wh; y++) {
      for (int x = 0; x < wh; x++) {
        if (x+y*wh >= size - 1) break;
        g.drawImage(images.get(x+y*wh), x*16, y*16, null);
      }
    }
    g.dispose();
    ImageIO.write(combined, "png", output);
  }

  public static void main(String[] args) {

    byte a = 0;
    byte b = (byte) 0xff;
    System.out.println(Byte.compareUnsigned(a, b));
    ArrayList<BufferedImage> tiles = new ArrayList<>();
    File[] tile_images = Path.of("tiles").toFile().listFiles();
    if (tile_images == null) {
      System.out.println("No tile images in folder /tiles");
    }
    for (File f : tile_images) {
      try {
        tiles.add(Main.get_tile(f));
      } catch (Exception e) {
        //System.out.println(e.getMessage());
      }
    }
//    try {
//      Main.generate_collage(tiles, Path.of("out/out.png").toFile());
//    } catch (IOException e) {
//      System.out.println("Could not write to output");
//    }
//
    BufferedImage[] tile_arr = new BufferedImage[tiles.size()];

    BufferedImage input_image = null;
    try {
      input_image = ImageIO.read(Path.of("out/cat.jpeg").toFile());
    } catch (IOException e) {
      System.out.println("Could not read input file");
    }

    for (int i = 0; i < tiles.size(); i++) {
      tile_arr[i] = tiles.get(i);
    }

    final int tile_size = 16;
    final int block_width = 200;
    //final int bw = block_width, bh = (int) (block_width * ((double)input_image.getHeight()/input_image.getWidth()));
    final int bw = (int) (block_width * ((double)input_image.getWidth()/input_image.getHeight())), bh = block_width;
    final long pcmps = (long) tile_size * (long) tile_size * (long)bw * (long)bh * (long)tile_images.length;

    MosaicGenerator<IntColorVector, BufferedImage, BufferedImage, BufferedImage> mosaic = new MosaicIntColThread();
    mosaic.set_tiles(tile_arr, tile_size);

    long end, start = System.currentTimeMillis();
    BufferedImage out = mosaic.generate(input_image, bw, bh);
    end = System.currentTimeMillis();

    double seconds = ((double)(end-start) / 1000);
    System.out.printf("start: %d, end: %d, pcmps: %d\n", start, end, pcmps);
    System.out.printf("%fs - %fMpx/s\n", seconds, (((double)pcmps / seconds) / 1000000));

    try {
      ImageIO.write(out, "png", Path.of("out/catout2.png").toFile());
    } catch (Exception e) {
      System.out.println("failed to output image");
    }

  }
}