package ImageMod;

import ImageMod.commands.ImageCommand;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.command.CommandSource;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.packs.ResourcePackLoader;

public class ImageModEventHandler {

    /*
    * Register commands
    * */
    @SubscribeEvent
    public static void registerCommandsEvent(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSource> commandDispatcher = event.getDispatcher();
        ImageCommand.register(commandDispatcher);
    }

}
