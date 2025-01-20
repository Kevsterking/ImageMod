package com.kevsterking.imagemod.neoforge.ImageBuilder;


import com.kevsterking.imagemod.neoforge.ImageModClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CoralBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.model.data.ModelData;

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
    ret.state = block.defaultBlockState();
    ret.image = ImageBlock.get_texture(ret.state);
    return ret;
  }

  // Filter out unwanted blocks based on properties that
  // create unwanted effects
  // true -> keep block, false -> don't use block
  public static boolean filter_block(Block block) throws Exception {
    BlockState state = block.defaultBlockState();
    VoxelShape vs = state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
    if (!Block.isShapeFullBlock(vs)) throw new Exception("Is not full block");
    if (block.hasDynamicShape()) throw new Exception("Has dynamic shape");
    if (state.getLightEmission(EmptyBlockGetter.INSTANCE, BlockPos.ZERO) != 0) throw new Exception("Emits light");
    if (state.hasProperty(BlockStateProperties.FACING) || state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) throw new Exception("Directional block");if (block instanceof CoralBlock) throw new Exception("Is coral block");
    if (block instanceof LeavesBlock) throw new Exception("Is leaves block");
    if (block_blacklist.contains(block)) throw new Exception("Is blacklisted");
    return true;
  }

  private static BufferedImage get_texture(BlockState state) throws IOException {
    // Black magic, don't question it
    ResourceManager resource_manager = Minecraft.getInstance().getResourceManager();
    ModelResourceLocation mrl = BlockModelShaper.stateToModelLocation(state);
    TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getModel(mrl).getParticleIcon(ModelData.EMPTY);
    ResourceLocation block_id = sprite.atlasLocation();
    String path = sprite.contents().name().getPath();
    ResourceLocation location = ResourceLocation.fromNamespaceAndPath(block_id.getNamespace(), "textures/" + path + ".png");
    ImageModClient.LOGGER.debug(location.getPath());
    return ImageIO.read(resource_manager.getResource(location).get().open());
  }

}
