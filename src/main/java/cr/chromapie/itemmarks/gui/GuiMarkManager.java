package cr.chromapie.itemmarks.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cr.chromapie.itemmarks.ClientProxy;
import cr.chromapie.itemmarks.core.MarkEntry;
import cr.chromapie.itemmarks.core.MarkRegistry;

public class GuiMarkManager extends GuiScreen {

    private GuiTextField fieldMark;
    private GuiTextField fieldItemId;
    private GuiTextField fieldNbtPath;
    private GuiTextField fieldNbtValue;
    private int scrollOffset = 0;
    private int selectedIndex = -1;
    private static final int ENTRY_HEIGHT = 12;
    private static final int LIST_X = 10;
    private static final int LIST_Y = 30;
    private int listHeight;
    private int helpX, helpY;

    private static final List<String> HELP_LINES = new ArrayList<>();
    static {
        HELP_LINES.add("§e=== Item ID ===");
        HELP_LINES.add("modid:item       §7meta=0");
        HELP_LINES.add("modid:item:16    §7meta=16");
        HELP_LINES.add("modid:item:*     §7any meta");
        HELP_LINES.add("");
        HELP_LINES.add("§e=== NBT Path ===");
        HELP_LINES.add("key              §7direct access");
        HELP_LINES.add("key.sub          §7nested");
        HELP_LINES.add("list[0]          §7first element");
        HELP_LINES.add("list[*]          §7any element");
        HELP_LINES.add("§7(empty)         root level");
        HELP_LINES.add("");
        HELP_LINES.add("§e=== NBT Value ===");
        HELP_LINES.add("123              §7exact (auto strip s/b/l)");
        HELP_LINES.add("*                §7field exists");
        HELP_LINES.add("!                §7field not exists");
        HELP_LINES.add("a=1&b=2          §7multi-condition");
        HELP_LINES.add("id=49&lvl=*      §7mixed condition");
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (ClientProxy.KEY_OPEN_GUI.isPressed()) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.currentScreen == null) {
                mc.displayGuiScreen(new GuiMarkManager());
            }
        }
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        listHeight = height - 120;
        int startY = height - 80;
        fieldMark = new GuiTextField(fontRendererObj, 45, startY, 30, 16);
        fieldMark.setMaxStringLength(2);
        fieldItemId = new GuiTextField(fontRendererObj, 45, startY + 20, 110, 16);
        fieldItemId.setMaxStringLength(256);
        fieldNbtPath = new GuiTextField(fontRendererObj, 190, startY, 80, 16);
        fieldNbtPath.setMaxStringLength(256);
        fieldNbtValue = new GuiTextField(fontRendererObj, 190, startY + 20, 80, 16);
        fieldNbtValue.setMaxStringLength(256);
        helpX = 275;
        helpY = startY + 4;
        buttonList.clear();
        buttonList.add(new GuiButton(0, width - 110, startY, 50, 20, "Add"));
        buttonList.add(new GuiButton(1, width - 55, startY, 50, 20, "Delete"));
        buttonList.add(new GuiButton(2, width - 110, startY + 25, 50, 20, "Update"));
        buttonList.add(new GuiButton(3, width - 55, startY + 25, 50, 20, "Clear"));
        buttonList.add(new GuiButton(4, 10, startY + 45, 80, 20, "From Hand"));
        buttonList.add(new GuiButton(5, 95, startY + 45, 60, 20, "NBT..."));
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Item Marks Manager", width / 2, 10, 0xFFFFFF);
        drawRect(LIST_X, LIST_Y, width - 10, LIST_Y + listHeight, 0x80000000);
        List<MarkEntry> entries = MarkRegistry.getEntries();
        int visibleCount = listHeight / ENTRY_HEIGHT;
        for (int i = 0; i < visibleCount && i + scrollOffset < entries.size(); i++) {
            int idx = i + scrollOffset;
            MarkEntry entry = entries.get(idx);
            int y = LIST_Y + i * ENTRY_HEIGHT;
            if (idx == selectedIndex) {
                drawRect(LIST_X, y, width - 10, y + ENTRY_HEIGHT, 0x80FFFFFF);
            }
            String text = String.format("[%s] %s", entry.getMark(), formatItemId(entry));
            if (entry.hasNbtCondition()) {
                String path = entry.getNbtPath();
                if (path == null || path.isEmpty()) {
                    text += String.format(" {%s}", entry.getNbtValue());
                } else {
                    text += String.format(" {%s=%s}", path, entry.getNbtValue());
                }
            }
            fontRendererObj.drawString(text, LIST_X + 2, y + 2, 0xFFFFFF);
        }
        int startY = height - 80;
        fontRendererObj.drawString("Mark:", 10, startY + 4, 0xFFFFFF);
        fontRendererObj.drawString("Item ID:", 10, startY + 24, 0xFFFFFF);
        fontRendererObj.drawString("NBT Path:", 160, startY + 4, 0xAAAAAA);
        fontRendererObj.drawString("NBT Value:", 160, startY + 24, 0xAAAAAA);
        fontRendererObj.drawString("§e[?]", helpX, helpY, 0xFFFF00);
        fieldMark.drawTextBox();
        fieldItemId.drawTextBox();
        fieldNbtPath.drawTextBox();
        fieldNbtValue.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (isMouseOverHelp(mouseX, mouseY)) {
            drawHoveringText(HELP_LINES, mouseX, mouseY, fontRendererObj);
        }
    }

    private boolean isMouseOverHelp(int mouseX, int mouseY) {
        int textWidth = fontRendererObj.getStringWidth("[?]");
        return mouseX >= helpX && mouseX <= helpX + textWidth && mouseY >= helpY && mouseY <= helpY + 10;
    }

    private String formatItemId(MarkEntry entry) {
        if (entry.getMeta() < 0) {
            return entry.getItemId() + ":*";
        } else {
            return entry.getItemId() + ":" + entry.getMeta();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                addEntry();
                break;
            case 1:
                deleteEntry();
                break;
            case 2:
                updateEntry();
                break;
            case 3:
                clearFields();
                break;
            case 4:
                fillFromHand();
                break;
            case 5:
                openNbtEditor();
                break;
        }
    }

    private void openNbtEditor() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;
        ItemStack held = mc.thePlayer.getHeldItem();
        if (held == null || held.getTagCompound() == null) return;
        mc.displayGuiScreen(new GuiNbtEditor(this, held));
    }

    public void setNbtFromEditor(String path, String value) {
        fieldNbtPath.setText(path != null ? path : "");
        fieldNbtValue.setText(value != null ? value : "");
    }

    private void addEntry() {
        String mark = fieldMark.getText()
            .trim();
        String itemIdRaw = fieldItemId.getText()
            .trim();
        if (mark.isEmpty() || itemIdRaw.isEmpty()) return;
        ParsedItemId parsed = parseItemId(itemIdRaw);
        String nbtPath = fieldNbtPath.getText()
            .trim();
        String nbtValue = fieldNbtValue.getText()
            .trim();
        if (nbtPath.isEmpty()) nbtPath = null;
        if (nbtValue.isEmpty()) nbtValue = null;
        MarkRegistry.addEntry(new MarkEntry(mark, parsed.itemId, parsed.meta, nbtPath, nbtValue));
    }

    private void deleteEntry() {
        if (selectedIndex >= 0 && selectedIndex < MarkRegistry.getEntries()
            .size()) {
            MarkRegistry.removeEntry(selectedIndex);
            selectedIndex = -1;
        }
    }

    private void updateEntry() {
        if (selectedIndex < 0 || selectedIndex >= MarkRegistry.getEntries()
            .size()) return;
        String mark = fieldMark.getText()
            .trim();
        String itemIdRaw = fieldItemId.getText()
            .trim();
        if (mark.isEmpty() || itemIdRaw.isEmpty()) return;
        ParsedItemId parsed = parseItemId(itemIdRaw);
        String nbtPath = fieldNbtPath.getText()
            .trim();
        String nbtValue = fieldNbtValue.getText()
            .trim();
        if (nbtPath.isEmpty()) nbtPath = null;
        if (nbtValue.isEmpty()) nbtValue = null;
        MarkRegistry.updateEntry(selectedIndex, new MarkEntry(mark, parsed.itemId, parsed.meta, nbtPath, nbtValue));
    }

    private void clearFields() {
        fieldMark.setText("");
        fieldItemId.setText("");
        fieldNbtPath.setText("");
        fieldNbtValue.setText("");
        selectedIndex = -1;
    }

    private void fillFromHand() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;
        ItemStack held = mc.thePlayer.getHeldItem();
        if (held == null || held.getItem() == null) return;
        String itemId = Item.itemRegistry.getNameForObject(held.getItem());
        if (itemId == null) return;
        int meta = held.getItemDamage();
        fieldItemId.setText(itemId + ":" + meta);
    }

    private ParsedItemId parseItemId(String raw) {
        int lastColon = raw.lastIndexOf(':');
        int firstColon = raw.indexOf(':');
        if (lastColon > firstColon) {
            String metaPart = raw.substring(lastColon + 1);
            String itemId = raw.substring(0, lastColon);
            if ("*".equals(metaPart)) {
                return new ParsedItemId(itemId, -1);
            }
            try {
                return new ParsedItemId(itemId, Integer.parseInt(metaPart));
            } catch (NumberFormatException e) {
                return new ParsedItemId(raw, -1);
            }
        }
        return new ParsedItemId(raw, -1);
    }

    private static class ParsedItemId {

        final String itemId;
        final int meta;

        ParsedItemId(String itemId, int meta) {
            this.itemId = itemId;
            this.meta = meta;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        fieldMark.mouseClicked(mouseX, mouseY, mouseButton);
        fieldItemId.mouseClicked(mouseX, mouseY, mouseButton);
        fieldNbtPath.mouseClicked(mouseX, mouseY, mouseButton);
        fieldNbtValue.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseX >= LIST_X && mouseX < width - 10 && mouseY >= LIST_Y && mouseY < LIST_Y + listHeight) {
            int idx = (mouseY - LIST_Y) / ENTRY_HEIGHT + scrollOffset;
            List<MarkEntry> entries = MarkRegistry.getEntries();
            if (idx >= 0 && idx < entries.size()) {
                selectedIndex = idx;
                MarkEntry entry = entries.get(idx);
                fieldMark.setText(entry.getMark() != null ? entry.getMark() : "");
                fieldItemId.setText(formatItemId(entry));
                fieldNbtPath.setText(entry.getNbtPath() != null ? entry.getNbtPath() : "");
                fieldNbtValue.setText(entry.getNbtValue() != null ? entry.getNbtValue() : "");
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
            return;
        }
        fieldMark.textboxKeyTyped(typedChar, keyCode);
        fieldItemId.textboxKeyTyped(typedChar, keyCode);
        fieldNbtPath.textboxKeyTyped(typedChar, keyCode);
        fieldNbtValue.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            int maxScroll = Math.max(
                0,
                MarkRegistry.getEntries()
                    .size() - listHeight / ENTRY_HEIGHT);
            if (scroll > 0) scrollOffset = Math.max(0, scrollOffset - 1);
            else scrollOffset = Math.min(maxScroll, scrollOffset + 1);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
