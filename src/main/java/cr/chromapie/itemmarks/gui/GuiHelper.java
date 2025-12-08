package cr.chromapie.itemmarks.gui;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.SecondaryPanel;
import com.cleanroommc.modularui.widgets.ButtonWidget;

/**
 * Utility methods for ModularUI GUI building.
 */
public final class GuiHelper {

    private GuiHelper() {}

    /**
     * Apply enabled/disabled button styling.
     * Enabled: normal button texture with hover.
     * Disabled: disabled texture, no hover.
     */
    public static void applyButtonStyle(ButtonWidget<?> btn, boolean enabled) {
        if (enabled) {
            btn.background(GuiTextures.MC_BUTTON);
            btn.hoverBackground(GuiTextures.MC_BUTTON_HOVERED);
        } else {
            btn.background(GuiTextures.MC_BUTTON_DISABLED);
            btn.disableHoverBackground();
        }
    }

    /**
     * Open a secondary panel from a parent panel.
     * 
     * @param parent        The parent panel to attach to
     * @param panelSupplier Supplier that creates the new panel
     */
    public static void openPanel(ModularPanel parent, Supplier<ModularPanel> panelSupplier) {
        if (parent == null) return;
        SecondaryPanel.IPanelBuilder builder = new SecondaryPanel.IPanelBuilder() {

            @Override
            public ModularPanel build(ModularPanel parentPanel, EntityPlayer player) {
                return panelSupplier.get();
            }
        };
        IPanelHandler handler = IPanelHandler.simple(parent, builder, true);
        handler.openPanel();
    }

    /**
     * Get the item currently held by the player.
     * 
     * @return The held ItemStack, or null if player is null or not holding anything
     */
    public static ItemStack getHeldItem() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return null;
        return mc.thePlayer.getHeldItem();
    }

    /**
     * Check if the held item has NBT data.
     * 
     * @return true if player is holding an item with NBT compound
     */
    public static boolean heldItemHasNbt() {
        ItemStack held = getHeldItem();
        return held != null && held.getTagCompound() != null;
    }

    /**
     * Check if the held item is valid (not null and has an item).
     * 
     * @return true if player is holding a valid item
     */
    public static boolean hasHeldItem() {
        ItemStack held = getHeldItem();
        return held != null && held.getItem() != null;
    }
}
