package cr.chromapie.itemmarks.core;

public class MarkEntry {

    private String mark;
    private String itemId;
    private int meta;
    private String nbtPath;
    private String nbtValue;

    public MarkEntry(String mark, String itemId, int meta, String nbtPath, String nbtValue) {
        this.mark = mark != null && mark.length() > 2 ? mark.substring(0, 2) : mark;
        this.itemId = itemId;
        this.meta = meta;
        this.nbtPath = nbtPath;
        this.nbtValue = nbtValue;
    }

    public MarkEntry(String mark, String itemId) {
        this(mark, itemId, -1, null, null);
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark != null && mark.length() > 2 ? mark.substring(0, 2) : mark;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public int getMeta() {
        return meta;
    }

    public void setMeta(int meta) {
        this.meta = meta;
    }

    public boolean hasMetaCondition() {
        return meta >= 0;
    }

    public boolean hasItemCondition() {
        return itemId != null && !itemId.isEmpty();
    }

    public String getNbtPath() {
        return nbtPath;
    }

    public void setNbtPath(String nbtPath) {
        this.nbtPath = nbtPath;
    }

    public String getNbtValue() {
        return nbtValue;
    }

    public void setNbtValue(String nbtValue) {
        this.nbtValue = nbtValue;
    }

    public boolean hasNbtCondition() {
        return nbtValue != null && !nbtValue.isEmpty();
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append(escapeField(mark))
            .append("|");
        sb.append(escapeField(itemId));
        if (itemId != null && !itemId.isEmpty()) {
            if (meta < 0) {
                sb.append(":*");
            } else if (meta > 0) {
                sb.append(":")
                    .append(meta);
            }
        }
        sb.append("|");
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
            try {
                meta = Integer.parseInt(metaPart);
            } catch (NumberFormatException e) {
                itemId = itemIdRaw;
            }
        } else {
            itemId = itemIdRaw;
        }
        String nbtPath = parts.length > 2 ? unescapeField(parts[2]) : null;
        String nbtValue = parts.length > 3 ? unescapeField(parts[3]) : null;
        if (nbtPath != null && nbtPath.isEmpty()) nbtPath = null;
        if (nbtValue != null && nbtValue.isEmpty()) nbtValue = null;
        return new MarkEntry(mark, itemId, meta, nbtPath, nbtValue);
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
