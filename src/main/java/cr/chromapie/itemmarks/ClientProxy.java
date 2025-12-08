package cr.chromapie.itemmarks;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cr.chromapie.itemmarks.core.MarkConfig;
import cr.chromapie.itemmarks.core.MarkRegistry;
import cr.chromapie.itemmarks.gui.GuiKeyHandler;

public class ClientProxy extends CommonProxy {

    public static final KeyBinding KEY_OPEN_GUI = new KeyBinding(
        "key.itemmarks.opengui",
        Keyboard.KEY_M,
        "key.categories.itemmarks");

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        MarkRegistry.load(event.getModConfigurationDirectory());
        MarkConfig.load(event.getModConfigurationDirectory());
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        // Mark rendering is handled by MixinRenderItem
        FMLCommonHandler.instance()
            .bus()
            .register(new GuiKeyHandler());
        ClientRegistry.registerKeyBinding(KEY_OPEN_GUI);
    }
}
