package cr.chromapie.itemmarks.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.CustomModularScreen;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.DoubleValue;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.SliderWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import com.github.bsideup.jabel.Desugar;

import cr.chromapie.itemmarks.core.MarkConfig;
import cr.chromapie.itemmarks.core.MarkEntry;
import cr.chromapie.itemmarks.core.MarkRegistry;

public class GuiMarkManagerMui extends CustomModularScreen {

    private static final int WIDTH = 320;
    private static final int HEIGHT = 180;
    private static final int PADDING = 6;
    private static final int LIST_Y = 24;

    private static int panelIdCounter = 0;

    private static String nextPanelId(String prefix) {
        return prefix + "_" + (panelIdCounter++);
    }

    private ListWidget<IWidget, ?> entryList;
    private ModularPanel mainPanel;
    private ModularPanel currentEditorPanel;

    public GuiMarkManagerMui() {
        super("itemmarks");
    }

    @Override
    public ModularPanel buildUI(ModularGuiContext context) {
        mainPanel = ModularPanel.defaultPanel("mark_manager", WIDTH, HEIGHT);

        mainPanel.child(GuiHelper.createTitle("itemmarks.gui.title", WIDTH, 6));

        ButtonWidget<?> configBtn = new ButtonWidget<>();
        configBtn.pos(4, 4);
        configBtn.size(14, 14);
        configBtn.overlay(IKey.str("C"));
        configBtn.onMousePressed(btn -> {
            openConfigPanel();
            return true;
        });
        mainPanel.child(configBtn);

        ButtonWidget<?> helpBtn = new ButtonWidget<>();
        helpBtn.pos(WIDTH - 18, 4);
        helpBtn.size(14, 14);
        helpBtn.overlay(IKey.str("§e?"));
        helpBtn.onMousePressed(btn -> {
            openHelpPanel();
            return true;
        });
        mainPanel.child(helpBtn);

        int listHeight = HEIGHT - LIST_Y - 30;
        entryList = new ListWidget<>();
        entryList.pos(PADDING, LIST_Y);
        entryList.size(WIDTH - PADDING * 2, listHeight);
        entryList.background(new Rectangle().setColor(0x80000000));
        buildEntryListContent();
        mainPanel.child(entryList);

        int btnY = HEIGHT - 24;
        int btnX = PADDING;

        ButtonWidget<?> addBtn = new ButtonWidget<>();
        addBtn.pos(btnX, btnY);
        addBtn.size(40, 18);
        addBtn.overlay(IKey.lang("itemmarks.gui.add"));
        addBtn.onMousePressed(btn -> {
            openEntryEditor(-1, null);
            return true;
        });
        mainPanel.child(addBtn);
        btnX += 44;

        ButtonWidget<?> fromHandBtn = new ButtonWidget<>();
        fromHandBtn.pos(btnX, btnY);
        fromHandBtn.size(65, 18);
        final String fromHandText = StatCollector.translateToLocal("itemmarks.gui.fromhand");
        fromHandBtn.overlay(IKey.dynamic(() -> {
            boolean enabled = GuiHelper.hasHeldItem();
            return enabled ? fromHandText : "§7" + fromHandText;
        }));
        fromHandBtn.onUpdateListener(btn -> GuiHelper.applyButtonStyle(btn, GuiHelper.hasHeldItem()), true);
        fromHandBtn.onMousePressed(btn -> {
            if (!GuiHelper.hasHeldItem()) return true;
            openEditorFromHand();
            return true;
        });
        mainPanel.child(fromHandBtn);

        return mainPanel;
    }

    private void openHelpPanel() {
        GuiHelper.openPanel(mainPanel, HelpPanel::new);
    }

    private void buildEntryListContent() {
        List<MarkEntry> entries = MarkRegistry.getEntries();
        for (int idx = 0; idx < entries.size(); idx++) {
            MarkEntry entry = entries.get(idx);
            entryList.child(buildEntryRow(entry, idx));
        }
    }

    private void refreshEntryList() {
        if (entryList == null) return;
        entryList.getChildren()
            .clear();
        buildEntryListContent();
    }

    private IWidget buildEntryRow(MarkEntry entry, int index) {
        String text;
        if (entry.hasItemCondition()) {
            text = String.format("[%s] %s", entry.getMark(), formatItemId(entry));
        } else {
            text = String.format("[%s] §7*", entry.getMark());
        }
        if (entry.hasNbtCondition()) {
            String path = entry.getNbtPath();
            if (path == null || path.isEmpty()) {
                text += String.format(" {%s}", entry.getNbtValue());
            } else {
                text += String.format(" {%s=%s}", path, entry.getNbtValue());
            }
        }

        final int maxLen = 50;
        if (text.length() > maxLen) {
            text = text.substring(0, maxLen - 3) + "...";
        }

        final int idx = index;
        final GuiMarkManagerMui self = this;
        ButtonWidget<?> btn = new ButtonWidget<>();
        btn.left(0);
        btn.right(0);
        btn.height(14);
        btn.background(new Rectangle().setColor(0x00000000));
        btn.hoverBackground(new Rectangle().setColor(0x40FFFFFF));
        btn.overlay(
            IKey.str(text)
                .alignment(Alignment.CenterLeft)
                .color(0xFFFFFF)
                .shadow(false));
        btn.onMousePressed(mouseBtn -> {
            if (mouseBtn == 0) {
                MarkEntry e = MarkRegistry.getEntries()
                    .get(idx);
                openEntryEditor(idx, e);
            } else if (mouseBtn == 1) {
                int mx = self.getContext()
                    .getAbsMouseX();
                int my = self.getContext()
                    .getAbsMouseY();
                openEntryContextMenu(idx, mx, my);
            }
            return true;
        });
        return btn;
    }

    private void openEntryContextMenu(int index, int mouseX, int mouseY) {
        final GuiMarkManagerMui self = this;
        GuiHelper.openPanel(mainPanel, () -> new EntryContextMenu(self, index, mouseX, mouseY));
    }

    private String formatItemId(MarkEntry entry) {
        if (entry.getMeta() < 0) {
            return entry.getItemId() + ":*";
        } else {
            return entry.getItemId() + ":" + entry.getMeta();
        }
    }

    public void deleteEntry(int index) {
        if (index >= 0 && index < MarkRegistry.getEntries()
            .size()) {
            MarkRegistry.removeEntry(index);
            refreshEntryList();
        }
    }

    private void openEditorFromHand() {
        ItemStack held = GuiHelper.getHeldItem();
        if (held == null || held.getItem() == null) return;

        String itemId = Item.itemRegistry.getNameForObject(held.getItem());
        if (itemId == null) return;

        int meta = held.getItemDamage();
        openEntryEditor(-1, new MarkEntry("", itemId, meta, null, null));
    }

    public void openEntryEditor(int idx, MarkEntry pf) {
        if (currentEditorPanel != null) {
            currentEditorPanel.closeIfOpen();
        }
        final GuiMarkManagerMui self = this;
        EntryEditorPanel panel = new EntryEditorPanel(self, idx, pf);
        currentEditorPanel = panel;
        GuiHelper.openPanel(mainPanel, () -> panel);
    }

    public void onEditorConfirm(int editIndex, String mark, String itemIdRaw, String nbtPath, String nbtValue) {
        if (mark.isEmpty()) return;
        if (nbtPath != null && nbtPath.isEmpty()) nbtPath = null;
        if (nbtValue != null && nbtValue.isEmpty()) nbtValue = null;
        if (itemIdRaw.isEmpty() && nbtValue == null) return;

        ParsedItemId parsed = parseItemId(itemIdRaw);

        MarkEntry entry = new MarkEntry(mark, parsed.itemId(), parsed.meta(), nbtPath, nbtValue);

        if (editIndex >= 0 && editIndex < MarkRegistry.getEntries()
            .size()) {
            MarkRegistry.updateEntry(editIndex, entry);
        } else {
            MarkRegistry.addEntry(entry);
        }
        refreshEntryList();
    }

    public void setNbtFromEditor(String path, String value, EntryEditorPanel editorPanel) {
        editorPanel.setNbtFields(path, value);
    }

    private void openConfigPanel() {
        final GuiMarkManagerMui self = this;
        GuiHelper.openPanel(mainPanel, () -> new ConfigPanel(self));
    }

    public void resetAllEntries() {
        MarkRegistry.clear();
        refreshEntryList();
    }

    static ParsedItemId parseItemId(String raw) {
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

    @Desugar
    record ParsedItemId(String itemId, int meta) {}

    @Override
    public boolean doesPauseGame() {
        return false;
    }

    public static class EntryEditorPanel extends ModularPanel {

        private static final int WIDTH = 240;
        private static final int HEIGHT = 130;

        private final GuiMarkManagerMui parent;
        private final int editIndex;

        private String markText = "";
        private String itemIdText = "";
        private String nbtPathText = "";
        private String nbtValueText = "";

        private TextFieldWidget markField;
        private TextFieldWidget itemIdField;
        private TextFieldWidget nbtPathField;
        private TextFieldWidget nbtValueField;

        public EntryEditorPanel(GuiMarkManagerMui parent, int editIndex, MarkEntry prefill) {
            super(nextPanelId("entry_editor"));
            this.parent = parent;
            this.editIndex = editIndex;

            if (prefill != null) {
                this.markText = prefill.getMark() != null ? prefill.getMark() : "";
                if (prefill.getItemId() != null && !prefill.getItemId()
                    .isEmpty()) {
                    if (prefill.getMeta() < 0) {
                        this.itemIdText = prefill.getItemId() + ":*";
                    } else {
                        this.itemIdText = prefill.getItemId() + ":" + prefill.getMeta();
                    }
                }
                this.nbtPathText = prefill.getNbtPath() != null ? prefill.getNbtPath() : "";
                this.nbtValueText = prefill.getNbtValue() != null ? prefill.getNbtValue() : "";
            }

            size(WIDTH, HEIGHT);
            background(com.cleanroommc.modularui.drawable.GuiTextures.MC_BACKGROUND);

            String titleKey = editIndex >= 0 ? "itemmarks.editor.edit" : "itemmarks.editor.add";
            child(GuiHelper.createTitle(titleKey, WIDTH, 6));

            final EntryEditorPanel self = this;
            int y = 22;

            TextWidget markLabel = new TextWidget(IKey.lang("itemmarks.editor.mark"));
            markLabel.pos(6, y + 2);
            markLabel.size(32, 14);
            markLabel.shadow(false);
            child(markLabel);

            markField = new TextFieldWidget();
            markField.value(new StringValue.Dynamic(() -> self.markText, val -> self.markText = val));
            markField.pos(40, y);
            markField.size(28, 16);
            markField.setMaxLength(2);
            markField.setText(this.markText);
            child(markField);

            TextWidget itemIdLabel = new TextWidget(IKey.lang("itemmarks.editor.itemid"));
            itemIdLabel.pos(75, y + 2);
            itemIdLabel.size(40, 14);
            itemIdLabel.shadow(false);
            child(itemIdLabel);

            itemIdField = new TextFieldWidget();
            itemIdField.value(new StringValue.Dynamic(() -> self.itemIdText, val -> self.itemIdText = val));
            itemIdField.pos(115, y);
            itemIdField.size(111, 16);
            itemIdField.paddingRight(8);
            itemIdField.setMaxLength(256);
            itemIdField.setText(this.itemIdText);
            child(itemIdField);

            y += 22;

            TextWidget nbtPathLabel = new TextWidget(IKey.lang("itemmarks.editor.nbtpath"));
            nbtPathLabel.pos(6, y + 2);
            nbtPathLabel.size(50, 14);
            nbtPathLabel.shadow(false);
            child(nbtPathLabel);

            nbtPathField = new TextFieldWidget();
            nbtPathField.value(new StringValue.Dynamic(() -> self.nbtPathText, val -> self.nbtPathText = val));
            nbtPathField.pos(58, y);
            nbtPathField.size(76, 16);
            nbtPathField.paddingRight(8);
            nbtPathField.setMaxLength(256);
            nbtPathField.setText(this.nbtPathText);
            child(nbtPathField);

            TextWidget nbtValueLabel = new TextWidget(IKey.lang("itemmarks.editor.value"));
            nbtValueLabel.pos(140, y + 2);
            nbtValueLabel.size(32, 14);
            nbtValueLabel.shadow(false);
            child(nbtValueLabel);

            nbtValueField = new TextFieldWidget();
            nbtValueField.value(new StringValue.Dynamic(() -> self.nbtValueText, val -> self.nbtValueText = val));
            nbtValueField.pos(172, y);
            nbtValueField.size(54, 16);
            nbtValueField.paddingRight(8);
            nbtValueField.setMaxLength(256);
            nbtValueField.setText(this.nbtValueText);
            child(nbtValueField);

            y += 24;

            ButtonWidget<?> fromHandBtn = new ButtonWidget<>();
            fromHandBtn.pos(6, y);
            fromHandBtn.size(65, 18);
            final String fromHandText = StatCollector.translateToLocal("itemmarks.gui.fromhand");
            fromHandBtn.overlay(IKey.dynamic(() -> {
                boolean enabled = GuiHelper.hasHeldItem();
                return enabled ? fromHandText : "§7" + fromHandText;
            }));
            fromHandBtn.onUpdateListener(btn -> GuiHelper.applyButtonStyle(btn, GuiHelper.hasHeldItem()), true);
            fromHandBtn.onMousePressed(btn -> {
                if (!GuiHelper.hasHeldItem()) return true;
                fillFromHand();
                return true;
            });
            child(fromHandBtn);

            ButtonWidget<?> nbtBtn = new ButtonWidget<>();
            nbtBtn.pos(75, y);
            nbtBtn.size(50, 18);
            final String nbtText = StatCollector.translateToLocal("itemmarks.editor.nbt");
            nbtBtn.overlay(IKey.dynamic(() -> {
                boolean enabled = GuiHelper.heldItemHasNbt();
                return enabled ? nbtText : "§7" + nbtText;
            }));
            nbtBtn.onUpdateListener(btn -> GuiHelper.applyButtonStyle(btn, GuiHelper.heldItemHasNbt()), true);
            nbtBtn.onMousePressed(btn -> {
                if (!GuiHelper.heldItemHasNbt()) return true;
                openNbtEditor();
                return true;
            });
            child(nbtBtn);

            y += 24;

            ButtonWidget<?> cancelBtn = new ButtonWidget<>();
            cancelBtn.pos(WIDTH / 2 - 70, y);
            cancelBtn.size(60, 18);
            cancelBtn.overlay(IKey.lang("itemmarks.editor.cancel"));
            cancelBtn.onMousePressed(btn -> {
                closeIfOpen();
                return true;
            });
            child(cancelBtn);

            ButtonWidget<?> confirmBtn = new ButtonWidget<>();
            confirmBtn.pos(WIDTH / 2 + 10, y);
            confirmBtn.size(60, 18);
            final String confirmText = StatCollector.translateToLocal("itemmarks.editor.confirm");
            confirmBtn.overlay(IKey.dynamic(() -> {
                boolean enabled = isConfirmEnabled();
                return enabled ? "§a" + confirmText : "§7" + confirmText;
            }));
            confirmBtn.onUpdateListener(btn -> GuiHelper.applyButtonStyle(btn, isConfirmEnabled()), true);
            confirmBtn.onMousePressed(btn -> {
                if (!isConfirmEnabled()) return true;
                String mark = markField.getText()
                    .trim();
                String itemId = itemIdField.getText()
                    .trim();
                String nbtPath = nbtPathField.getText()
                    .trim();
                String nbtVal = nbtValueField.getText()
                    .trim();
                parent.onEditorConfirm(editIndex, mark, itemId, nbtPath, nbtVal);
                closeIfOpen();
                return true;
            });
            child(confirmBtn);
        }

        private boolean isConfirmEnabled() {
            String mark = markField.getText()
                .trim();
            String itemIdRaw = itemIdField.getText()
                .trim();
            String nbtPath = nbtPathField.getText()
                .trim();
            String nbtVal = nbtValueField.getText()
                .trim();

            if (mark.isEmpty()) return false;
            if (itemIdRaw.isEmpty() && nbtVal.isEmpty()) return false;

            ParsedItemId parsed = parseItemId(itemIdRaw);
            String path = nbtPath.isEmpty() ? null : nbtPath;
            String value = nbtVal.isEmpty() ? null : nbtVal;
            MarkEntry tempEntry = new MarkEntry(mark, parsed.itemId(), parsed.meta(), path, value);
            return !MarkRegistry.isDuplicateExcluding(tempEntry, editIndex);
        }

        private void fillFromHand() {
            ItemStack held = GuiHelper.getHeldItem();
            if (held == null || held.getItem() == null) return;
            String itemId = Item.itemRegistry.getNameForObject(held.getItem());
            if (itemId == null) return;
            int meta = held.getItemDamage();
            String text = itemId + ":" + meta;
            itemIdText = text;
            if (itemIdField != null) {
                itemIdField.setText(text);
            }
        }

        private void openNbtEditor() {
            ItemStack held = GuiHelper.getHeldItem();
            if (held == null || held.getTagCompound() == null) return;

            final EntryEditorPanel self = this;
            final ItemStack heldStack = held;
            GuiHelper.openPanel(this, () -> new NbtEditorPanelForEditor(self, heldStack));
        }

        public void setNbtFields(String path, String value) {
            nbtPathText = path != null ? path : "";
            nbtValueText = value != null ? value : "";
            if (nbtPathField != null) nbtPathField.setText(nbtPathText);
            if (nbtValueField != null) nbtValueField.setText(nbtValueText);
        }

        public boolean hasNbtContent() {
            String path = nbtPathField != null ? nbtPathField.getText()
                .trim() : nbtPathText.trim();
            String value = nbtValueField != null ? nbtValueField.getText()
                .trim() : nbtValueText.trim();
            return !path.isEmpty() || !value.isEmpty();
        }

        @Override
        public boolean closeOnOutOfBoundsClick() {
            return false;
        }

        @Override
        public boolean disablePanelsBelow() {
            return true;
        }

        @Override
        public boolean isDraggable() {
            return true;
        }
    }

    public static class NbtEditorPanelForEditor extends ModularPanel {

        private static final int WIDTH = 200;
        private static final int HEIGHT = 120;

        private final EntryEditorPanel parent;
        private final ItemStack stack;
        private final List<NbtNode> nodes = new ArrayList<>();
        private final List<NbtNode> nodeCache = new ArrayList<>();
        private ListWidget<IWidget, ?> nodeList;

        public NbtEditorPanelForEditor(EntryEditorPanel parent, ItemStack stack) {
            super(nextPanelId("nbt_editor"));
            this.parent = parent;
            this.stack = stack;

            size(WIDTH, HEIGHT);
            background(com.cleanroommc.modularui.drawable.GuiTextures.MC_BACKGROUND);

            child(GuiHelper.createTitle("itemmarks.nbt.title", WIDTH, 6));
            child(ButtonWidget.panelCloseButton());

            nodeList = new ListWidget<>();
            nodeList.pos(6, 24);
            nodeList.size(WIDTH - 12, HEIGHT - 30);
            nodeList.background(new Rectangle().setColor(0x80000000));

            rebuildNodes();
            refreshNodeList();

            child(nodeList);
        }

        private void openContextMenu(NbtNode node, int mouseX, int mouseY) {
            final NbtEditorPanelForEditor self = this;
            GuiHelper.openPanel(this, () -> new NbtContextMenu(self, node, mouseX, mouseY));
        }

        void applyCondition(NbtNode node, int type) {
            String path = node.wildcardPath != null ? node.wildcardPath : node.path;
            String value;
            switch (type) {
                case 0:
                    if (node.tag instanceof net.minecraft.nbt.NBTTagCompound
                        || node.tag instanceof net.minecraft.nbt.NBTTagList) {
                        value = "*";
                    } else {
                        String v = node.tag.toString();
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

            if (parent.hasNbtContent()) {
                final String finalPath = path;
                final String finalValue = value;
                final NbtEditorPanelForEditor self = this;
                GuiHelper.openPanel(this, () -> new NbtOverwriteConfirmPanel(self, finalPath, finalValue));
            } else {
                parent.setNbtFields(path, value);
                closeIfOpen();
            }
        }

        void doApply(String path, String value) {
            parent.setNbtFields(path, value);
            closeIfOpen();
        }

        @Override
        public boolean closeOnOutOfBoundsClick() {
            return false;
        }

        @Override
        public boolean disablePanelsBelow() {
            return false;
        }

        @Override
        public boolean isDraggable() {
            return true;
        }

        private void rebuildNodes() {
            nodes.clear();
            if (stack == null || stack.getTagCompound() == null) return;
            net.minecraft.nbt.NBTTagCompound root = stack.getTagCompound();
            rebuildCompound(root, "", 0);
        }

        private void rebuildCompound(net.minecraft.nbt.NBTTagCompound compound, String pathPrefix, int depth) {
            java.util.Set<String> keys = compound.func_150296_c();
            for (String key : keys) {
                net.minecraft.nbt.NBTBase tag = compound.getTag(key);
                String path = pathPrefix.isEmpty() ? key : pathPrefix + "." + key;
                NbtNode node = findOrCreateNode(key, tag, path, null, depth, false);
                nodes.add(node);
                if (node.expanded) {
                    if (tag instanceof net.minecraft.nbt.NBTTagCompound) {
                        rebuildCompound((net.minecraft.nbt.NBTTagCompound) tag, path, depth + 1);
                    } else if (tag instanceof net.minecraft.nbt.NBTTagList) {
                        rebuildList((net.minecraft.nbt.NBTTagList) tag, path, depth + 1);
                    }
                }
            }
        }

        private void rebuildList(net.minecraft.nbt.NBTTagList list, String pathPrefix, int depth) {
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound tag = list.getCompoundTagAt(i);
                String path = pathPrefix + "[" + i + "]";
                String wildcardPath = pathPrefix + "[*]";
                NbtNode node = findOrCreateNode("[" + i + "]", tag, path, wildcardPath, depth, true);
                nodes.add(node);
                if (node.expanded && tag instanceof net.minecraft.nbt.NBTTagCompound) {
                    rebuildCompound(tag, path, depth + 1);
                }
            }
        }

        private NbtNode findOrCreateNode(String key, net.minecraft.nbt.NBTBase tag, String path, String wildcardPath,
            int depth, boolean isListElement) {
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

        private void refreshNodeList() {
            if (nodeList == null) return;
            nodeList.getChildren()
                .clear();
            rebuildNodes();
            for (NbtNode node : nodes) {
                nodeList.child(buildNodeRow(node));
            }
        }

        private IWidget buildNodeRow(NbtNode node) {
            boolean isExpandable = node.tag instanceof net.minecraft.nbt.NBTTagCompound
                || node.tag instanceof net.minecraft.nbt.NBTTagList;
            String prefix;
            if (isExpandable) {
                prefix = node.expanded ? "§7[-] " : "§7[+] ";
            } else {
                prefix = "    ";
            }

            StringBuilder indent = new StringBuilder();
            for (int i = 0; i < node.depth; i++) {
                indent.append("  ");
            }

            String display = indent + prefix + "§b" + node.key + "§f: " + formatValue(node.tag);

            ButtonWidget<?> btn = new ButtonWidget<>();
            btn.left(0);
            btn.right(0);
            btn.height(14);
            btn.background(new Rectangle().setColor(0x00000000));
            btn.hoverBackground(new Rectangle().setColor(0x40FFFFFF));
            btn.overlay(
                IKey.str(display)
                    .alignment(Alignment.CenterLeft)
                    .shadow(false));
            final NbtNode n = node;
            final NbtEditorPanelForEditor self = this;
            btn.onMousePressed(mouseButton -> {
                if (mouseButton == 0) {
                    if (isExpandable) {
                        n.expanded = !n.expanded;
                        refreshNodeList();
                    }
                } else if (mouseButton == 1) {
                    int mx = self.getContext()
                        .getAbsMouseX();
                    int my = self.getContext()
                        .getAbsMouseY();
                    openContextMenu(n, mx, my);
                }
                return true;
            });
            return btn;
        }

        private String formatValue(net.minecraft.nbt.NBTBase tag) {
            if (tag instanceof net.minecraft.nbt.NBTTagCompound) {
                return "§8{" + ((net.minecraft.nbt.NBTTagCompound) tag).func_150296_c()
                    .size() + " entries}";
            } else if (tag instanceof net.minecraft.nbt.NBTTagList) {
                return "§8[" + ((net.minecraft.nbt.NBTTagList) tag).tagCount() + " elements]";
            } else {
                String s = tag.toString();
                if (s.length() > 30) s = s.substring(0, 27) + "...";
                return "§a" + s;
            }
        }
    }

    public static class NbtContextMenu extends ModularPanel {

        private static final int WIDTH = 70;
        private static final int HEIGHT = 54;

        public NbtContextMenu(NbtEditorPanelForEditor parent, NbtNode node, int mouseX, int mouseY) {
            super(nextPanelId("nbt_ctx"));
            size(WIDTH, HEIGHT);
            background(com.cleanroommc.modularui.drawable.GuiTextures.MC_BACKGROUND);

            GuiHelper.positionContextMenu(this, mouseX, mouseY, WIDTH, HEIGHT, parent.getScreen());

            child(GuiHelper.createContextMenuButton("itemmarks.nbt.ctx.value", 4, 4, WIDTH - 8, 14, () -> {
                parent.applyCondition(node, 0);
                closeIfOpen();
            }));

            child(GuiHelper.createContextMenuButton("itemmarks.nbt.ctx.exists", 4, 20, WIDTH - 8, 14, () -> {
                parent.applyCondition(node, 1);
                closeIfOpen();
            }));

            child(GuiHelper.createContextMenuButton("itemmarks.nbt.ctx.not", 4, 36, WIDTH - 8, 14, () -> {
                parent.applyCondition(node, 2);
                closeIfOpen();
            }));
        }

        @Override
        public boolean closeOnOutOfBoundsClick() {
            return true;
        }

        @Override
        public boolean disablePanelsBelow() {
            return false;
        }

        @Override
        public boolean isDraggable() {
            return false;
        }
    }

    public static class EntryContextMenu extends ModularPanel {

        private static final int WIDTH = 60;
        private static final int HEIGHT = 40;

        public EntryContextMenu(GuiMarkManagerMui parent, int entryIndex, int mouseX, int mouseY) {
            super(nextPanelId("entry_ctx"));
            size(WIDTH, HEIGHT);
            background(com.cleanroommc.modularui.drawable.GuiTextures.MC_BACKGROUND);

            GuiHelper.positionContextMenu(this, mouseX, mouseY, WIDTH, HEIGHT, parent);

            child(GuiHelper.createContextMenuButton("itemmarks.entry.ctx.dupe", 4, 4, WIDTH - 8, 14, () -> {
                MarkEntry entry = MarkRegistry.getEntries()
                    .get(entryIndex);
                parent.openEntryEditor(-1, entry);
                closeIfOpen();
            }));

            child(GuiHelper.createContextMenuButton("itemmarks.entry.ctx.delete", 4, 20, WIDTH - 8, 14, () -> {
                parent.deleteEntry(entryIndex);
                closeIfOpen();
            }));
        }

        @Override
        public boolean closeOnOutOfBoundsClick() {
            return true;
        }

        @Override
        public boolean disablePanelsBelow() {
            return false;
        }

        @Override
        public boolean isDraggable() {
            return false;
        }
    }

    private static class NbtNode {

        String key;
        net.minecraft.nbt.NBTBase tag;
        String path;
        String wildcardPath;
        int depth;
        boolean expanded;
        boolean isListElement;

        NbtNode(String key, net.minecraft.nbt.NBTBase tag, String path, String wildcardPath, int depth,
            boolean isListElement) {
            this.key = key;
            this.tag = tag;
            this.path = path;
            this.wildcardPath = wildcardPath;
            this.depth = depth;
            this.isListElement = isListElement;
            this.expanded = false;
        }
    }

    public static class NbtOverwriteConfirmPanel extends ModularPanel {

        public NbtOverwriteConfirmPanel(NbtEditorPanelForEditor nbtPanel, String path, String value) {
            super(nextPanelId("nbt_overwrite"));
            size(200, 80);
            background(com.cleanroommc.modularui.drawable.GuiTextures.MC_BACKGROUND);

            child(GuiHelper.createTitle("itemmarks.nbt.overwrite.title", 200, 12));

            TextWidget msg = new TextWidget(IKey.lang("itemmarks.nbt.overwrite.message"));
            msg.alignment(Alignment.Center);
            msg.pos(0, 28);
            msg.size(200, 12);
            msg.shadow(false);
            child(msg);

            ButtonWidget<?> confirmBtn = new ButtonWidget<>();
            confirmBtn.pos(30, 50);
            confirmBtn.size(60, 18);
            confirmBtn.overlay(IKey.lang("itemmarks.nbt.overwrite.confirm"));
            confirmBtn.onMousePressed(btn -> {
                nbtPanel.doApply(path, value);
                return true;
            });
            child(confirmBtn);

            ButtonWidget<?> cancelBtn = new ButtonWidget<>();
            cancelBtn.pos(110, 50);
            cancelBtn.size(60, 18);
            cancelBtn.overlay(IKey.lang("itemmarks.editor.cancel"));
            cancelBtn.onMousePressed(btn -> {
                closeIfOpen();
                return true;
            });
            child(cancelBtn);
        }

        @Override
        public boolean isDraggable() {
            return false;
        }

        @Override
        public boolean closeOnOutOfBoundsClick() {
            return false;
        }

        @Override
        public boolean disablePanelsBelow() {
            return true;
        }
    }

    public static class ResetConfirmPanel extends ModularPanel {

        public ResetConfirmPanel(GuiMarkManagerMui parent) {
            super(nextPanelId("reset_confirm"));
            size(200, 80);
            background(com.cleanroommc.modularui.drawable.GuiTextures.MC_BACKGROUND);

            child(GuiHelper.createTitle("itemmarks.reset.title", 200, 12));

            TextWidget msg = new TextWidget(IKey.lang("itemmarks.reset.message"));
            msg.alignment(Alignment.Center);
            msg.pos(0, 28);
            msg.size(200, 12);
            msg.shadow(false);
            child(msg);

            ButtonWidget<?> confirmBtn = new ButtonWidget<>();
            confirmBtn.pos(30, 50);
            confirmBtn.size(60, 18);
            confirmBtn.overlay(IKey.lang("itemmarks.reset.confirm"));
            confirmBtn.onMousePressed(btn -> {
                parent.resetAllEntries();
                closeIfOpen();
                return true;
            });
            child(confirmBtn);

            ButtonWidget<?> cancelBtn = new ButtonWidget<>();
            cancelBtn.pos(110, 50);
            cancelBtn.size(60, 18);
            cancelBtn.overlay(IKey.lang("itemmarks.editor.cancel"));
            cancelBtn.onMousePressed(btn -> {
                closeIfOpen();
                return true;
            });
            child(cancelBtn);
        }

        @Override
        public boolean isDraggable() {
            return false;
        }

        @Override
        public boolean closeOnOutOfBoundsClick() {
            return true;
        }

        @Override
        public boolean disablePanelsBelow() {
            return true;
        }
    }

    public static class ConfigPanel extends ModularPanel {

        private int currentScale;

        public ConfigPanel(GuiMarkManagerMui parent) {
            super(nextPanelId("config"));
            size(200, 130);
            background(com.cleanroommc.modularui.drawable.GuiTextures.MC_BACKGROUND);
            currentScale = MarkConfig.getMarkScale();

            child(GuiHelper.createTitle("itemmarks.config.title", 200, 6));

            ButtonWidget<?> resetBtn = new ButtonWidget<>();
            resetBtn.pos(200 - 18, 4);
            resetBtn.size(14, 14);
            resetBtn.overlay(IKey.str("§cR"));
            resetBtn.onMousePressed(btn -> {
                GuiHelper.openPanel(this, () -> new ResetConfirmPanel(parent));
                return true;
            });
            child(resetBtn);

            TextWidget enabledLabel = new TextWidget(IKey.lang("itemmarks.config.enabled"));
            enabledLabel.pos(10, 24);
            enabledLabel.size(80, 14);
            enabledLabel.shadow(false);
            child(enabledLabel);

            ButtonWidget<?> enabledBtn = new ButtonWidget<>();
            enabledBtn.pos(95, 22);
            enabledBtn.size(95, 16);
            enabledBtn.overlay(
                IKey.dynamic(
                    () -> MarkConfig.isEnabled() ? "§a" + StatCollector.translateToLocal("itemmarks.config.on")
                        : "§c" + StatCollector.translateToLocal("itemmarks.config.off")));
            enabledBtn.onMousePressed(btn -> {
                MarkConfig.setEnabled(!MarkConfig.isEnabled());
                return true;
            });
            child(enabledBtn);

            TextWidget posLabel = new TextWidget(IKey.lang("itemmarks.config.position"));
            posLabel.pos(10, 44);
            posLabel.size(80, 14);
            posLabel.shadow(false);
            child(posLabel);

            ButtonWidget<?> posBtn = new ButtonWidget<>();
            posBtn.pos(95, 42);
            posBtn.size(95, 16);
            posBtn.overlay(
                IKey.dynamic(
                    () -> MarkConfig.getMarkPosition()
                        .getLocalizedName()));
            posBtn.onMousePressed(btn -> {
                MarkConfig.cycleMarkPosition();
                return true;
            });
            child(posBtn);

            final ConfigPanel self = this;
            final String scalePrefix = StatCollector.translateToLocal("itemmarks.config.scale");
            TextWidget scaleLabel = new TextWidget(IKey.dynamic(() -> scalePrefix + " " + self.currentScale + "%"));
            scaleLabel.pos(10, 64);
            scaleLabel.size(180, 14);
            scaleLabel.shadow(false);
            child(scaleLabel);

            SliderWidget scaleSlider = new SliderWidget();
            scaleSlider.pos(10, 78);
            scaleSlider.size(180, 14);
            scaleSlider.bounds(50, 300);
            scaleSlider.stopper(50);
            scaleSlider.value(new DoubleValue.Dynamic(() -> (double) self.currentScale, val -> {
                self.currentScale = (int) Math.round(val);
                MarkConfig.setMarkScale(self.currentScale);
            }));
            scaleSlider.background(new Rectangle().setColor(0x80000000));
            child(scaleSlider);

            ButtonWidget<?> closeBtn = new ButtonWidget<>();
            closeBtn.pos(70, 102);
            closeBtn.size(60, 18);
            closeBtn.overlay(IKey.lang("itemmarks.config.close"));
            closeBtn.onMousePressed(btn -> {
                closeIfOpen();
                return true;
            });
            child(closeBtn);
        }

        @Override
        public boolean isDraggable() {
            return false;
        }

        @Override
        public boolean closeOnOutOfBoundsClick() {
            return true;
        }

        @Override
        public boolean disablePanelsBelow() {
            return true;
        }
    }

    public static class HelpPanel extends ModularPanel {

        private static final int WIDTH = 160;
        private static final int HEIGHT = 140;

        public HelpPanel() {
            super(nextPanelId("help"));
            size(WIDTH, HEIGHT);
            background(com.cleanroommc.modularui.drawable.GuiTextures.MC_BACKGROUND);

            TextWidget title = new TextWidget(IKey.lang("itemmarks.help.title"));
            title.alignment(Alignment.Center);
            title.pos(0, 6);
            title.size(WIDTH, 10);
            child(title);

            child(ButtonWidget.panelCloseButton());

            @SuppressWarnings("rawtypes")
            ListWidget list = new ListWidget<>();
            list.pos(6, 18);
            list.size(WIDTH - 12, HEIGHT - 24);

            addSection(list, "itemmarks.help.section.entry", "itemmarks.help.leftclick", "itemmarks.help.rightclick");

            addSection(list, "itemmarks.help.section.nbt", "itemmarks.help.nbt.expand", "itemmarks.help.nbt.select");

            addSection(
                list,
                "itemmarks.help.section.itemid",
                "itemmarks.help.itemid.base",
                "itemmarks.help.itemid.meta",
                "itemmarks.help.itemid.any",
                "itemmarks.help.itemid.empty");

            addSection(
                list,
                "itemmarks.help.section.nbtpath",
                "itemmarks.help.nbtpath.key",
                "itemmarks.help.nbtpath.nested",
                "itemmarks.help.nbtpath.index",
                "itemmarks.help.nbtpath.any",
                "itemmarks.help.nbtpath.wildcard",
                "itemmarks.help.nbtpath.wildcardnest",
                "itemmarks.help.nbtpath.empty");

            addSection(
                list,
                "itemmarks.help.section.nbtvalue",
                "itemmarks.help.nbtvalue.exact",
                "itemmarks.help.nbtvalue.exists",
                "itemmarks.help.nbtvalue.notexists",
                "itemmarks.help.nbtvalue.multi");

            child(list);
        }

        @SuppressWarnings("rawtypes")
        private void addSection(ListWidget list, String titleKey, String... lineKeys) {
            TextWidget sectionTitle = new TextWidget(IKey.lang(titleKey));
            sectionTitle.alignment(Alignment.Center);
            sectionTitle.left(0)
                .right(0);
            sectionTitle.maxWidth(WIDTH - 20);
            list.child(sectionTitle);

            for (String key : lineKeys) {
                TextWidget line = new TextWidget(IKey.lang(key));
                line.alignment(Alignment.CenterLeft);
                line.left(0)
                    .right(0);
                line.maxWidth(WIDTH - 20);
                list.child(line);
            }

            TextWidget spacer = new TextWidget(IKey.str(""));
            spacer.left(0)
                .right(0)
                .height(6);
            list.child(spacer);
        }

        @Override
        public boolean isDraggable() {
            return true;
        }

        @Override
        public boolean closeOnOutOfBoundsClick() {
            return false;
        }

        @Override
        public boolean disablePanelsBelow() {
            return false;
        }
    }
}
