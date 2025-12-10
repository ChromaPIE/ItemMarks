package cr.chromapie.itemmarks.gui;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.SecondaryPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.TextWidget;

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
        SecondaryPanel.IPanelBuilder builder = (parentPanel, player) -> panelSupplier.get();
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

    /**
     * Calculate context menu position, clamping to screen bounds.
     *
     * @param mouseX  Mouse X position
     * @param mouseY  Mouse Y position
     * @param width   Context menu width
     * @param height  Context menu height
     * @param screenW Screen width
     * @param screenH Screen height
     * @return int array with [x, y] position
     */
    public static int[] clampToScreen(int mouseX, int mouseY, int width, int height, int screenW, int screenH) {
        int x = mouseX;
        int y = mouseY;
        if (x + width > screenW) x = screenW - width;
        if (y + height > screenH) y = screenH - height;
        return new int[] { x, y };
    }

    /**
     * Position a context menu at mouse location, clamped to screen.
     *
     * @param panel  The panel to position
     * @param mouseX Mouse X position
     * @param mouseY Mouse Y position
     * @param width  Panel width
     * @param height Panel height
     * @param screen The screen to get bounds from
     */
    public static void positionContextMenu(ModularPanel panel, int mouseX, int mouseY, int width, int height,
        ModularScreen screen) {
        Area area = screen.getScreenArea();
        int[] pos = clampToScreen(mouseX, mouseY, width, height, area.width, area.height);
        panel.leftRel(0)
            .topRel(0)
            .left(pos[0])
            .top(pos[1]);
    }

    /**
     * Create a centered title widget for a panel.
     *
     * @param langKey Language key for the title
     * @param width   Panel width for centering
     * @param y       Y position
     * @return Configured TextWidget
     */
    public static TextWidget createTitle(String langKey, int width, int y) {
        TextWidget title = new TextWidget(IKey.lang(langKey));
        title.alignment(Alignment.Center);
        title.pos(0, y);
        title.size(width, 14);
        title.shadow(false);
        return title;
    }

    /**
     * Create a context menu button.
     *
     * @param langKey Language key for button text
     * @param x       X position
     * @param y       Y position
     * @param width   Button width
     * @param height  Button height
     * @param onClick Click action
     * @return Configured ButtonWidget
     */
    public static ButtonWidget<?> createContextMenuButton(String langKey, int x, int y, int width, int height,
        Runnable onClick) {
        ButtonWidget<?> btn = new ButtonWidget<>();
        btn.pos(x, y);
        btn.size(width, height);
        btn.overlay(IKey.lang(langKey));
        btn.onMousePressed(b -> {
            onClick.run();
            return true;
        });
        return btn;
    }
}
