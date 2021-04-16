package ImageMod.util;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class TransformCreationData {
	public ServerWorld 	world;
	public BlockPos 	pos;
	public Direction   	xDir, yDir, zDir;
	public int			w, h, d;
}