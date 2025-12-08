package cr.chromapie.itemmarks.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import org.lwjgl.input.Mouse;

public class GuiNbtEditor extends GuiScreen {

    private final GuiMarkManager parent;
    private final ItemStack stack;
    private final List<NbtNode> nodes = new ArrayList<>();
    private int scrollOffset = 0;
    private NbtNode selectedNode = null;
    private static final int LINE_HEIGHT = 12;
    private static final int INDENT = 10;
    private static final int LIST_X = 10;
    private static final int LIST_Y = 30;
    private int listHeight;

    public GuiNbtEditor(GuiMarkManager parent, ItemStack stack) {
        this.parent = parent;
        this.stack = stack;
        parseNbt();
    }

    private void parseNbt() {
        nodes.clear();
        if (stack == null || stack.getTagCompound() == null) return;
        NBTTagCompound root = stack.getTagCompound();
        parseCompound(root, "", 0);
    }

    private void parseCompound(NBTTagCompound compound, String pathPrefix, int depth) {
        Set<String> keys = compound.func_150296_c();
        for (String key : keys) {
            NBTBase tag = compound.getTag(key);
            String path = pathPrefix.isEmpty() ? key : pathPrefix + "." + key;
            addNode(key, tag, path, depth);
        }
    }

    private void parseList(NBTTagList list, String pathPrefix, int depth) {
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            String path = pathPrefix + "[" + i + "]";
            String wildcardPath = pathPrefix + "[*]";
            NbtNode node = new NbtNode("[" + i + "]", tag, path, wildcardPath, depth, true);
            nodes.add(node);
            if (tag instanceof NBTTagCompound && node.expanded) {
                parseCompound(tag, path, depth + 1);
            }
        }
    }

    private void addNode(String key, NBTBase tag, String path, int depth) {
        NbtNode node = new NbtNode(key, tag, path, null, depth, false);
        nodes.add(node);
        if (node.expanded) {
            if (tag instanceof NBTTagCompound) {
                parseCompound((NBTTagCompound) tag, path, depth + 1);
            } else if (tag instanceof NBTTagList) {
                parseList((NBTTagList) tag, path, depth + 1);
            }
        }
    }

    @Override
    public void initGui() {
        listHeight = height - 80;
        buttonList.clear();
        int btnY = height - 45;
        buttonList.add(new GuiButton(0, 10, btnY, 60, 20, "= Value"));
        buttonList.add(new GuiButton(1, 75, btnY, 60, 20, "Exists (*)"));
        buttonList.add(new GuiButton(2, 140, btnY, 60, 20, "Not (!)"));
        buttonList.add(new GuiButton(3, width - 70, btnY, 60, 20, "Cancel"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(
            fontRendererObj,
            "NBT Editor - Click to select, then choose condition",
            width / 2,
            10,
            0xFFFFFF);
        if (stack == null || stack.getTagCompound() == null) {
            drawCenteredString(fontRendererObj, "No NBT data on held item", width / 2, height / 2, 0xFF5555);
        } else {
            drawRect(LIST_X, LIST_Y, width - 10, LIST_Y + listHeight, 0x80000000);
            rebuildNodes();
            int visibleCount = listHeight / LINE_HEIGHT;
            int y = LIST_Y;
            for (int i = scrollOffset; i < nodes.size() && i < scrollOffset + visibleCount; i++) {
                NbtNode node = nodes.get(i);
                int x = LIST_X + 5 + node.depth * INDENT;
                boolean isSelected = node == selectedNode;
                if (isSelected) {
                    drawRect(LIST_X, y, width - 10, y + LINE_HEIGHT, 0x80FFFF00);
                }
                String prefix = "";
                if (node.tag instanceof NBTTagCompound || node.tag instanceof NBTTagList) {
                    prefix = node.expanded ? "§7[-] " : "§7[+] ";
                }
                String display = prefix + "§b" + node.key + "§f: " + formatValue(node.tag);
                fontRendererObj.drawStringWithShadow(display, x, y + 2, 0xFFFFFF);
                y += LINE_HEIGHT;
            }
        }
        if (selectedNode != null) {
            String info = "§eSelected: §f" + selectedNode.path;
            drawString(fontRendererObj, info, 10, height - 60, 0xFFFFFF);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void rebuildNodes() {
        nodes.clear();
        if (stack == null || stack.getTagCompound() == null) return;
        NBTTagCompound root = stack.getTagCompound();
        rebuildCompound(root, "", 0);
    }

    private void rebuildCompound(NBTTagCompound compound, String pathPrefix, int depth) {
        Set<String> keys = compound.func_150296_c();
        for (String key : keys) {
            NBTBase tag = compound.getTag(key);
            String path = pathPrefix.isEmpty() ? key : pathPrefix + "." + key;
            NbtNode node = findOrCreateNode(key, tag, path, null, depth, false);
            nodes.add(node);
            if (node.expanded) {
                if (tag instanceof NBTTagCompound) {
                    rebuildCompound((NBTTagCompound) tag, path, depth + 1);
                } else if (tag instanceof NBTTagList) {
                    rebuildList((NBTTagList) tag, path, depth + 1);
                }
            }
        }
    }

    private void rebuildList(NBTTagList list, String pathPrefix, int depth) {
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            String path = pathPrefix + "[" + i + "]";
            String wildcardPath = pathPrefix + "[*]";
            NbtNode node = findOrCreateNode("[" + i + "]", tag, path, wildcardPath, depth, true);
            nodes.add(node);
            if (node.expanded && tag instanceof NBTTagCompound) {
                rebuildCompound(tag, path, depth + 1);
            }
        }
    }

    private final List<NbtNode> nodeCache = new ArrayList<>();

    private NbtNode findOrCreateNode(String key, NBTBase tag, String path, String wildcardPath, int depth,
        boolean isListElement) {
        for (NbtNode n : nodeCache) {
            if (n.path.equals(path)) {
                n.key = key;
                n.tag = tag;
                n.depth = depth;
                return n;
            }
        }
        NbtNode node = new NbtNode(key, tag, path, wildcardPath, depth, isListElement);
        nodeCache.add(node);
        return node;
    }

    private String formatValue(NBTBase tag) {
        if (tag instanceof NBTTagCompound) {
            return "§8{" + ((NBTTagCompound) tag).func_150296_c()
                .size() + " entries}";
        } else if (tag instanceof NBTTagList) {
            return "§8[" + ((NBTTagList) tag).tagCount() + " elements]";
        } else {
            String s = tag.toString();
            if (s.length() > 40) s = s.substring(0, 37) + "...";
            return "§a" + s;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseX >= LIST_X && mouseX < width - 10 && mouseY >= LIST_Y && mouseY < LIST_Y + listHeight) {
            int idx = (mouseY - LIST_Y) / LINE_HEIGHT + scrollOffset;
            if (idx >= 0 && idx < nodes.size()) {
                NbtNode node = nodes.get(idx);
                if (mouseButton == 0) {
                    if (node.tag instanceof NBTTagCompound || node.tag instanceof NBTTagList) {
                        int toggleX = LIST_X + 5 + node.depth * INDENT;
                        if (mouseX >= toggleX && mouseX <= toggleX + 20) {
                            node.expanded = !node.expanded;
                            return;
                        }
                    }
                    selectedNode = node;
                }
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 3) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (selectedNode == null) return;
        String path = selectedNode.wildcardPath != null ? selectedNode.wildcardPath : selectedNode.path;
        String value;
        switch (button.id) {
            case 0:
                if (selectedNode.tag instanceof NBTTagCompound || selectedNode.tag instanceof NBTTagList) {
                    value = "*";
                } else {
                    String v = selectedNode.tag.toString();
                    if (v.startsWith("\"") && v.endsWith("\"")) {
                        v = v.substring(1, v.length() - 1);
                    }
                    v = v.replaceAll("[bslfdBSLFD]$", "");
                    value = v;
                }
                break;
            case 1:
                value = "*";
                break;
            case 2:
                value = "!";
                break;
            default:
                return;
        }
        parent.setNbtFromEditor(path, value);
        mc.displayGuiScreen(parent);
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            int maxScroll = Math.max(0, nodes.size() - listHeight / LINE_HEIGHT);
            if (scroll > 0) scrollOffset = Math.max(0, scrollOffset - 3);
            else scrollOffset = Math.min(maxScroll, scrollOffset + 3);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private static class NbtNode {

        String key;
        NBTBase tag;
        String path;
        String wildcardPath;
        int depth;
        boolean expanded;
        boolean isListElement;

        NbtNode(String key, NBTBase tag, String path, String wildcardPath, int depth, boolean isListElement) {
            this.key = key;
            this.tag = tag;
            this.path = path;
            this.wildcardPath = wildcardPath;
            this.depth = depth;
            this.isListElement = isListElement;
            this.expanded = false;
        }
    }
}
