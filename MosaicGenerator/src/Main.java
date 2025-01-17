import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Main {
  public static void main(String[] args) {

    File input = Path.of("tiles/acacia_log.png").toFile();
    File output = Path.of("out/out.png").toFile();

    try {
      BufferedImage image = ImageIO.read(input);
      ImageIO.write(image, "png", output);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}