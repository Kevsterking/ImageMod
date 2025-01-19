package com.kevsterking.imagemod.neoforge;

import com.kevsterking.imagemod.neoforge.commands.ImageCommand;

import com.kevsterking.imagemod.neoforge.util.DirectoryArgument;
import com.kevsterking.imagemod.neoforge.util.ImageFileArgument;
import com.kevsterking.imagemod.neoforge.util.PathArgument;
import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLanguageProvider;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ImageMod.MOD_ID)
public class ImageMod {

  public static final String MOD_ID = "imagemod";
  public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

  private static final DeferredRegister<ArgumentTypeInfo<?,?>> ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, ImageMod.MOD_ID);

  public ImageMod(IEventBus modBus) {

    ARGUMENT_TYPES.register(
      "path_argument",
      () -> ArgumentTypeInfos.registerByClass(PathArgument.class, SingletonArgumentInfo.contextFree(PathArgument::new))
    );

    ARGUMENT_TYPES.register(
      "directory_argument",
      () -> ArgumentTypeInfos.registerByClass(DirectoryArgument.class, SingletonArgumentInfo.contextFree(DirectoryArgument::new))
    );

    ARGUMENT_TYPES.register(
      "image_file_argument",
      () -> ArgumentTypeInfos.registerByClass(ImageFileArgument.class, SingletonArgumentInfo.contextFree(ImageFileArgument::new))
    );

    IEventBus modEventBus = NeoForge.EVENT_BUS;

    modEventBus.register(EventHandler.class);
    ARGUMENT_TYPES.register(modBus);

  }

}