package com.kevsterking.imagemod.fabric;

import com.kevsterking.imagemod.core.ImageModCore;
import com.kevsterking.imagemod.core.commands.ImageCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.jetbrains.annotations.NotNull;

public class ImageModFabric implements ClientModInitializer {

	private ImageModCommandInterfaceFabric fabric_implementation = new ImageModCommandInterfaceFabric();
	private ImageCommand<@NotNull FabricClientCommandSource> image_command;

	@Override
	public void onInitializeClient() {
		ImageModCore.LOGGER.info("Initializing...");
		this.image_command = new ImageCommand<@NotNull FabricClientCommandSource>(this.fabric_implementation);
		ClientCommandRegistrationCallback.EVENT.register(this.image_command::register);
	}

}