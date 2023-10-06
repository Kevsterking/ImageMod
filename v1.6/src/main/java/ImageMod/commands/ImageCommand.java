package ImageMod.command;

import ImageMod.ImageBuilder.BlockImageBuilder;
import ImageMod.ImageBuilder.BlockImageCreationData;
import ImageMod.ImageBuilder.ImageBlock;
import ImageMod.ImageBuilder.ResizeableImage;
import ImageMod.ImageMod;
import ImageMod.WorldTransformer.WorldTransformAction;
import ImageMod.util.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CoralBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
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

    public static ArrayList<ImageBlock> blockList;

    private static final Logger LOGGER = LogManager.getLogger();
    private static final PathArgument imageSourceArgument = new PathArgument();

    public static Stack<WorldTransformAction> undoStack = new Stack<>();
    public static Stack<WorldTransformAction> redoStack = new Stack<>();

     /**
     * Setup class
     **/

    public static void setup() {
        try {
            ImageCommand.setRootDirectory(System.getProperty("user.home") + "/Downloads");
        } catch (Exception ignored) {}
    }

     /**
     * Register this command to Minecraft
     **/

     public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
         ImageCommand.setup();

         LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("image")
                 .then(Commands.literal("create")
                         .then(Commands.argument("src", imageSourceArgument)
                                 .then(Commands.literal("size")
                                         .then(Commands.argument("size", IntegerArgumentType.integer())
                                                 .executes(ctx -> createImageCommand(ctx, IntegerArgumentType.getInteger(ctx, "size"), IntegerArgumentType.getInteger(ctx, "size"))))
                                         .then(Commands.argument("width", IntegerArgumentType.integer())
                                                 .then(Commands.argument("height", IntegerArgumentType.integer())
                                                         .executes(ctx -> createImageCommand(ctx, IntegerArgumentType.getInteger(ctx, "width"), IntegerArgumentType.getInteger(ctx, "height")))))
                                         .executes(ctx -> {
                                             sendErrorMessage(ctx.getSource());
                                             return 0;
                                         })))
                         .executes(ctx -> {
                             sendErrorMessage(ctx.getSource());
                             return 0;
                         }))
                 .then(Commands.literal("reload")
                         .executes(ImageCommand::reloadCommand))
                 .then(Commands.literal("undo")
                         .executes(ImageCommand::undoCommand))
                 .then(Commands.literal("redo")
                         .executes(ImageCommand::redoCommand))
                 .then(Commands.literal("setDirectory")
                         .then(Commands.argument("dir", new DirectoryArgument())
                                 .executes(ImageCommand::setDirectoryCommand)));

         dispatcher.register(root);
     }

    private static void sendErrorMessage(CommandSourceStack source) {
        source.sendFailure(Component.literal("Please provide width and height arguments."));
    }

     /**
     * Set the root directory for images on local machine
     **/

    public static void setRootDirectory(Path path) throws NotDirectoryException, FileNotFoundException {
        PathArgument.setRootDirectory(path);
    }
    public static void setRootDirectory(String dir) throws NotDirectoryException, FileNotFoundException {
        PathArgument.setRootDirectory(dir);
    }

     /**
     * Reload assets
     **/

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
    private static int createImageCommand(CommandContext<CommandSourceStack> ctx, int size, int integer) {

         /**
         * Load source image
         **/
        ResizeableImage image = null;
        int w = 0, h = 0;
        boolean width_set = false, height_set = false;
        try {
            w = IntegerArgumentType.getInteger(ctx, "width");
            width_set = true;
        } catch (Exception ignored) {}

        try {
            h = IntegerArgumentType.getInteger(ctx, "height");
            height_set = true;
        } catch (Exception ignored) {}

        Path path = PathArgument.getPath(ctx, "src");

        try {
            image = new ResizeableImage(ImageIO.read(path.toFile()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage()); // TODO: throw command syntax errors
        }

        if (!width_set && !height_set) {
            return -1;
        } else if (!width_set) {
            if (image != null) {
                w = (h * image.width) / image.height;
            }
        } else if (!height_set) {
            if (image != null) {
                h = (w * image.height) / image.width;
            }
        }


         /**
         * world in which the command was sent
         **/
        CommandSourceStack source 	= ctx.getSource();
        Entity entity  	            = source.getEntity();
        ServerLevel world   	    = source.getLevel();

         /**
         * Get relative directions and positions
         * to entity for placing image blocks at the
         * right place
         **/
        Direction viewDir       = null;
        if (entity != null) {
            viewDir = entity.getDirection();
        }
        Direction rightDir      = null;
        if (viewDir != null) {
            rightDir = viewDir.getClockWise();
        }
        BlockPos startPos       = null;
        if (entity != null) {
            startPos = entity.blockPosition().relative(viewDir, 2);
        }

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
        creationData.onError = (e) -> source.sendFailure(Component.literal(e.getMessage()));

        creationData.onSuccess = (transform) -> {
            source.sendSuccess(() -> Component.literal(String.format("Successfully created (%dx%d) image", ww, hh)), true);
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
    private static int undoCommand(CommandContext<CommandSourceStack> ctx) {

        if (ImageCommand.undoStack.empty()) {
            ctx.getSource().sendFailure(Component.literal("Undo stack is empty"));
            return -1;
        }

        WorldTransformAction action = ImageCommand.undoStack.pop();
        action.revertAction();
        ImageCommand.redoStack.push(action);

        ctx.getSource().sendSuccess(() -> Component.literal("Successfully reverted last image creation"), true);

        return 1;
    }

    /*
     * Redo last undo
     * */
    private static int redoCommand(CommandContext<CommandSourceStack> ctx) {

        if (ImageCommand.redoStack.empty()) {
            ctx.getSource().sendFailure(Component.literal("Redo stack is empty"));
            return -1;
        }

        WorldTransformAction action = ImageCommand.redoStack.pop();
        action.performAction();
        ImageCommand.undoStack.push(action);

        ctx.getSource().sendSuccess(() -> Component.literal("Successfully recreated image"), true);

        return 1;
    }

    /*
     * Set the root directory for where to look for images
     * */
    private static int setDirectoryCommand(CommandContext<CommandSourceStack> ctx) {

        Path dir = DirectoryArgument.getPath(ctx, "dir");

        try {
            PathArgument.setRootDirectory(dir);
            ctx.getSource().sendSuccess(() -> Component.literal("Successfully set image directory to \"" + dir.toString() + "\""), true);
            return 1;
        } catch (NotDirectoryException e) {
            ctx.getSource().sendFailure(Component.literal("Provided path is not a directory"));
        } catch (FileNotFoundException e) {
            ctx.getSource().sendFailure(Component.literal("Provided path could not be found"));
        }

        return -1;
    }

    /*
     * Reload command variables
     * */
    private static int reloadCommand(CommandContext<CommandSourceStack> ctx) {
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
     * ex. non-full square blocks
     * */
    private static ArrayList<ImageBlock> filterBlocks(Collection<Block> blocks) {

        System.out.println("!!!!!Filtering Blocks!!!!!");

        ArrayList<ImageBlock> ret = new ArrayList<>();

        ResourceManager manager = ImageMod.getResourceManager();
        BlockGetter blocKReader = EmptyBlockGetter.INSTANCE;

        for (Block block : blocks) {

            BlockState state = block.defaultBlockState();
            VoxelShape vs = state.getShape(blocKReader, BlockPos.ZERO);

            boolean applicable = Block.isShapeFullBlock(vs);

            if (!state.isSolidRender(blocKReader, BlockPos.ZERO)) {
                applicable = false;
            }

            if (block.hasDynamicShape()) {
                applicable = false;
            }

            if (block.getLightEmission(state, blocKReader, BlockPos.ZERO) > 0) {
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

            ResourceLocation bLoc = BlockModelShaper.stateToModelLocation(state);
            ResourceLocation texture = new ResourceLocation(bLoc.getNamespace(), "textures/block/"+bLoc.getPath()+".png");

            LOGGER.warn(bLoc.getPath());

            try {
                Optional<InputStream> inputStream = Optional.of(manager.getResource(texture).get().open());
                BufferedImage img = ImageIO.read(inputStream.get());
                ret.add(new ImageBlock(state, new ResizeableImage(img)));
                LOGGER.info("Successfully loaded: " + texture.getPath());
            } catch (Exception e) {
                LOGGER.warn("Error loading image from " + texture.getPath() + ": " + e.getMessage());
            }


        }
        return ret;
    }

}
