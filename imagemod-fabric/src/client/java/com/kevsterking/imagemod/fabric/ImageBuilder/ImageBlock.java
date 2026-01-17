package com.kevsterking.imagemod.fabric.ImageBuilder;

import com.kevsterking.imagemod.fabric.ImageModClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CoralBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
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
    if (!state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)) throw new Exception("Is not full block");
    if (block.hasDynamicShape()) throw new Exception("Has dynamic shape");
    if (state.getLightEmission() != 0) throw new Exception("Emits light");
    if (state.hasProperty(BlockStateProperties.FACING) || state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) throw new Exception("Directional block");
    if (block instanceof CoralBlock) throw new Exception("Is coral block");
    if (block instanceof LeavesBlock) throw new Exception("Is leaves block");
    if (block_blacklist.contains(block)) throw new Exception("Is blacklisted");
    return true;
  }

  private static BufferedImage get_texture(BlockState state) throws IOException {
    // Black magic, don't question it
    Minecraft mc = Minecraft.getInstance();
    if (mc.level == null) throw new IOException("World level is null");
    BlockModelShaper bms = mc.getModelManager().getBlockModelShaper();
    TextureAtlasSprite sprite = bms.getParticleIcon(state);
    Identifier location = sprite.contents().name().withPrefix("textures/").withSuffix(".png");
    Resource resource = mc.getResourceManager().getResource(location)
      .orElseThrow(() -> new IOException("Resource not found: " + location));
    ImageModClient.LOGGER.debug(location.getPath());
    try (InputStream is = resource.open()) {
        return ImageIO.read(is);
    } catch (Exception e) {
      throw new IOException("reading " + location + " failed: " + e.getMessage());
    }
  }

}
