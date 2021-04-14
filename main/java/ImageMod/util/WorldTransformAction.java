package ImageMod.util;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class WorldTransformAction {

	/*====================================================*/
    /*=======================vars=========================*/
    /*====================================================*/
	
	public TransformCreationData creationData;
	
	public BlockState[][][] previousStructure;
	public BlockState[][][] structure;
	
	public WorldTransformAction(TransformCreationData creationData) {
		creationData.d = Math.max(creationData.d, 1);
		creationData.h = Math.max(creationData.h, 1);
		creationData.w = Math.max(creationData.w, 1);
		
		this.creationData 		= creationData;
		this.structure 			= new BlockState[creationData.w][creationData.h][creationData.d];
		this.previousStructure 	= new BlockState[creationData.w][creationData.h][creationData.d];
	}
	
    /*====================================================*/
    /*=====================classes========================*/
    /*====================================================*/
	
	public static class TransformCreationData {
		public Entity   	invoker;
		public ServerWorld 	world;
		public BlockPos 	origin;
		public Direction   	x, y, z;
		public int			w, h, d;
	}
	
	/*====================================================*/
    /*=====================methods========================*/
    /*====================================================*/
	
	public static BlockState[][][] getStructureFromWorld(ServerWorld world, TransformCreationData creationData) {
		return null;
	}
	
}
