package cr.chromapie.itemmarks.gui;

import net.minecraft.client.Minecraft;

import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cr.chromapie.itemmarks.ClientProxy;

public class GuiKeyHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (ClientProxy.KEY_OPEN_GUI.isPressed()) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.currentScreen == null) {
                ModularScreen screen = new GuiMarkManagerMui();
                UISettings settings = new UISettings();
                screen.getContext()
                    .setSettings(settings);
                mc.displayGuiScreen(new GuiScreenWrapper(screen));
            }
        }
    }
}
