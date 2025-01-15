package com.kevsterking.imagemod.neoforge.ImageBuilder;

import com.kevsterking.imagemod.neoforge.WorldTransformer.WorldTransformAction;
import com.kevsterking.imagemod.neoforge.WorldTransformer.WorldTransformCreationData;

import java.util.ArrayList;

public class BlockImageBuilder extends Thread {

    private BlockImageCreationData creationData;
    private WorldTransformAction transform;
    private ArrayList<ImageBlock> image_blocks;

    public BlockImageBuilder(BlockImageCreationData creationData, ArrayList<ImageBlock> image_blocks) {
    	this.image_blocks = image_blocks;
        this.creationData = creationData;
    	this.run();//.start();
    }

    // Report back error
    private void reportError(Exception e) {
    	if (this.creationData.onError != null) {
    		this.creationData.onError.accept(e);
    	}
    }
    
    // Report back success
    private void reportSuccess(WorldTransformAction transform) {
    	this.creationData.onSuccess.accept(transform);
    }
    
    // Thread run entry
    public void run() {
		
        // Setup data for world creation action
		WorldTransformCreationData transformData = new WorldTransformCreationData();
        transformData.world 	= creationData.world;
        transformData.pos 		= creationData.pos;
        transformData.xDir 		= creationData.xDir;
        transformData.yDir		= creationData.yDir;
        transformData.zDir		= creationData.zDir;
        transformData.w			= creationData.blockWidth;
        transformData.h			= creationData.blockHeight;
        transformData.d 		= 1;
        
        // Create world transform action
        this.transform = new WorldTransformAction(transformData);
        
        // Image meta-data
        final int tileWidth  = Math.max(this.creationData.image.width  / this.creationData.blockWidth, 1);
        final int tileHeight = Math.max(this.creationData.image.height / this.creationData.blockHeight, 1);

        // Pre-size block images to tileImage size for
        // performance gain and to make it easy to
        // compare later.
        ArrayList<ImageBlock> preSized = new ArrayList<>();
        for (ImageBlock block : this.image_blocks) {
            preSized.add(new ImageBlock(block.blockState, ResizeableImage.resize(block.image, tileWidth, tileHeight)));
        }

        BlockImageCreationWorker[] workers = new BlockImageCreationWorker[this.creationData.blockHeight];
        for (int y = 0; y < this.creationData.blockHeight; y++) {
            workers[y] = new BlockImageCreationWorker(this.transform, this.creationData, preSized, tileWidth, tileHeight);
            workers[y].setWorkload(0, this.creationData.blockWidth, y, y+1);
            workers[y].run();//.start();
        }

        /*
        for (int y = 0; y < this.creationData.blockHeight; y++) {
        	try {
    			workers[y].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        */
        this.reportSuccess(this.transform);
        
        try {
			this.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
}