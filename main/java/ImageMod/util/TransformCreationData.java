package ImageMod.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class TransformCreationData {
	public Entity   	invoker;
	public ServerWorld 	world;
	public BlockPos 	origin;
	public Direction   	x, y, z;
	public int			w, h, d;
}