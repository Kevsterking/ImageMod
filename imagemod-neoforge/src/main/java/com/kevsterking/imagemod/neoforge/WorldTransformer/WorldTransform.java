package com.kevsterking.imagemod.neoforge.WorldTransformer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class WorldTransform {

	private final BlockState[][][] previous_structure;
	public BlockState[][][] structure;

	private final Level level;
	private final BlockPos position;
	private final Direction direction_x, direction_y, direction_z;
	private final int width, height, depth;

	public WorldTransform(
		Level level,
		BlockPos position,
		Direction direction_x,
		Direction direction_y,
		Direction direction_z,
		int width, int height, int depth
	) {
		this.level = level;
		this.position = position;
		this.direction_x = direction_x;
		this.direction_y = direction_y;
		this.direction_z = direction_z;
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.structure = new BlockState[width][height][depth];
		this.previous_structure = this.get_current_structure();
	}

	// Get current BlockState's in world at position
	public BlockState[][][] get_current_structure() {
		BlockState[][][] ret = new BlockState[this.width][this.height][this.depth];
		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				for (int z = 0; z < this.depth; z++) {
					BlockPos pos = this.position
						.relative(this.direction_x, x)
						.relative(this.direction_y, y)
						.relative(this.direction_z, z);
					ret[x][y][z] = this.level.getBlockState(pos);
				}
			}
		}
		return ret;
	}

	public void perform_action() {
		this.place_structure(this.structure);
	}
	
	public void revert_action() {
		this.place_structure(this.previous_structure);
	}

	private void place_structure(BlockState[][][] structure) {
		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				for (int z = 0; z < this.depth; z++) {
					BlockPos pos = this.position
						.relative(this.direction_x, x)
						.relative(this.direction_y, y)
						.relative(this.direction_z, z);
					BlockState state = structure[x][y][z];
					this.level.setBlock(pos, state, 0);
				}
			}
		}
	}

}
