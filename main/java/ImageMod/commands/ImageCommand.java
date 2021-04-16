package ImageMod.commands;

import ImageMod.ImageBuilder.BlockImageBuilder;
import ImageMod.ImageBuilder.BlockImageCreationData;
import ImageMod.ImageBuilder.ImageBlock;
import ImageMod.ImageBuilder.ResizeableImage;
import ImageMod.WorldTransformer.WorldTransformAction;
import ImageMod.util.*;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CoralBlock;
import net.minecraft.block.LeavesBlock;
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
import net.minecraft.util.text.StringTextComponent;
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

    public static ArrayList<ImageBlock> blockList;
	
    private static final Logger LOGGER = LogManager.getLogger();
    private static final PathArgument imageSourceArgument = new PathArgument();

    public static Stack<WorldTransformAction> undoStack = new Stack<>();
    public static Stack<WorldTransformAction> redoStack = new Stack<>();

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

        /* image */
        LiteralArgumentBuilder<CommandSource> root = Commands.literal("image");

        /* Create */
        LiteralArgumentBuilder<CommandSource> createLiteral = Commands.literal("create").requires((source) -> { return source.hasPermission(2); });

        LiteralArgumentBuilder<CommandSource> wLiteral  	= Commands.literal("-width");
        LiteralArgumentBuilder<CommandSource> hLiteral  	= Commands.literal("-height");
        LiteralArgumentBuilder<CommandSource> whInfoLiteral = Commands.literal("~ ~");
        
        RequiredArgumentBuilder<CommandSource, Path> srcArgument        = Commands.argument("src", imageSourceArgument);
        //RequiredArgumentBuilder<CommandSource, Path> srcArgumentFinal   = Commands.argument("src", imageSourceArgument).executes(ImageCommand::createImageCommand);

        RequiredArgumentBuilder<CommandSource, Integer> wArgument      = Commands.argument("width", IntegerArgumentType.integer());
        RequiredArgumentBuilder<CommandSource, Integer> wArgumentFinal = Commands.argument("width", IntegerArgumentType.integer()).executes(ImageCommand::createImageCommand);

        //RequiredArgumentBuilder heightArgument      = Commands.argument("height", IntegerArgumentType.integer());
        RequiredArgumentBuilder<CommandSource, Integer> hArgumentFinal = Commands.argument("height", IntegerArgumentType.integer()).executes(ImageCommand::createImageCommand);

        LiteralArgumentBuilder<CommandSource> reload = Commands.literal("reload").executes(ImageCommand::reloadCommand);
        
        wLiteral.then(wArgumentFinal);
        hLiteral.then(hArgumentFinal);
        wArgument.then(hArgumentFinal);

        srcArgument.then(wLiteral).then(hLiteral).then(whInfoLiteral).then(wArgument);

        createLiteral.then(srcArgument);//.then(srcArgumentFinal);

        /* Undo */
        LiteralArgumentBuilder<CommandSource> undoLiteral = Commands.literal("undo").executes(ImageCommand::undoCommand);

        /* Redo */
        LiteralArgumentBuilder<CommandSource> redoLiteral = Commands.literal("redo").executes(ImageCommand::redoCommand);
        
        /* SetDirectory */
        LiteralArgumentBuilder<CommandSource> setDirectoryLiteral = Commands.literal("setDirectory");
        RequiredArgumentBuilder<CommandSource, Path> directoryArgument  = Commands.argument("dir", new DirectoryArgument()).executes(ImageCommand::setDirectoryCommand);

        setDirectoryLiteral.then(directoryArgument);

        root.then(createLiteral).then(setDirectoryLiteral).then(reload).then(undoLiteral).then(redoLiteral);

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


        /*
         * world in which the command was sent
         * */
        CommandSource source 	= ctx.getSource();
    	Entity		entity  	= source.getEntity();
        ServerWorld world   	= source.getLevel();

        /*
         * Get relative directions and positions
         * to entity for placing image blocks at the
         * right place
         * */
        Direction viewDir    = entity.getDirection();
        Direction rightDir   = viewDir.getClockWise();
        BlockPos  startPos   = entity.blockPosition().relative(viewDir, 2);

        /*
         * Feels like dumb solution but it works
         * */
        final int ww = w;
        final int hh = h;

        /*
         * Set creation data needed for creation of an image
         * */
        BlockImageCreationData creationData = new BlockImageCreationData();
        creationData.image       = image;
        creationData.world       = world;
        creationData.pos         = startPos;
        creationData.xDir        = rightDir;
        creationData.yDir        = Direction.UP;
        creationData.zDir		 = viewDir;
        creationData.blockWidth  = w;
        creationData.blockHeight = h;
        creationData.onError = (e) -> {
        	source.sendFailure(new StringTextComponent(e.getMessage()));
        };
        creationData.onSuccess = (transform) -> {
        	source.sendSuccess(new StringTextComponent(String.format("Successfully created (%dx%d) image", ww, hh)), true);
        	transform.performAction();
        	ImageCommand.undoStack.push(transform);
        };
        
        
        /*
         * Create image 
         * */
        ImageCommand.createImage(creationData);
        
        return 1;
    }

    /*
    * Undo last world transform
    * */
    private static int undoCommand(CommandContext<CommandSource> ctx) {
        
    	if (ImageCommand.undoStack.empty()) {
    		ctx.getSource().sendFailure(new StringTextComponent("Undo stack is empty"));
    		return -1;
    	}
    	
    	WorldTransformAction action = ImageCommand.undoStack.pop();;
        action.revertAction();
        ImageCommand.redoStack.push(action);
        
        ctx.getSource().sendSuccess(new StringTextComponent("Successfully reverted last image creation"), true);
        
        return 1;
    }

    /*
    * Redo last undo
    * */
    private static int redoCommand(CommandContext<CommandSource> ctx) {
        
    	if (ImageCommand.redoStack.empty()) {
    		ctx.getSource().sendFailure(new StringTextComponent("Redo stack is empty"));
    		return -1;
    	}
    	
    	WorldTransformAction action = ImageCommand.redoStack.pop();
        action.performAction();
        ImageCommand.undoStack.push(action);
        
        ctx.getSource().sendSuccess(new StringTextComponent("Successfully recreated image"), true);
        
        return 1;
    }

    /*
    * Set the root directory for where to look for images
    * */
    private static int setDirectoryCommand(CommandContext<CommandSource> ctx) throws CommandSyntaxException {

        Path dir = DirectoryArgument.getPath(ctx, "dir");

        try {
            ImageCommand.imageSourceArgument.setRootDirectory(dir);
            ctx.getSource().sendSuccess(new StringTextComponent("Successfully set image directory to \"" + dir.toString() + "\""), true);
            return 1;
        } catch (NotDirectoryException e) {
        	ctx.getSource().sendFailure(new StringTextComponent("Provided path is not a directory"));
        } catch (FileNotFoundException e) {
        	ctx.getSource().sendFailure(new StringTextComponent("Provided path could not be found"));
        }

        return -1;
    }
    
    /*
     * Reload command variables
     * */
    private static int reloadCommand(CommandContext<CommandSource> ctx) {
    	ImageCommand.reload();
    	return 1;
    }
    
    /*
    * Create the image as an in game block pixel-art
    * */
    private static void createImage(BlockImageCreationData creationData) {
        new BlockImageBuilder(creationData);
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

            boolean applicable = true;
            
            if (!Block.isShapeFullBlock(vs)) {
            	applicable = false;
            }
            
            if (!m.isSolid()) {
            	applicable = false;
            }
            
            if (block.hasTileEntity(state)) {
            	applicable = false;
            }
            
            if (block.getLightValue(state, blocKReader, BlockPos.ZERO) > 0) {
            	applicable = false;
            }
            
            if (block instanceof CoralBlock) {
            	applicable = false;
            }
            
            if (block instanceof LeavesBlock) {
            	applicable = false;
            }
            
            if (!applicable) {
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
