package com.kevsterking.imagemod.neoforge.ImageBuilder;

import com.kevsterking.imagemod.neoforge.WorldTransformer.WorldTransformAction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class BlockImageCreationData {
    public ResizeableImage image;
    public Level world;
    public BlockPos pos;
    public Direction xDir;
    public Direction yDir;
    public Direction zDir;
    public int blockWidth, blockHeight;
    public Consumer<Exception> onError;
    public Consumer<WorldTransformAction> onSuccess;
}