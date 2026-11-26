package com.creatorshq.master.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loads/saves {@code creators-hq.json}. A corrupt file is moved to {@code .corrupt-<time>} (never deleted)
 * and defaults are used. Saves are atomic (temp file + move).
 */
public final class ConfigManager {
    private static final Logger LOG = Logger.getLogger("creatorshq");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path file;
    private static HqConfig config = new HqConfig();

    private ConfigManager() {}

    public static HqConfig get() { return config; }

    public static void init(Path configDir) {
        file = configDir.resolve("creators-hq.json");
        load();
        ThemeManager.apply(config.theme);
        ThemeManager.setPersistence(id -> { config.theme = id; save(); });
    }

    public static void load() {
        if (file == null || !Files.exists(file)) { config = new HqConfig(); save(); return; }
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            HqConfig loaded = GSON.fromJson(json, HqConfig.class);
            if (loaded == null) throw new IllegalStateException("empty config");
            int fixes = ConfigValidator.sanitize(loaded);
            config = loaded;
            if (fixes > 0) { LOG.info("Repaired " + fixes + " config field(s)"); save(); }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Config unreadable, keeping a copy and using defaults", e);
            try {
                Files.move(file, file.resolveSibling("creators-hq.json.corrupt-" + System.currentTimeMillis()),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException io) { LOG.log(Level.WARNING, "Could not back up corrupt config", io); }
            config = new HqConfig();
            save();
        }
    }

    public static synchronized void save() {
        if (file == null) return;
        try {
            Files.createDirectories(file.getParent());
            Path tmp = file.resolveSibling("creators-hq.json.tmp");
            Files.writeString(tmp, GSON.toJson(config), StandardCharsets.UTF_8);
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to save Creators HQ config", e);
        }
    }

    public static boolean isEnabled(String moduleId) {
        return config.moduleEnabled.getOrDefault(moduleId, Boolean.TRUE);
    }

    public static void setEnabled(String moduleId, boolean on) {
        config.moduleEnabled.put(moduleId, on);
        save();
    }

    /** Module-private settings map (created on demand). */
    public static Map<String, String> moduleSettings(String moduleId) {
        return config.moduleSettings.computeIfAbsent(moduleId, k -> new LinkedHashMap<>());
    }

    public static void resetModule(String moduleId) {
        config.moduleSettings.remove(moduleId);
        save();
    }

    public static void resetAll() {
        config = new HqConfig();
        ThemeManager.apply(config.theme);
        save();
    }
}
