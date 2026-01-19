package com.kevsterking.imagemod.core.transform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class WorldTransform {

	private final WorldStructure previous_structure;
	private final WorldStructure structure;

	private final Level level;
	private final BlockPos position;
	private final Direction direction_x, direction_y, direction_z;

	protected WorldTransform(
					ServerLevel level,
					WorldStructure structure,
					WorldTransformCreation creation
	) {
		this.level = level;
		this.position = creation.position;
		this.direction_x = creation.direction_x;
		this.direction_y = creation.direction_y;
		this.direction_z = creation.direction_z;
		this.structure = structure;
    this.previous_structure = this.get_current_structure();
	}

	public void perform_transform() {
		this.place_structure(this.structure);
	}

	public void revert_transform() {
		this.place_structure(this.previous_structure);
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
