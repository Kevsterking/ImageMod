package ImageMod;

import ImageMod.commands.ImageCommand;
import ImageMod.util.DirectoryArgument;
import ImageMod.util.PathArgument;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ImageMod.modid)
public class ImageMod {

    public static final String modid = "imagemod";
    private static final Logger LOGGER = LogManager.getLogger();


    private static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, ImageMod.modid);

    private static final RegistryObject<SingletonArgumentInfo<PathArgument>> PATH_ARGUMENT = ARGUMENT_TYPES.register(
            "pathargument",
            () -> ArgumentTypeInfos.registerByClass(PathArgument.class, SingletonArgumentInfo.contextFree(PathArgument::new))
    );

    private static final RegistryObject<SingletonArgumentInfo<DirectoryArgument>> DIRECTORY_ARGUMENT = ARGUMENT_TYPES.register(
            "directoryargument",
            () -> ArgumentTypeInfos.registerByClass(DirectoryArgument.class, SingletonArgumentInfo.contextFree(DirectoryArgument::new))
    );

    @OnlyIn(Dist.CLIENT)
    private static ResourceManager resourceManger;

    public ImageMod() {

        boolean clientSide = true;

        try {
    		resourceManger = Minecraft.getInstance().getResourceManager();
    	} catch(Exception e) {
    		clientSide = false;
    	}
    	
    	IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        
    	if (clientSide) {
    	    bus.addListener(this::setup);
            bus.addListener(this::clientSetup);
            bus.addListener(this::loadComplete);
            MinecraftForge.EVENT_BUS.register(ImageModEventHandler.class);
            ARGUMENT_TYPES.register(bus);
        } else {
        }
    }

    /*
     * Get mod resource manager
     * */
    @OnlyIn(Dist.CLIENT)
    public static ResourceManager getResourceManger() {
        return ImageMod.resourceManger;
    }

    /*
    * Setup the mod
    * */
    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("ImageMod is set up.");
    }

    /*
    * Client sided mod action
    * */
    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("ImageMod Client set up.");
    }

    private void loadComplete(final FMLLoadCompleteEvent event) {
        ImageCommand.reload();
        LOGGER.info("Load completed!");
    }
}
