package com.kevsterking;

import com.kevsterking.commands.ImageCommand;
import com.kevsterking.util.PathArgument;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class ImagemodClient implements ClientModInitializer {

	public static final String MOD_ID = "imagemod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {

		LOGGER.info("Initializing");

		try {
			PathArgument.setRootDirectory(Path.of(System.getProperty("user.home") + "/Downloads"));
		} catch (Exception e) {
			LOGGER.error("Could not set default location to downloads folder");
		}

		// Register commands
		ClientCommandRegistrationCallback.EVENT.register(ImageCommand::register);

		LOGGER.info("Initialized");

	}

}