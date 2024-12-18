package com.kevsterking.commands;

import com.kevsterking.ImageBuilder.BlockImageBuilder;
import com.kevsterking.ImageBuilder.BlockImageCreationData;
import com.kevsterking.ImageBuilder.ImageBlock;
import com.kevsterking.ImageBuilder.ResizeableImage;
import com.kevsterking.ImagemodClient;
import com.kevsterking.WorldTransformer.WorldTransformAction;
import com.kevsterking.util.DirectoryArgument;
import com.kevsterking.util.PathArgument;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CoralBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.EmptyBlockView;
import javax.imageio.ImageIO;
import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ImageCommand {

    private static final PathArgument src_arg = new PathArgument();
    private static final PathArgument dir_arg = new DirectoryArgument();
    private static ArrayList<ImageBlock> image_blocks = new ArrayList<ImageBlock>();
    private static Stack<WorldTransformAction> undo_stack = new Stack<>();
    private static Stack<WorldTransformAction> redo_stack = new Stack<>();

    // Filter out unwanted blocks based on properties that
    // create unwanted visual differences
    // true -> keep block, false -> don't use block
    private static boolean filter_block(Block block) {
        BlockState state = block.getDefaultState();
        VoxelShape vs = state.getCollisionShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
        return (Block.isShapeFullCube(vs) &&
            state.isSolidBlock(EmptyBlockView.INSTANCE, BlockPos.ORIGIN) &&
            !block.hasDynamicBounds() &&
            state.getLuminance() == 0 &&
            !(block instanceof CoralBlock) &&
            !(block instanceof LeavesBlock));
    }

    // Load textured resource for block into ImageBlock
    private static ImageBlock get_image_block(Block block, Direction dir) throws Exception {

        BlockState state = block.getDefaultState();
        Identifier block_id, texture_id;
        ResourceManager resource_manager = MinecraftClient.getInstance().getResourceManager();

        block_id = MinecraftClient
                .getInstance()
                .getBakedModelManager()
                .getBlockModels()
                .getModel(state)
                .getParticleSprite()
                .getContents()
                .getId();

        texture_id = Identifier.of(block_id.getNamespace(), "textures/"+block_id.getPath() + ".png");

        if (resource_manager.getResource(texture_id).isEmpty()) {
            throw new Exception("No resource available for block");
        }

        return new ImageBlock(state, new ResizeableImage(
            ImageIO.read(resource_manager.getResource(texture_id).get().getInputStream()))
        );

    }

    // Update our locally stored block list
    // Filter out bad block types and assign
    public static void update_block_list() {

        ImagemodClient.LOGGER.info("Loading blocks.");

        List<Block> blocks = Registries.BLOCK.stream().toList();
        image_blocks.clear();

        for (Block block : blocks) {
            if (filter_block(block)) {
                try {
                    image_blocks.add(get_image_block(block, Direction.NORTH));
                    ImagemodClient.LOGGER.debug("{} - SUCCESS!", block.getName().getString());
                } catch (Exception e) {
                    ImagemodClient.LOGGER.debug("{} - rejected: {}", block.getName().getString(), e.getMessage());
                }
            } else {
                ImagemodClient.LOGGER.debug("{} - rejected: filter-block", block.getName().getString());
            }
        }

        ImagemodClient.LOGGER.info("Block list update complete.");
    }

    private static int create_execute(
            CommandContext<FabricClientCommandSource> ctx
    ) throws CommandSyntaxException {

        // Load source image
        ResizeableImage image = null;
        int w = 0, h = 0;
        boolean width_set = false, height_set = false;
        try {
            w = IntegerArgumentType.getInteger(ctx, "width");
            width_set = true;
        } catch (Exception e) {}

        try {
            h = IntegerArgumentType.getInteger(ctx, "height");
            height_set = true;
        } catch (Exception e) {}

        Path path = PathArgument.getPath(ctx, "src");

        try {
            image = new ResizeableImage(ImageIO.read(path.toFile()));
        } catch (Exception e) {
            ImagemodClient.LOGGER.error(e.getMessage()); // TODO: throw command syntax errors
        }

        if (!width_set && !height_set) {
            return -1;
        } else if (!width_set) {
            w = (h * image.width) / image.height;
        } else if (!height_set) {
            h = (w * image.height) / image.width;
        }

        // World in which the command was sent
        FabricClientCommandSource source = ctx.getSource();
        Entity entity = source.getEntity();
        ClientWorld world = source.getWorld();

        // Get relative directions and positions
        // to entity for placing image blocks at the
        // right place
        Direction viewDir = entity.getFacing();
        Direction rightDir = viewDir.rotateYClockwise();
        BlockPos startPos = entity.getBlockPos().offset(viewDir, 2);

        // Feels like dumb solution but it works
        final int ww = w;
        final int hh = h;

        // Set creation data needed for creation of an image
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
            source.sendError(Text.literal(e.getMessage()));
        };
        creationData.onSuccess = (transform) -> {
            source.sendFeedback(Text.literal(String.format("Successfully created (%dx%d) image", ww, hh)));
            transform.performAction();
            ImageCommand.undo_stack.push(transform);
        };

        // Create image
        new BlockImageBuilder(creationData, ImageCommand.image_blocks);

        return 1;
    }

    // Reload block list
    private static int reload_execute(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
        ImageCommand.update_block_list();
        ctx.getSource().sendFeedback(Text.literal("Reloaded block list"));
        return 1;
    }

    // Undo an image create
    private static int undo_execute(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
        if (ImageCommand.undo_stack.empty()) {
            ctx.getSource().sendError(Text.literal("Undo stack is empty"));
            return -1;
        }
        WorldTransformAction action = ImageCommand.undo_stack.pop();;
        action.revertAction();
        ImageCommand.redo_stack.push(action);
        ctx.getSource().sendFeedback(Text.literal("Successfully reverted last image creation"));
        return 1;
    }

    // Redo an undone image create
    private static int redo_execute(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
       if (ImageCommand.redo_stack.empty()) {
            ctx.getSource().sendError(Text.literal("Redo stack is empty"));
            return -1;
        }
        WorldTransformAction action = ImageCommand.redo_stack.pop();
        action.performAction();
        ImageCommand.undo_stack.push(action);
        ctx.getSource().sendFeedback(Text.literal("Successfully recreated image"));
        return 1;
    }

    // Set the directory where the image create command looks for image files
    private static int set_directory_execute(
            CommandContext<FabricClientCommandSource> ctx
    ) {
        Path dir = DirectoryArgument.getPath(ctx, "dir");
        try {
            PathArgument.setRootDirectory(dir);
            ctx.getSource().sendFeedback(Text.literal("Successfully set image directory to \"" + dir.toString() + "\""));
            return 1;
        } catch (NotDirectoryException e) {
            ctx.getSource().sendError(Text.literal("Provided path is not a directory"));
        } catch (FileNotFoundException e) {
            ctx.getSource().sendError(Text.literal("Provided path could not be found"));
        }
        return -1;
    }

    // Register command structure
    public static void register(
            CommandDispatcher<FabricClientCommandSource> dispatcher,
            CommandRegistryAccess access
    ) {

        ImagemodClient.LOGGER.info("Registering Commands");

        // update block list when we are registering command
        ImageCommand.update_block_list();

        // image command
        LiteralArgumentBuilder<FabricClientCommandSource> root = ClientCommandManager.literal("image");

        // create option
        LiteralArgumentBuilder<FabricClientCommandSource> createLiteral = ClientCommandManager.literal("create");

        LiteralArgumentBuilder<FabricClientCommandSource> wLiteral  	= ClientCommandManager.literal("-width");
        LiteralArgumentBuilder<FabricClientCommandSource> hLiteral  	= ClientCommandManager.literal("-height");
        LiteralArgumentBuilder<FabricClientCommandSource> whInfoLiteral = ClientCommandManager.literal("~ ~");

        RequiredArgumentBuilder<FabricClientCommandSource, Path> srcArgument = ClientCommandManager.argument("src", src_arg);

        RequiredArgumentBuilder<FabricClientCommandSource, Integer> wArgument      = ClientCommandManager.argument("width", IntegerArgumentType.integer());
        RequiredArgumentBuilder<FabricClientCommandSource, Integer> wArgumentFinal = ClientCommandManager.argument("width", IntegerArgumentType.integer()).executes(ImageCommand::create_execute);
        RequiredArgumentBuilder<FabricClientCommandSource, Integer> hArgumentFinal = ClientCommandManager.argument("height", IntegerArgumentType.integer()).executes(ImageCommand::create_execute);

        wLiteral.then(wArgumentFinal);
        hLiteral.then(hArgumentFinal);
        wArgument.then(hArgumentFinal);

        srcArgument.then(wLiteral).then(hLiteral).then(whInfoLiteral).then(wArgument);
        createLiteral.then(srcArgument);

        // Reload option
        LiteralArgumentBuilder<FabricClientCommandSource> reload = ClientCommandManager.literal("reload").executes(ImageCommand::reload_execute);

        // Undo
        LiteralArgumentBuilder<FabricClientCommandSource> undoLiteral = ClientCommandManager.literal("undo").executes(ImageCommand::undo_execute);

        // Redo
        LiteralArgumentBuilder<FabricClientCommandSource> redoLiteral = ClientCommandManager.literal("redo").executes(ImageCommand::redo_execute);;

        // SetDirectory
        LiteralArgumentBuilder<FabricClientCommandSource> setDirectoryLiteral = ClientCommandManager.literal("setDirectory");
        RequiredArgumentBuilder<FabricClientCommandSource, Path> directoryArgument  = ClientCommandManager.argument("dir", dir_arg).executes(ImageCommand::set_directory_execute);

        setDirectoryLiteral.then(directoryArgument);

        // Root relations
        root.then(createLiteral).then(setDirectoryLiteral).then(reload).then(undoLiteral).then(redoLiteral);
        dispatcher.register(root);

        ImagemodClient.LOGGER.info("Commands Registered");

    }

}
