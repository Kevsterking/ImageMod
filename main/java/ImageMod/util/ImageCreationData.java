package ImageMod.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

/* classes */
public class ImageCreationData {
    public ResizeableImage  image;
    public Entity           invoker;
    public ServerWorld      world;
    public BlockPos         pos;
    public Direction        xDir, yDir;
    public int              blockWidth, blockHeight;
}