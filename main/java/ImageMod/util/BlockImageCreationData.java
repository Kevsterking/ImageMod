package ImageMod.util;

import java.util.function.Consumer;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

/* classes */
public class BlockImageCreationData {
    public ResizeableImage  image;
    public ServerWorld      world;
    public BlockPos         pos;
    public Direction        xDir, yDir, zDir;
    public int              blockWidth, blockHeight;
    
    public Consumer<Exception> 			  onError;
    public Consumer<WorldTransformAction> onSuccess;
}