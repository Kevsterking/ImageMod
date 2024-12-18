package com.kevsterking.ImageBuilder;

import com.kevsterking.WorldTransformer.WorldTransformAction;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
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