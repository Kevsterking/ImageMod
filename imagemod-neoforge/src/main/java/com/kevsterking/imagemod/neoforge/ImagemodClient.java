package com.kevsterking.imagemod.neoforge;

import com.kevsterking.imagemod.neoforge.commands.ImageCommand;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ImagemodClient.MOD_ID)
public class ImagemodClient {

  public static final String MOD_ID = "imagemod";
  public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

  public ImagemodClient() {
    IEventBus modEventBus = NeoForge.EVENT_BUS;
    modEventBus.register(this);
  }

  @SubscribeEvent
  private void registerClientCommands(RegisterClientCommandsEvent event) {
    LOGGER.info("Initializing");
    ImageCommand.register(event.getDispatcher());
    LOGGER.info("Initialized");
  }

}