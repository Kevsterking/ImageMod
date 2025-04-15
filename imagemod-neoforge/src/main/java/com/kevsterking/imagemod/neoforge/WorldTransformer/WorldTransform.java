package com.kevsterking.imagemod.neoforge.WorldTransformer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class WorldTransform {

	private final WorldStructure previous_structure;
	private final WorldStructure structure;

	private final ServerLevel level;
	private final BlockPos position;
	private final Direction direction_x, direction_y, direction_z;

	protected WorldTransform(
		ServerLevel level,
		BlockPos position,
		Direction direction_x,
		Direction direction_y,
		Direction direction_z,
		WorldStructure structure
	) {
		this.level = level;
		this.position = position;
		this.direction_x = direction_x;
		this.direction_y = direction_y;
		this.direction_z = direction_z;
		this.structure = structure;
		this.previous_structure = this.get_current_structure();
	}

	// Get current BlockState's in world at position
	public WorldStructure get_current_structure() {
		WorldStructure ret = new WorldStructure(this.structure.width, this.structure.height, this.structure.depth);
		for (int x = 0; x < this.structure.width; x++) {
			for (int y = 0; y < this.structure.height; y++) {
				for (int z = 0; z < this.structure.depth; z++) {
					BlockPos pos = this.position
						.relative(this.direction_x, x)
						.relative(this.direction_y, y)
						.relative(this.direction_z, z);
					ret.structure[x][y][z] = this.level.getBlockState(pos);
				}
			}
		}
		return ret;
	}

	public void perform_transform() {
		this.place_structure(this.structure);
	}
	
	public void revert_transform() {
		this.place_structure(this.previous_structure);
	}

	private void place_structure(WorldStructure structure) {
		for (int x = 0; x < structure.width; x++) {
			for (int y = 0; y < structure.height; y++) {
				for (int z = 0; z < structure.depth; z++) {
					BlockPos pos = this.position
						.relative(this.direction_x, x)
						.relative(this.direction_y, y)
						.relative(this.direction_z, z);
					if (structure.structure[x][y][z] == null) continue;
					this.level.setBlockAndUpdate(pos, structure.structure[x][y][z]);
				}
			}
		}
	}

}
