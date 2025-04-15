package com.kevsterking.imagemod.fabric.ImageBuilder;

import com.kevsterking.imagemod.fabric.ImageModClient;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.resource.ResourceManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.EmptyBlockView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ImageBlock {

  private static final Set<Block> block_blacklist = new HashSet<>();

  public BlockState state;
  public BufferedImage image;

  static {
    block_blacklist.add(Blocks.CARTOGRAPHY_TABLE);
    block_blacklist.add(Blocks.DRIED_KELP_BLOCK);
    block_blacklist.add(Blocks.SCULK_SHRIEKER);
    block_blacklist.add(Blocks.ICE);
    block_blacklist.add(Blocks.BLUE_ICE);
    block_blacklist.add(Blocks.FROSTED_ICE);
    block_blacklist.add(Blocks.PACKED_ICE);
  }

  public static ImageBlock get_air() {
    ImageBlock ret = new ImageBlock();
    BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
    img.setRGB(0, 0, 0x00FFFFFF);
    ret.state = null;
    ret.image = ImageUtil.load(img, 16, 16);
    return ret;
  }

  public static ImageBlock get(Block block) throws IOException {
    ImageBlock ret = new ImageBlock();
    ret.state = block.getDefaultState();
    ret.image = ImageBlock.get_texture(ret.state);
    return ret;
  }

  // Filter out unwanted blocks based on properties that
  // create unwanted effects
  // true -> keep block, false -> don't use block
  public static boolean filter_block(Block block) throws Exception {
    BlockState state = block.getDefaultState();
    VoxelShape vs = state.getCollisionShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
    if (!Block.isShapeFullCube(vs)) throw new Exception("Is not full block");
    if (block.hasDynamicBounds()) throw new Exception("Has dynamic shape");
    if (state.getLuminance() != 0) throw new Exception("Emits light");
    if (state.contains(Properties.FACING) || state.contains(Properties.HORIZONTAL_FACING)) throw new Exception("Directional block");
    if (block instanceof CoralBlockBlock) throw new Exception("Is coral block");
    if (block instanceof LeavesBlock) throw new Exception("Is leaves block");
    if (block_blacklist.contains(block)) throw new Exception("Is blacklisted");
    return true;

  }

  private static BufferedImage get_texture(BlockState state) throws IOException {
    // Black magic, don't question it
    MinecraftClient client = MinecraftClient.getInstance();
    ResourceManager resource_manager = client.getResourceManager();
    BlockStateModel model = client.getBakedModelManager().getBlockModels().getModel(state);
    Sprite sprite = model.particleSprite();
    Identifier block_id = sprite.getAtlasId();
    String path = sprite.getContents().getId().getPath();
    Identifier location = Identifier.of(block_id.getNamespace(), "textures/" + path + ".png");
    ImageModClient.LOGGER.debug(location.getPath());
    return ImageIO.read(resource_manager.getResource(location).get().getInputStream());
  }

}
