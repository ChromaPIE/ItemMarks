package cr.chromapie.itemmarks.core;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import net.minecraft.util.StatCollector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Configuration for Item Marks mod.
 */
public class MarkConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
        .create();
    private static File configFile;
    private static ConfigData data = new ConfigData();

    public enum MarkPosition {

        TOP_LEFT("Top Left", "itemmarks.position.topleft"),
        TOP_RIGHT("Top Right", "itemmarks.position.topright"),
        BOTTOM_LEFT("Bottom Left", "itemmarks.position.bottomleft"),
        MIDDLE("Middle", "itemmarks.position.middle");

        private final String displayName;
        private final String langKey;

        MarkPosition(String displayName, String langKey) {
            this.displayName = displayName;
            this.langKey = langKey;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getLocalizedName() {
            return StatCollector.translateToLocal(langKey);
        }

        public MarkPosition next() {
            MarkPosition[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    private static class ConfigData {

        boolean enabled = true;
        MarkPosition markPosition = MarkPosition.TOP_LEFT;
        int markScale = 100;
    }

    public static void load(File configDir) {
        configFile = new File(configDir, "itemmarks_config.json");
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                ConfigData loaded = GSON.fromJson(reader, ConfigData.class);
                if (loaded != null) {
                    data = loaded;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void save() {
        if (configFile == null) return;
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(data, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isEnabled() {
        return data.enabled;
    }

    public static void setEnabled(boolean enabled) {
        data.enabled = enabled;
        save();
    }

    public static MarkPosition getMarkPosition() {
        return data.markPosition;
    }

    public static void setMarkPosition(MarkPosition position) {
        data.markPosition = position;
        save();
    }

    public static void cycleMarkPosition() {
        data.markPosition = data.markPosition.next();
        save();
    }

    public static int getMarkScale() {
        return data.markScale;
    }

    public static void setMarkScale(int scale) {
        data.markScale = Math.max(50, Math.min(300, scale));
        save();
    }

    /**
     * Returns the actual GL scale factor.
     * 100% = 0.5F (half font size), 200% = 1.0F (full font size)
     */
    public static float getActualScale() {
        return 0.5F * (data.markScale / 100F);
    }
}
