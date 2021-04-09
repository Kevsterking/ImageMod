package ImageMod.commands;

import ImageMod.util.DirectoryArgument;
import ImageMod.util.PathArgument;
import ImageMod.util.ResizeableImage;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.Blockreader;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.*;

public class ImageCommand {

    /*====================================================*/
    /*=======================vars=========================*/
    /*====================================================*/

    private static final Logger LOGGER = LogManager.getLogger();
    private static final PathArgument imageSourceArgument = new PathArgument();
    private static ArrayList<ImageBlock> blockList;

    /*====================================================*/
    /*=====================classes========================*/
    /*====================================================*/

    /*
    * Class for storing a state and it's respective
    * texture to use for image creation
    * */
    private static final class ImageBlock {
        public BlockState      blockState;
        public ResizeableImage image;

        ImageBlock(BlockState blockState, ResizeableImage image) {
            this.blockState  = blockState;
            this.image       = image;
        }
    }

    private static final class ImageCreationData {
        public ResizeableImage  image;
        public Entity           invoker;
        public ServerWorld      world;
        public BlockPos         pos;
        public Direction        xDir, yDir;
        public int              blockWidth, blockHeight;
    }

    private static final class ImageCreationThread extends Thread {

        /* vars */
        private ImageCreationData data;

        /* classes */

        private static class BlockPixelThread extends Thread {

            private ImageCreationData data;
            private ArrayList<ImageBlock> preSized;
            private final int x, y;
            private final int tileWidth, tileHeight;

            BlockPixelThread(ImageCreationData data, ArrayList<ImageBlock> preSized, final int x, final int y, final int tileWidth, final int tileHeight) {
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
                BlockState bestState    = ImageCommand.getBestFit(tileImg, this.preSized);
                this.data.world.setBlockAndUpdate(relativePos, bestState);
            }
        }

        /* Constructor */
        public ImageCreationThread(ImageCreationData data) {
            this.data = data;
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
                    new BlockPixelThread(this.data, preSized, x, y, tileWidth, tileHeight).run();
                }
            }
        }
    }

    /*====================================================*/
    /*==================public methods====================*/
    /*====================================================*/

    /*
    * Setup class
    * */
    public static void setup() {
        try {
            ImageCommand.setRootDirectory(System.getProperty("user.home") + "/Downloads");
        } catch (Exception e) {}
    }

    /*
    * Register this command to Minecraft
    * */
    public static void register(CommandDispatcher<CommandSource> dispatcher) {

        ImageCommand.setup();

        /* /image */
        LiteralArgumentBuilder<CommandSource> root = Commands.literal("image");

        /* Create */
        LiteralArgumentBuilder createLiteral = Commands.literal("create");

        LiteralArgumentBuilder wLiteral  = Commands.literal("-width");
        LiteralArgumentBuilder hLiteral  = Commands.literal("-height");
        LiteralArgumentBuilder whInfoLiteral = Commands.literal("~ ~");

        RequiredArgumentBuilder srcArgument        = Commands.argument("src", imageSourceArgument);
        RequiredArgumentBuilder srcArgumentFinal   = Commands.argument("src", imageSourceArgument).executes(ImageCommand::createImageCommand);

        RequiredArgumentBuilder widthArgument      = Commands.argument("width", IntegerArgumentType.integer());
        RequiredArgumentBuilder widthArgumentFinal = Commands.argument("width", IntegerArgumentType.integer()).executes(ImageCommand::createImageCommand);

        //RequiredArgumentBuilder heightArgument      = Commands.argument("height", IntegerArgumentType.integer());
        RequiredArgumentBuilder heightArgumentFinal = Commands.argument("height", IntegerArgumentType.integer()).executes(ImageCommand::createImageCommand);

        wLiteral.then(widthArgumentFinal);
        hLiteral.then(heightArgumentFinal);
        widthArgument.then(heightArgumentFinal);

        srcArgument.then(wLiteral).then(hLiteral).then(whInfoLiteral);
        srcArgument.then(widthArgument);

        createLiteral.then(srcArgument).then(srcArgumentFinal);

        //ArgumentBuilder srcArgument = Commands.argument("src", new PathArgument());

        /* Undo */
        //LiteralArgumentBuilder undoLiteral = Commands.literal("undo").executes(ImageCommand::undoCommand);

        /* SetDirectory */
        LiteralArgumentBuilder setDirectoryLiteral = Commands.literal("setDirectory");
        RequiredArgumentBuilder directoryArgument  = Commands.argument("dir", new DirectoryArgument()).executes(ImageCommand::setDirectoryCommand);

        setDirectoryLiteral.then(directoryArgument);

        root.then(createLiteral).then(setDirectoryLiteral);

        dispatcher.register(root);
    }

    /*
    * Set the root directory for images on local machine
    * */
    public static void setRootDirectory(Path path) throws NotDirectoryException, FileNotFoundException {
        imageSourceArgument.setRootDirectory(path);
    }
    public static void setRootDirectory(String dir) throws NotDirectoryException, FileNotFoundException {
        imageSourceArgument.setRootDirectory(dir);
    }

    /*
    * Reload assets
    * */
    public static void reload() {
        ImageCommand.updateBlockList();
        LOGGER.info("Reload complete.");
    }

    /*
    * Update our locally stored block list
    * Filter out bad block types and assign
    * */
    public static void updateBlockList() {
        ImageCommand.blockList = ImageCommand.filterBlocks(ForgeRegistries.BLOCKS.getValues());
        LOGGER.info("Block list update complete.");
    }

    /*====================================================*/
    /*==================private methods===================*/
    /*====================================================*/

    /*
    * code that gets ran once the command /image create <src> etc. is called.
    * */
    private static int createImageCommand(CommandContext<CommandSource> ctx) throws CommandSyntaxException {

        /*
        * Load source image
        * */
        ResizeableImage image = null;
        int w = 0, h = 0;
        boolean wset = false, hset = false;
        try {
            w = IntegerArgumentType.getInteger(ctx, "width");
            wset = true;
        } catch (Exception e) {}

        try {
            h = IntegerArgumentType.getInteger(ctx, "height");
            hset = true;
        } catch (Exception e) {}

        Path path = PathArgument.getPath(ctx, "src");

        try {
            image = new ResizeableImage(ImageIO.read(path.toFile()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage()); // TODO: throw command syntax errors
        }

        if (!wset && !hset) {
            return -1;
        } else if (!wset) {
            w = (h * image.width) / image.height;
        } else if (!hset) {
            h = (w * image.height) / image.width;
        }

        ImageCommand.createImage(ctx.getSource(), image, w, h);
        LOGGER.info("Generated image successfully");
        return 1;
    }

    // TODO: implement
    private static int undoCommand(CommandContext<CommandSource> ctx) {
        return -1;
    }

    /*
    * Set the root directory for where to look for images
    * */
    private static int setDirectoryCommand(CommandContext<CommandSource> ctx) throws CommandSyntaxException {

        Path dir = DirectoryArgument.getPath(ctx, "dir");

        try {
            ImageCommand.imageSourceArgument.setRootDirectory(dir);
            return 1;
        } catch (Exception e) {}

        return -1;
    }

    /*
    * Create the image as an in game block pixel-art
    * */
    private static void createImage(CommandSource source, ResizeableImage image, final int w, final int h) {

        /*
        * Entity that sent command &
        * world in which the command was sent
        * */
        Entity      invoker = source.getEntity();
        ServerWorld world   = source.getLevel();

        /*
        * Get relative directions and positions
        * to entity for placing image blocks at the
        * right place
        * */
        Direction viewDir    = invoker.getDirection();
        Direction rightDir   = viewDir.getClockWise();
        BlockPos  invokerPos = invoker.blockPosition();
        BlockPos  startPos   = invokerPos.relative(viewDir, 2);

        ImageCreationData data = new ImageCreationData();
        data.image       = image;
        data.invoker     = invoker;
        data.world       = world;
        data.pos         = startPos;
        data.xDir        = rightDir;
        data.yDir        = Direction.UP;
        data.blockWidth  = w;
        data.blockHeight = h;

        new ImageCreationThread(data).run();
    }

    /*
    * Get best BlockState fit for an image
    * Get a dissimilarity score for each option
    * Save record low and return when all options
    * has been tried
    * */
    private static BlockState getBestFit(ResizeableImage img, ArrayList<ImageBlock> blockList) {

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

    /*
    * Filter out unwanted blockStates.
    * ex. non full square blocks
    * */
    private static ArrayList<ImageBlock> filterBlocks(Collection<Block> blocks) {

        ArrayList<ImageBlock> ret = new ArrayList<>();

        Minecraft minecraft = Minecraft.getInstance();
        IResourceManager manager = minecraft.getResourceManager();

        Blockreader blocKReader = new Blockreader(null);

        for (Block block : blocks) {

            BlockState state = block.defaultBlockState();

            Material m    = state.getMaterial();
            VoxelShape vs = state.getShape(blocKReader, BlockPos.ZERO);

            if (!Block.isShapeFullBlock(vs) || !m.isSolid() || block.hasTileEntity(block.defaultBlockState())) {
                LOGGER.error(block.getName().getString() + " is not applicable");
                continue;
            }

            ResourceLocation bLoc = block.getRegistryName();
            ResourceLocation texture = new ResourceLocation(bLoc.getNamespace(), "textures/block/"+bLoc.getPath()+".png");

            try {
                BufferedImage img = ImageIO.read(manager.getResource(texture).getInputStream());
                ret.add(new ImageBlock(state, new ResizeableImage(img)));
                LOGGER.info("successfully loaded: " + texture.getPath());
            } catch(Exception e) {
                LOGGER.warn(e.getMessage());
            }

        }
        return ret;
    }

}
