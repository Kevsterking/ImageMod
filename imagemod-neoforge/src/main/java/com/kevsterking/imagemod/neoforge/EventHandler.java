package com.kevsterking.imagemod.neoforge;

import com.kevsterking.imagemod.neoforge.commands.ImageCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid=ImageMod.MOD_ID)
public class EventHandler {
  @SubscribeEvent
  public static void registerCommands(RegisterCommandsEvent event) {
    ImageMod.LOGGER.info("Initializing");
    ImageCommand.register(event.getDispatcher());
    ImageMod.LOGGER.info("Initialized");
  }
}
