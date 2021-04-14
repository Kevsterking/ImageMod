package ImageMod;

import ImageMod.commands.ImageCommand;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
