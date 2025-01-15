package com.kevsterking.imagemod.neoforge.WorldTransformer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class WorldTransformAction {

	public WorldTransformCreationData creationData;
	public BlockState[][][] previousStructure;
	public BlockState[][][] structure;
	
	public WorldTransformAction(WorldTransformCreationData creationData) {
		creationData.d = Math.max(creationData.d, 1);
		creationData.h = Math.max(creationData.h, 1);
		creationData.w = Math.max(creationData.w, 1);
		this.creationData 		= creationData;
		this.structure 			= new BlockState[creationData.w][creationData.h][creationData.d];
		this.previousStructure 	= WorldTransformAction.getCurrentStructure(creationData);
	}

	// Get current blockstates in world at position
	public static BlockState[][][] getCurrentStructure(WorldTransformCreationData creationData) {
		BlockState[][][] ret = new BlockState[creationData.w][creationData.h][creationData.d];
		for (int x = 0; x < creationData.w; x++) {
			for (int y = 0; y < creationData.h; y++) {
				for (int z = 0; z < creationData.d; z++) {
					BlockPos pos = creationData.pos
						.relative(creationData.xDir, x)
						.relative(creationData.yDir, y)
						.relative(creationData.zDir, z);
					ret[x][y][z] = creationData.world.getBlockState(pos);
				}
			}
		}
		return ret;
	}
	
	// Set blockstate in structure
	public void set(int x, int y, int z, BlockState state) {
		this.structure[x][y][z] = state;
	}
	public void set(int x, int y, BlockState state) {
		this.set(x,  y, 0, state);
	}
	
	// Place the structure
	public void performAction() {
		this.placeStructure(this.structure);
	}
	
	// Revert the performed action
	public void revertAction() {
		this.placeStructure(this.previousStructure);
	}
	
	// Place blocks for structure in world
	private void placeStructure(BlockState[][][] structure) {
		for (int x = 0; x < this.creationData.w; x++) {
			for (int y = 0; y < this.creationData.h; y++) {
				for (int z = 0; z < this.creationData.d; z++) {
					BlockPos pos = this.creationData.pos
							.relative(this.creationData.xDir, x)
							.relative(this.creationData.yDir, y)
							.relative(this.creationData.zDir, z);
					BlockState state = structure[x][y][z];
					this.creationData.world.setBlock(pos, state, 16);
				}
			}
		}
	}

}
