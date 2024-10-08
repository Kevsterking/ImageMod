package com.kevsterking.imagemod.ImageBuilder;

import java.util.ArrayList;

import com.kevsterking.imagemod.WorldTransformer.WorldTransformAction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BlockImageCreationWorker extends Thread {

	// Vars
	private WorldTransformAction 	    transform;
    private BlockImageCreationData 		creationData;
    private ArrayList<ImageBlock> 	    preSized;
    private final int 				    tileWidth, tileHeight;

    private int xMin, xMax, yMin, yMax;
    
    public BlockImageCreationWorker(WorldTransformAction transform, BlockImageCreationData creationData, ArrayList<ImageBlock> preSized, final int tileWidth, final int tileHeight) {
        this.transform  	= transform;
    	this.creationData 	= creationData;
        this.preSized   	= preSized;
        this.tileWidth  	= tileWidth;
        this.tileHeight 	= tileHeight;
    }

    // Perform workload
    public void run() {
    	for (int y = yMin; y < yMax; y++) {
    		for (int x = xMin; x < xMax; x++) {
				final int imgX = (x * this.creationData.image.width)  / this.creationData.blockWidth;
				final int imgY = (y * this.creationData.image.height) / this.creationData.blockHeight;
				ResizeableImage tileImg = this.creationData.image.subImage(imgX, imgY, this.tileWidth, this.tileHeight);
				this.transform.set(x, y, this.getBestFit(tileImg, this.preSized));
    		}
    	}
    }
    
    // Set a workload for worker
    public void setWorkload(int xMin, int xMax, int yMin, int yMax) {
    	this.xMin = xMin;
    	this.xMax = xMax;
    	this.yMin = yMin;
    	this.yMax = yMax;
    }
    

     // Get best BlockState fit for an image
     // Get a dissimilarity score for each option
     // Save record low and return when all options
     // has been tried
     private BlockState getBestFit(ResizeableImage img, ArrayList<ImageBlock> blockList) {

         BlockState ret = Blocks.AIR.defaultBlockState();
         int minScore = img.getSimilarity(ResizeableImage.getTransparant(img.width, img.height));

         for (ImageBlock block : blockList) {
             int score = block.image.getSimilarity(img);
             if (score < minScore) {
                 minScore = score;
                 ret = block.blockState;
             }
         }

         return ret;
     }
}
