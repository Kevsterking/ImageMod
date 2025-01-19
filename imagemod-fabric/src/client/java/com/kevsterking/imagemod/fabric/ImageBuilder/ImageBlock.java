package com.kevsterking.imagemod.fabric.ImageBuilder;

import com.kevsterking.imagemod.fabric.ImageModClient;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ImageBlock {

  public BlockState state;
  public BufferedImage image;

  private static BufferedImage get_texture(BlockState state) throws IOException {
    // Black magic, don't question it
    MinecraftClient client = MinecraftClient.getInstance();
    ResourceManager resource_manager = client.getResourceManager();
    BakedModel model = client.getBakedModelManager().getBlockModels().getModel(state);
    Sprite sprite = model.getParticleSprite();
    Identifier block_id = sprite.getAtlasId();
    String path = sprite.getContents().getId().getPath();
    Identifier location = Identifier.of(block_id.getNamespace(), "textures/" + path + ".png");
    ImageModClient.LOGGER.debug(location.getPath());
    return ImageIO.read(resource_manager.getResource(location).get().getInputStream());
  }

  public static ImageBlock get(Block block) throws IOException {
    ImageBlock ret = new ImageBlock();
    ret.state = block.getDefaultState();
    ret.image = ImageBlock.get_texture(ret.state);
    return ret;
  }

}
