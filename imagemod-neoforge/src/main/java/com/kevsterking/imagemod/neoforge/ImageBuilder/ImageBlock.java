package com.kevsterking.imagemod.neoforge.ImageBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ImageBlock {

  public BlockState state;
  public BufferedImage image;

  private static BufferedImage get_texture(BlockState state) throws IOException {
    // Black magic, don't question it
    ResourceManager resource_manager = Minecraft.getInstance().getResourceManager();
    ModelResourceLocation mrl = BlockModelShaper.stateToModelLocation(state);
    TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getModel(mrl).getParticleIcon(ModelData.EMPTY);
    ResourceLocation block_id = sprite.atlasLocation();
    String path = sprite.contents().name().getPath();
    ResourceLocation location = ResourceLocation.fromNamespaceAndPath(block_id.getNamespace(), "textures/" + path + ".png");
    return ImageIO.read(resource_manager.getResource(location).get().open());
  }

  public static ImageBlock get(Block block) throws IOException {
    ImageBlock ret = new ImageBlock();
    ret.state = block.defaultBlockState();
    ret.image = ImageBlock.get_texture(ret.state);
    return ret;
  }

}
