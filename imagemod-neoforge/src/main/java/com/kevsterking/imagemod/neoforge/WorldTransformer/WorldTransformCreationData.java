package com.kevsterking.imagemod.neoforge.WorldTransformer;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public class WorldTransformCreationData {
	public Level world;
	public BlockPos pos;
	public Direction xDir, yDir, zDir;
	public int w, h, d;
}