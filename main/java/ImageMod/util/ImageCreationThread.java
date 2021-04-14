package ImageMod.util;

import java.util.ArrayList;

import ImageMod.commands.ImageCommand;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class ImageCreationThread extends Thread {

    /* vars */
    private ImageCreationData data;
    
	private TransformCreationData transformData;
    private WorldTransformAction transform;
    
	/* classes */
    private static class BlockPixel {

        private ImageCreationData data;
        private ArrayList<ImageBlock> preSized;
        private final int x, y;
        private final int tileWidth, tileHeight;

        public BlockPixel(ImageCreationData data, ArrayList<ImageBlock> preSized, final int x, final int y, final int tileWidth, final int tileHeight) {
            this.data       = data;
            this.preSized   = preSized;
            this.x          = x;
            this.y          = y;
            this.tileWidth  = tileWidth;
            this.tileHeight = tileHeight;
        }

        public void run() {
            final int imgX = (this.x * this.data.image.width)  / this.data.blockWidth;
            final int imgY = (this.y * this.data.image.height) / this.data.blockHeight;
            BlockPos relativePos    = this.data.pos.relative(this.data.xDir, this.x).relative(this.data.yDir, this.y);
            ResizeableImage tileImg = this.data.image.subImage(imgX, imgY, this.tileWidth, this.tileHeight);
            BlockState bestState    = this.getBestFit(tileImg, this.preSized);
            this.data.world.setBlockAndUpdate(relativePos, bestState);
        }
        
        /*
         * Get best BlockState fit for an image
         * Get a dissimilarity score for each option
         * Save record low and return when all options
         * has been tried
         * */
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

    /* Constructor */
    public ImageCreationThread(ImageCreationData data) {
        this.data = data;
        
        transformData.invoker 	= data.invoker;
        transformData.world 	= data.world;
        transformData.origin 	= data.pos;
        transformData.x 		= data.xDir;
        transformData.y			= data.yDir;
        transformData.z			= data.invoker.getDirection();
        transformData.w			= data.blockWidth;
        transformData.h			= data.blockHeight;
        transformData.d 		= 1;
        
        transform = new WorldTransformAction(transformData);
    }

    public void run() {

        /*
        * Image meta-data
        * */
        final int tileWidth  = Math.max(this.data.image.width  / this.data.blockWidth, 1);
        final int tileHeight = Math.max(this.data.image.height / this.data.blockHeight, 1);

        /*
        * Pre-size block images to tileImage size for
        * performance gain and to make it easy to
        * compare later.
        * */
        ArrayList<ImageBlock> preSized = new ArrayList<>();
        for (ImageBlock block : ImageCommand.blockList) {
            preSized.add(new ImageBlock(block.blockState, ResizeableImage.resize(block.image, tileWidth, tileHeight)));
        }

        /*
         * Go through tiles and pick best block fit for each
         * */
        for (int y = 0; y < this.data.blockHeight; y++) {
            for (int x = 0; x < this.data.blockWidth; x++) {
                new BlockPixel(this.data, preSized, x, y, tileWidth, tileHeight).run();
            }
        }
    }
}