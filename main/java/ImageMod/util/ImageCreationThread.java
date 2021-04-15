package ImageMod.util;

import java.util.ArrayList;

import ImageMod.commands.ImageCommand;

public class ImageCreationThread extends Thread {

    /* Vars */
    private ImageCreationData 		creationData;
    private WorldTransformAction 	transform;

    public ImageCreationThread(ImageCreationData creationData) {
    	this.creationData = creationData;
    }
    
    /* Methods */
	public void run() {
		
		/*
		 * Setup data for world creation action 
		 * */
		TransformCreationData transformData = new TransformCreationData();
        
        transformData = new TransformCreationData();
        transformData.invoker 	= creationData.invoker;
        transformData.world 	= creationData.world;
        transformData.origin 	= creationData.pos;
        transformData.x 		= creationData.xDir;
        transformData.y			= creationData.yDir;
        transformData.z			= creationData.invoker.getDirection();
        transformData.w			= creationData.blockWidth;
        transformData.h			= creationData.blockHeight;
        transformData.d 		= 1;
        
        /*
         * Create world transform action
         * */
        this.transform = new WorldTransformAction(transformData);
        
        /*
        * Image meta-data
        * */
        final int tileWidth  = Math.max(this.creationData.image.width  / this.creationData.blockWidth, 1);
        final int tileHeight = Math.max(this.creationData.image.height / this.creationData.blockHeight, 1);

        /*
        * Pre-size block images to tileImage size for
        * performance gain and to make it easy to
        * compare later.
        * */
        ArrayList<ImageBlock> preSized = new ArrayList<>();
        for (ImageBlock block : ImageCommand.blockList) {
            preSized.add(new ImageBlock(block.blockState, ResizeableImage.resize(block.image, tileWidth, tileHeight)));
        }

        //int cores = Runtime.getRuntime().availableProcessors();
        
        ImageCreationWorker[] workers = new ImageCreationWorker[this.creationData.blockWidth];
        for (int y = 0; y < this.creationData.blockHeight; y++) {
            workers[y] = new ImageCreationWorker(this.transform, this.creationData, preSized, tileWidth, tileHeight);
            workers[y].setWorkload(0, this.creationData.blockWidth, y, y+1);
            workers[y].start();
        }
    
        for (int y = 0; y < this.creationData.blockHeight; y++) {
        	try {
    			workers[y].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        this.transform.performAction();
        
        try {
			this.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
}