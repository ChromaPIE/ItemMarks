package cr.chromapie.itemmarks.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class MarkRegistry {

    private static final List<MarkEntry> entries = new ArrayList<>();
    private static File configDir;

    public static void load(File modConfigDir) {
        configDir = modConfigDir;
        entries.clear();
        File file = new File(configDir, "itemmarks.txt");
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                MarkEntry entry = MarkEntry.deserialize(line);
                if (entry != null) entries.add(entry);
            }
        } catch (Exception ignored) {}
    }

    public static void save() {
        if (configDir == null) return;
        File file = new File(configDir, "itemmarks.txt");
        try (BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (MarkEntry entry : entries) {
                writer.write(entry.serialize());
                writer.newLine();
            }
        } catch (Exception ignored) {}
    }

    public static String getMark(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return null;
        String itemId = Item.itemRegistry.getNameForObject(stack.getItem());
        int meta = stack.getItemDamage();
        NBTTagCompound nbt = stack.getTagCompound();

        // Priority 1: Item + NBT + Meta
        for (MarkEntry entry : entries) {
            if (!entry.hasItemCondition()) continue;
            if (itemId == null || !itemId.equals(entry.getItemId())) continue;
            if (entry.hasNbtCondition() && entry.hasMetaCondition()) {
                if (entry.getMeta() == meta && nbt != null && matchNbt(nbt, entry.getNbtPath(), entry.getNbtValue())) {
                    return entry.getMark();
                }
            }
        }

        // Priority 2: Item + NBT (any meta)
        for (MarkEntry entry : entries) {
            if (!entry.hasItemCondition()) continue;
            if (itemId == null || !itemId.equals(entry.getItemId())) continue;
            if (entry.hasNbtCondition() && !entry.hasMetaCondition()) {
                if (nbt != null && matchNbt(nbt, entry.getNbtPath(), entry.getNbtValue())) {
                    return entry.getMark();
                }
            }
        }

        // Priority 3: Item + Meta (no NBT)
        for (MarkEntry entry : entries) {
            if (!entry.hasItemCondition()) continue;
            if (itemId == null || !itemId.equals(entry.getItemId())) continue;
            if (!entry.hasNbtCondition() && entry.hasMetaCondition()) {
                if (entry.getMeta() == meta) {
                    return entry.getMark();
                }
            }
        }

        // Priority 4: Item only (no NBT, no meta)
        for (MarkEntry entry : entries) {
            if (!entry.hasItemCondition()) continue;
            if (itemId == null || !itemId.equals(entry.getItemId())) continue;
            if (!entry.hasNbtCondition() && !entry.hasMetaCondition()) {
                return entry.getMark();
            }
        }

        // Priority 5: NBT + Meta only (no item)
        for (MarkEntry entry : entries) {
            if (entry.hasItemCondition()) continue;
            if (entry.hasNbtCondition() && entry.hasMetaCondition()) {
                if (entry.getMeta() == meta && nbt != null && matchNbt(nbt, entry.getNbtPath(), entry.getNbtValue())) {
                    return entry.getMark();
                }
            }
        }

        // Priority 6: NBT only (no item, no meta)
        for (MarkEntry entry : entries) {
            if (entry.hasItemCondition()) continue;
            if (entry.hasNbtCondition() && !entry.hasMetaCondition()) {
                if (nbt != null && matchNbt(nbt, entry.getNbtPath(), entry.getNbtValue())) {
                    return entry.getMark();
                }
            }
        }

        return null;
    }

    private static boolean matchNbt(NBTTagCompound nbt, String path, String value) {
        if (path == null || path.isEmpty()) {
            if (value.contains("&") || value.contains("=")) {
                return matchMultiCondition(nbt, value);
            }
        }
        return matchNbtRecursive(nbt, path, value);
    }

    private static boolean matchNbtRecursive(NBTBase current, String path, String value) {
        if (path.isEmpty()) {
            if ("*".equals(value)) {
                return current != null;
            }
            if ("!".equals(value)) {
                return false;
            }
            if (value.contains("&") && current instanceof NBTTagCompound) {
                return matchMultiCondition((NBTTagCompound) current, value);
            }
            return matchValue(current, value);
        }
        int dotIdx = findUnescaped(path, '.');
        int bracketIdx = findUnescaped(path, '[');
        String segment;
        String nextPath;
        if (dotIdx == -1 && bracketIdx == -1) {
            segment = unescapePath(path);
            nextPath = "";
        } else if (bracketIdx != -1 && (dotIdx == -1 || bracketIdx < dotIdx)) {
            segment = unescapePath(path.substring(0, bracketIdx));
            nextPath = path.substring(bracketIdx);
        } else {
            segment = unescapePath(path.substring(0, dotIdx));
            nextPath = path.substring(dotIdx + 1);
        }
        if (segment.isEmpty() && nextPath.startsWith("[")) {
            int closeIdx = nextPath.indexOf(']');
            if (closeIdx == -1) return false;
            String indexPart = nextPath.substring(1, closeIdx);
            nextPath = nextPath.substring(closeIdx + 1);
            if (nextPath.startsWith(".")) nextPath = nextPath.substring(1);
            if (!(current instanceof NBTTagList list)) return false;
            if ("*".equals(indexPart)) {
                for (int i = 0; i < list.tagCount(); i++) {
                    if (matchNbtRecursive(list.getCompoundTagAt(i), nextPath, value)) {
                        return true;
                    }
                }
                return false;
            } else {
                int idx = Integer.parseInt(indexPart);
                if (idx < 0 || idx >= list.tagCount()) return false;
                return matchNbtRecursive(list.getCompoundTagAt(idx), nextPath, value);
            }
        }
        if (!(current instanceof NBTTagCompound compound)) return false;
        if ("*".equals(segment)) {
            java.util.Set<String> keys = compound.func_150296_c();
            for (String key : keys) {
                NBTBase child = compound.getTag(key);
                if (matchNbtRecursive(child, nextPath, value)) {
                    return true;
                }
            }
            return false;
        }
        if (!compound.hasKey(segment)) {
            return "!".equals(value) && nextPath.isEmpty();
        }
        NBTBase next = compound.getTag(segment);
        return matchNbtRecursive(next, nextPath, value);
    }

    private static int findUnescaped(String s, char target) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                i++;
                continue;
            }
            if (c == target) return i;
        }
        return -1;
    }

    private static String unescapePath(String s) {
        if (s.indexOf('\\') == -1) return s;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                sb.append(s.charAt(++i));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static boolean matchMultiCondition(NBTTagCompound compound, String conditions) {
        String[] parts = conditions.split("&");
        for (String part : parts) {
            int eqIdx = part.indexOf('=');
            if (eqIdx == -1) return false;
            String key = part.substring(0, eqIdx)
                .trim();
            String val = part.substring(eqIdx + 1)
                .trim();
            if ("*".equals(val)) {
                if (!compound.hasKey(key)) return false;
            } else if ("!".equals(val)) {
                if (compound.hasKey(key)) return false;
            } else {
                if (!compound.hasKey(key)) return false;
                if (!matchValue(compound.getTag(key), val)) return false;
            }
        }
        return true;
    }

    private static boolean matchValue(NBTBase tag, String expected) {
        String actual = tag.toString();
        if (actual.startsWith("\"") && actual.endsWith("\"")) {
            actual = actual.substring(1, actual.length() - 1);
        }
        if (expected.equals(actual)) return true;
        String stripped = actual.replaceAll("[bslfdBSLFD]$", "");
        return expected.equals(stripped);
    }

    public static List<MarkEntry> getEntries() {
        return entries;
    }

    public static boolean addEntry(MarkEntry entry) {
        if (isDuplicate(entry)) return false;
        entries.add(0, entry);
        save();
        return true;
    }

    public static boolean isDuplicate(MarkEntry entry) {
        return isDuplicateExcluding(entry, -1);
    }

    public static boolean isDuplicateExcluding(MarkEntry entry, int excludeIndex) {
        for (int i = 0; i < entries.size(); i++) {
            if (i == excludeIndex) continue;
            MarkEntry e = entries.get(i);
            if (!strEquals(e.getItemId(), entry.getItemId())) continue;
            if (e.getMeta() != entry.getMeta()) continue;
            if (!strEquals(e.getNbtPath(), entry.getNbtPath())) continue;
            if (!strEquals(e.getNbtValue(), entry.getNbtValue())) continue;
            return true;
        }
        return false;
    }

    private static boolean strEquals(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    public static void removeEntry(int index) {
        if (index >= 0 && index < entries.size()) {
            entries.remove(index);
            save();
        }
    }

    public static void updateEntry(int index, MarkEntry entry) {
        if (index >= 0 && index < entries.size()) {
            entries.set(index, entry);
            save();
        }
    }

    public static void clear() {
        entries.clear();
        save();
    }
}
