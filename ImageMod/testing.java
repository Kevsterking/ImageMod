package ImageMod;

import java.nio.file.Path;
import java.nio.file.Paths;

public class testing {


    public static final void main(String[] args) {
        Path p = Paths.get("C:/");
        String path = p.toFile().getPath();
        Path relative = p.relativize(Paths.get(path,"Users"));

        boolean exists = relative.toFile().exists();
        String name = relative.toFile().getPath();
        int a = 10;
    }


}
