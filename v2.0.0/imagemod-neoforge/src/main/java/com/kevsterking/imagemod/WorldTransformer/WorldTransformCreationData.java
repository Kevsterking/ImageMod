package com.kevsterking.imagemod.WorldTransformer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class WorldTransformCreationData {
	public ServerLevel world;
	public BlockPos pos;
	public Direction xDir, yDir, zDir;
	public int			w, h, d;
}