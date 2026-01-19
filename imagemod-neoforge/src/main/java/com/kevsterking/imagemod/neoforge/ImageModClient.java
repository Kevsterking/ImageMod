package com.kevsterking.imagemod.neoforge;

import com.kevsterking.imagemod.core.commands.ImageCommand;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(ImageModClient.MOD_ID)
public class ImageModClient {

	public static final String MOD_ID = "imagemod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public ImageModNeoForgeImplementation mod_implementation = new ImageModNeoForgeImplementation();
	public ImageCommand<@NotNull CommandSourceStack> image_command;

	public ImageModClient() {
		ImageModClient.LOGGER.info("Initializing...");
		this.image_command = new ImageCommand<@NotNull CommandSourceStack>(this.mod_implementation);
		NeoForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
  private void registerClientCommands(RegisterClientCommandsEvent event) {
    this.image_command.register(event.getDispatcher(), null);
  }

}