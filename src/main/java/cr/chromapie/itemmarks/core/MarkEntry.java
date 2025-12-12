package cr.chromapie.itemmarks.core;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record MarkEntry(String mark, String itemId, int meta, String oreDict, String nbtPath, String nbtValue) {

    public MarkEntry(String mark, String itemId, int meta, String oreDict, String nbtPath, String nbtValue) {
        this.mark = mark != null && mark.length() > 4 ? mark.substring(0, 4) : mark;
        this.itemId = itemId;
        this.meta = meta;
        this.oreDict = oreDict;
        this.nbtPath = nbtPath;
        this.nbtValue = nbtValue;
    }

    public boolean hasMetaCondition() {
        return meta >= 0;
    }

    public boolean hasItemCondition() {
        return itemId != null && !itemId.isEmpty();
    }

    public boolean hasOreDictCondition() {
        return oreDict != null && !oreDict.isEmpty();
    }

    public boolean hasNbtCondition() {
        return nbtValue != null && !nbtValue.isEmpty();
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append(escapeField(mark))
            .append("|");
        sb.append(escapeField(itemId));
        if (itemId != null && !itemId.isEmpty() && meta >= 0) {
            sb.append(":")
                .append(meta);
        }
        sb.append("|");
        sb.append(escapeField(oreDict))
            .append("|");
        sb.append(escapeField(nbtPath))
            .append("|");
        sb.append(escapeField(nbtValue));
        return sb.toString();
    }

    public static MarkEntry deserialize(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 2) return null;
        String mark = unescapeField(parts[0]);
        String itemIdRaw = unescapeField(parts[1]);
        String itemId;
        int meta = -1;
        int lastColon = itemIdRaw.lastIndexOf(':');
        int firstColon = itemIdRaw.indexOf(':');
        if (lastColon > firstColon) {
            String metaPart = itemIdRaw.substring(lastColon + 1);
            itemId = itemIdRaw.substring(0, lastColon);
            if (!"*".equals(metaPart)) {
                try {
                    meta = Integer.parseInt(metaPart);
                } catch (NumberFormatException e) {
                    itemId = itemIdRaw;
                }
            }
        } else {
            itemId = itemIdRaw;
        }
        String oreDict;
        String nbtPath;
        String nbtValue;
        if (parts.length >= 5) {
            oreDict = unescapeField(parts[2]);
            nbtPath = unescapeField(parts[3]);
            nbtValue = unescapeField(parts[4]);
        } else {
            oreDict = null;
            nbtPath = parts.length > 2 ? unescapeField(parts[2]) : null;
            nbtValue = parts.length > 3 ? unescapeField(parts[3]) : null;
        }
        if (oreDict != null && oreDict.isEmpty()) oreDict = null;
        if (nbtPath != null && nbtPath.isEmpty()) nbtPath = null;
        if (nbtValue != null && nbtValue.isEmpty()) nbtValue = null;
        return new MarkEntry(mark, itemId, meta, oreDict, nbtPath, nbtValue);
    }

    private static String escapeField(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
            .replace("|", "\\p")
            .replace("\n", "\\n");
    }

    private static String unescapeField(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.replace("\\n", "\n")
            .replace("\\p", "|")
            .replace("\\\\", "\\");
    }
}
