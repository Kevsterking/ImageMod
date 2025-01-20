package com.kevsterking.imagemod.fabric;

import com.kevsterking.imagemod.fabric.commands.ImageCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageModClient implements ClientModInitializer {

	public static final String MOD_ID = "imagemod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public ImageCommand image_command;

	@Override
	public void onInitializeClient() {
		ImageModClient.LOGGER.info("Initializing...");
		this.image_command = new ImageCommand();
		ClientCommandRegistrationCallback.EVENT.register(this.image_command::register);
	}

}