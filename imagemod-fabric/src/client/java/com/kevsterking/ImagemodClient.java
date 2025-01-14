package com.kevsterking;

import com.kevsterking.commands.ImageCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImagemodClient implements ClientModInitializer {

	public static final String MOD_ID = "imagemod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {

		LOGGER.info("Initializing");

		// Register commands
		ClientCommandRegistrationCallback.EVENT.register(ImageCommand::register);

		LOGGER.info("Initialized");

	}

}