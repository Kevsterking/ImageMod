package com.kevsterking.imagemod.fabric.ImageBuilder;

import com.kevsterking.imagemod.core.ImageBuilder.ResizeableImage;
import com.kevsterking.imagemod.fabric.WorldTransformer.WorldTransformAction;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import java.util.function.Consumer;

public class BlockImageCreationData {
    public ResizeableImage image;
    public ClientWorld world;
    public BlockPos pos;
    public Direction xDir;
    public Direction yDir;
    public Direction zDir;
    public int blockWidth, blockHeight;
    public Consumer<Exception> onError;
    public Consumer<WorldTransformAction> onSuccess;
}