package ImageMod.ImageBuilder;

import java.util.function.Consumer;

import ImageMod.WorldTransformer.WorldTransformAction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

/* classes */
public class BlockImageCreationData {
    public ResizeableImage image;
    public ServerLevel world;
    public BlockPos pos;
    public Direction xDir;
    public Direction yDir;
    public Direction zDir;
    public int blockWidth, blockHeight;
    
    public Consumer<Exception> onError;
    public Consumer<WorldTransformAction> onSuccess;
}
