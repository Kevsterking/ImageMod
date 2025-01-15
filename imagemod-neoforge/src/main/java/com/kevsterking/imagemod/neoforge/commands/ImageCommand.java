package com.kevsterking.imagemod.neoforge.commands;

import com.kevsterking.imagemod.neoforge.ImageBuilder.BlockImageBuilder;
import com.kevsterking.imagemod.neoforge.ImageBuilder.BlockImageCreationData;
import com.kevsterking.imagemod.neoforge.ImageBuilder.ImageBlock;
import com.kevsterking.imagemod.neoforge.ImageBuilder.ResizeableImage;
import com.kevsterking.imagemod.neoforge.ImagemodClient;
import com.kevsterking.imagemod.neoforge.WorldTransformer.WorldTransformAction;
import com.kevsterking.imagemod.neoforge.util.DirectoryArgument;
import com.kevsterking.imagemod.neoforge.util.ImageFileArgument;
import com.kevsterking.imagemod.neoforge.util.PathArgument;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;

import javax.imageio.ImageIO;
import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CoralBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.data.ModelData;

public class ImageCommand {

    private static final PathArgument src_arg = new ImageFileArgument();
    private static final PathArgument dir_arg = new DirectoryArgument();
    private static final ArrayList<ImageBlock> image_blocks = new ArrayList<ImageBlock>();
    private static final Stack<WorldTransformAction> undo_stack = new Stack<>();
    private static final Stack<WorldTransformAction> redo_stack = new Stack<>();

    // Filter out unwanted blocks based on properties that
    // create unwanted visual differences
    // true -> keep block, false -> don't use block
    private static boolean filter_block(Block block) {
        BlockState state = block.defaultBlockState();
        VoxelShape vs = state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        return (Block.isShapeFullBlock(vs) &&
            !block.hasDynamicShape() &&
            state.getLightEmission(EmptyBlockGetter.INSTANCE, BlockPos.ZERO) == 0 &&
            !(block instanceof CoralBlock) &&
            !(block instanceof LeavesBlock));
    }

    private static ResourceLocation get_resource_location(Block block) {

        BlockState state = block.defaultBlockState();
        ModelResourceLocation mrl = BlockModelShaper.stateToModelLocation(state);

        TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getModel(mrl).getParticleIcon(ModelData.EMPTY);
        ResourceLocation block_id = sprite.atlasLocation();

        String path = sprite.contents().name().getPath();

        return ResourceLocation.fromNamespaceAndPath(block_id.getNamespace(), "textures/" + path + ".png");

    }

    // Load textured resource for block into ImageBlock
    private static ImageBlock get_image_block(Block block, Direction dir) throws Exception {

        BlockState state = block.defaultBlockState();

        ResourceManager resource_manager = Minecraft.getInstance().getResourceManager();
        ResourceLocation texture_id = get_resource_location(block);

        if (resource_manager.getResource(texture_id).isEmpty()) {
            throw new Exception("No resource available for block");
        }

        return new ImageBlock(state, new ResizeableImage(
            ImageIO.read(resource_manager.getResource(texture_id).get().open()))
        );

    }

    // Update our locally stored block list
    // Filter out bad block types and assign
    public static void update_block_list() {

        ImagemodClient.LOGGER.info("Loading blocks.");

        List<Block> blocks = BuiltInRegistries.BLOCK.stream().toList();
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
            CommandContext<CommandSourceStack> ctx
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

        Path path = PathArgument.get_path(ctx, "src");

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
        CommandSourceStack source = ctx.getSource();
        Entity entity = source.getEntity();
        Level world = source.getUnsidedLevel();

        if (entity == null) return -1;

        // Get relative directions and positions
        // to entity for placing image blocks at the
        // right place

        Direction viewDir = entity.getDirection();
        Direction rightDir = viewDir.getClockWise();
        BlockPos startPos = entity.blockPosition().relative(viewDir, 2);

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
        creationData.zDir		     = viewDir;
        creationData.blockWidth  = w;
        creationData.blockHeight = h;
        creationData.onError = (e) -> {
            source.sendFailure(Component.literal(e.getMessage()));
        };
        creationData.onSuccess = (transform) -> {
            source.sendSuccess(() -> Component.literal(String.format("Successfully created (%dx%d) image", ww, hh)), false);
            transform.performAction();
            ImageCommand.undo_stack.push(transform);
        };

        // Create image
        new BlockImageBuilder(creationData, ImageCommand.image_blocks);

        return 1;
    }

    // Reload block list
    private static int reload_execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ImageCommand.update_block_list();
        ctx.getSource().sendSuccess(() -> Component.literal("Reloaded block list"), false);
        return 1;
    }

    // Undo an image create
    private static int undo_execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (ImageCommand.undo_stack.empty()) {
            ctx.getSource().sendFailure(Component.literal("Undo stack is empty"));
            return -1;
        }
        WorldTransformAction action = ImageCommand.undo_stack.pop();;
        action.revertAction();
        ImageCommand.redo_stack.push(action);
        ctx.getSource().sendSuccess(() -> Component.literal("Successfully reverted last image creation"), false);
        return 1;
    }

    // Redo an undone image create
    private static int redo_execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
       if (ImageCommand.redo_stack.empty()) {
            ctx.getSource().sendFailure(Component.literal("Redo stack is empty"));
            return -1;
        }
        WorldTransformAction action = ImageCommand.redo_stack.pop();
        action.performAction();
        ImageCommand.undo_stack.push(action);
        ctx.getSource().sendSuccess(() -> Component.literal("Successfully recreated image"), false);
        return 1;
    }

    // Set the directory where the image create command looks for image files
    private static int set_directory_execute(
            CommandContext<CommandSourceStack> ctx
    ) {
        Path dir = DirectoryArgument.get_path(ctx, "dir");
        try {
            PathArgument.set_root_directory(String.valueOf(dir));
            ctx.getSource().sendSuccess(() -> Component.literal("Successfully set image directory to \"" + dir.toString() + "\""), false);
            return 1;
        } catch (NotDirectoryException e) {
            ctx.getSource().sendFailure(Component.literal("Provided path is not a directory"));
        } catch (FileNotFoundException e) {
            ctx.getSource().sendFailure(Component.literal("Provided path could not be found"));
        }
        return -1;
    }

    // Register command structure
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        ImagemodClient.LOGGER.info("Registering Commands");

        // update block list when we are registering command
        ImageCommand.update_block_list();

        // image command
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("image");

        // create option
        LiteralArgumentBuilder<CommandSourceStack> createLiteral = Commands.literal("create");

        LiteralArgumentBuilder<CommandSourceStack> wLiteral  	= Commands.literal("-width");
        LiteralArgumentBuilder<CommandSourceStack> hLiteral  	= Commands.literal("-height");
        LiteralArgumentBuilder<CommandSourceStack> whInfoLiteral = Commands.literal("~ ~");

        RequiredArgumentBuilder<CommandSourceStack, Path> srcArgument = Commands.argument("src", src_arg);

        RequiredArgumentBuilder<CommandSourceStack, Integer> wArgument      = Commands.argument("width", IntegerArgumentType.integer());
        RequiredArgumentBuilder<CommandSourceStack, Integer> wArgumentFinal = Commands.argument("width", IntegerArgumentType.integer()).executes(ImageCommand::create_execute);
        RequiredArgumentBuilder<CommandSourceStack, Integer> hArgumentFinal = Commands.argument("height", IntegerArgumentType.integer()).executes(ImageCommand::create_execute);

        wLiteral.then(wArgumentFinal);
        hLiteral.then(hArgumentFinal);
        wArgument.then(hArgumentFinal);

        srcArgument.then(wLiteral).then(hLiteral).then(whInfoLiteral).then(wArgument);
        createLiteral.then(srcArgument);

        // Reload option
        LiteralArgumentBuilder<CommandSourceStack> reload = Commands.literal("reload").executes(ImageCommand::reload_execute);

        // Undo
        LiteralArgumentBuilder<CommandSourceStack> undoLiteral = Commands.literal("undo").executes(ImageCommand::undo_execute);

        // Redo
        LiteralArgumentBuilder<CommandSourceStack> redoLiteral = Commands.literal("redo").executes(ImageCommand::redo_execute);;

        // SetDirectory
        LiteralArgumentBuilder<CommandSourceStack> setDirectoryLiteral = Commands.literal("setDirectory");
        RequiredArgumentBuilder<CommandSourceStack, Path> directoryArgument  = Commands.argument("dir", dir_arg).executes(ImageCommand::set_directory_execute);

        setDirectoryLiteral.then(directoryArgument);

        // Root relations
        root.then(createLiteral).then(setDirectoryLiteral).then(reload).then(undoLiteral).then(redoLiteral);
        dispatcher.register(root);

        ImagemodClient.LOGGER.info("Commands Registered");

    }

}
