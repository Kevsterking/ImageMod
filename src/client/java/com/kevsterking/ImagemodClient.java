package com.kevsterking;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImagemodClient implements ClientModInitializer {

	public static final String MOD_ID = "imagemod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


	@Override
	public void onInitializeClient() {

		LOGGER.info("Initializing");

		// Register the command
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			LOGGER.info("Registering Commands");
			dispatcher.register(ClientCommandManager.literal("image").executes(context -> {
				context.getSource().sendFeedback(Text.literal("Successfully called command"));
				return 1;
			}));
			LOGGER.info("Commands Registered");
		});

		LOGGER.info("Initialized");

	}

}