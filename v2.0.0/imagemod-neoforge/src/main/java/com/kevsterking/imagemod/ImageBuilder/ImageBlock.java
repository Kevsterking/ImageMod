package com.kevsterking.imagemod.ImageBuilder;

import net.minecraft.world.level.block.state.BlockState;

/*
 * Class for storing a state and it's respective
 * texture to use for image creation
 * */
 public class ImageBlock {

     public BlockState blockState;
     public ResizeableImage image;

     public ImageBlock(BlockState blockState, ResizeableImage image) {
         this.blockState  = blockState;
         this.image       = image;
     }
 }