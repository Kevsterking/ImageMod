package com.kevsterking.imagemod.fabric;

import com.kevsterking.imagemod.core.commands.ImageCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageModClient implements ClientModInitializer {

	public static final String MOD_ID = "imagemod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public ImageModFabricImplementation fabric_implementation = new ImageModFabricImplementation();
	public ImageCommand<@NotNull FabricClientCommandSource> image_command;

	@Override
	public void onInitializeClient() {
		ImageModClient.LOGGER.info("Initializing...");
		this.image_command = new ImageCommand<@NotNull FabricClientCommandSource>(this.fabric_implementation);
		ClientCommandRegistrationCallback.EVENT.register(this.image_command::register);
	}

}