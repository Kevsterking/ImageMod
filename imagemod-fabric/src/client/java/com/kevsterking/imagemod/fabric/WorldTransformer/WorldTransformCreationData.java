package com.kevsterking.imagemod.fabric.WorldTransformer;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class WorldTransformCreationData {
	public ClientWorld world;
	public BlockPos pos;
	public Direction xDir, yDir, zDir;
	public int w, h, d;
}