package com.kevsterking.imagemod;

import com.kevsterking.imagemod.commands.ImageCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ImageMod.modid)
public class ImageMod {

    public static final String modid = "imagemod";

    private static final Logger LOGGER = LogManager.getLogger();
    private static IResourceManager resourceManger = Minecraft.getInstance().getResourceManager();

    public ImageMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);
        bus.addListener(this::clientSided);
        bus.addListener(this::loadComplete);
        MinecraftForge.EVENT_BUS.register(ImageModEventHandler.class);
    }

    /*
     * Get mod resource manager
     * */
    public static IResourceManager getResourceManger() {
        return ImageMod.resourceManger;
    }

    /*
    * Setup the mod
    * */
    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("com.kevsterking.ImageMod is set up.");
    }

    /*
    * Client sided mod action
    * */
    private void clientSided(final FMLClientSetupEvent event) {
        LOGGER.info("Client side set up.");
    }

    /*
    * Load completed
    * */
    private void loadComplete(final FMLLoadCompleteEvent event) {
        ImageCommand.reload();
        LOGGER.info("Load completed!");
    }

}
