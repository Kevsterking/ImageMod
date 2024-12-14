package com.kevsterking.imagemod;

import com.kevsterking.imagemod.commands.ImageCommand;
import com.kevsterking.imagemod.util.DirectoryArgument;
import com.kevsterking.imagemod.util.PathArgument;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.server.command.EnumArgument;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ImageMod.MODID)
public class ImageMod {

    public static final String MODID = "imagemod";
    private static final Logger LOGGER = LogManager.getLogger();

    @OnlyIn(Dist.CLIENT)
    private static ResourceManager resourceManger;

    private static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(BuiltInRegistries.COMMAND_ARGUMENT_TYPE, ImageMod.MODID);

    private static final Holder<ArgumentTypeInfo<?, ?>> PATH_COMMAND_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES.register("path", () -> ArgumentTypeInfos.registerByClass(PathArgument.class, SingletonArgumentInfo.contextFree(PathArgument::new)));
    private static final Holder<ArgumentTypeInfo<?, ?>> DIRECTORY_COMMAND_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES.register("directory", () -> ArgumentTypeInfos.registerByClass(DirectoryArgument.class, SingletonArgumentInfo.contextFree(DirectoryArgument::new)));

    public ImageMod(IEventBus modEventBus, ModContainer modContainer) {

        boolean clientSide = true;

        resourceManger = Minecraft.getInstance().getResourceManager();

    	if (clientSide) {

            COMMAND_ARGUMENT_TYPES.register(modEventBus);

            modEventBus.addListener(this::onSetup);
            modEventBus.addListener(this::onLoadComplete);
            NeoForge.EVENT_BUS.addListener(this::onRegisterCommandsEvent);

        }

    }

    private void onSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("ImageMod is set up.");
    }

    private void onLoadComplete(final FMLLoadCompleteEvent event) {
        ImageCommand.reload();
        LOGGER.info("Load completed!");
    }

    // Get mod resource manager
    @OnlyIn(Dist.CLIENT)
    public static ResourceManager getResourceManger() {
        return ImageMod.resourceManger;
    }

    @SubscribeEvent
    public void onRegisterCommandsEvent(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> commandDispatcher = event.getDispatcher();
        ImageCommand.register(commandDispatcher);
        LOGGER.info("Image command registered");
    }

}
