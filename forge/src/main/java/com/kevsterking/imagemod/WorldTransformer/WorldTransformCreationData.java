package com.kevsterking.imagemod.WorldTransformer;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class WorldTransformCreationData {
	public ServerWorld 	world;
	public BlockPos 	pos;
	public Direction   	xDir, yDir, zDir;
	public int			w, h, d;
}