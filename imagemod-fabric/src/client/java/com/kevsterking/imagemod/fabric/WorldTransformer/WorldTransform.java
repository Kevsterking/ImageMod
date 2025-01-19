package com.kevsterking.imagemod.fabric.WorldTransformer;


import com.kevsterking.imagemod.fabric.ImageModClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class WorldTransform {

	private final WorldStructure previous_structure;
	private final WorldStructure structure;

	private final World level;
	private final BlockPos position;
	private final Direction direction_x, direction_y, direction_z;

	private WorldTransform(
		World level,
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
						.offset(this.direction_x, x)
						.offset(this.direction_y, y)
						.offset(this.direction_z, z);
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
						.offset(this.direction_x, x)
						.offset(this.direction_y, y)
						.offset(this.direction_z, z);
					if (structure.structure[x][y][z] == null) continue;
					this.level.setBlockState(pos, structure.structure[x][y][z]);
				}
			}
		}
	}

	public static WorldTransform place(
		WorldStructure structure,
		ClientWorld world,
		BlockPos position,
		Direction direction_x,
		Direction direction_y,
		Direction direction_z
	) throws Exception {
		ImageModClient.LOGGER.info("In Worldtransform place");
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.getServer() != null) {
			WorldTransform ret = new WorldTransform(
				client.getServer().getWorld(world.getRegistryKey()),
				position,
				direction_x,
				direction_y,
				direction_z,
				structure
			);
			ret.perform_transform();
			return ret;
		}
		throw new Exception("multiplayer image creation coming soon...");
	}

}
