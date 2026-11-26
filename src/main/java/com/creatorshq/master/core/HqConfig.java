package com.creatorshq.master.core;

import java.util.LinkedHashMap;
import java.util.Map;

/** Master configuration data. Plain POJO so Gson can (de)serialise it. */
public final class HqConfig {
    public static final int CURRENT_SCHEMA = 1;

    public int schemaVersion = CURRENT_SCHEMA;
    public String theme = ThemeManager.DEFAULT_ID;
    public int uiScalePercent = 100;          // 75..150
    public boolean animations = true;
    public boolean compactLayout = false;
    public Map<String, Boolean> moduleEnabled = new LinkedHashMap<>();
    /** Mirror of the key bindings (GLFW codes) for export/import; vanilla options.txt stays authoritative. */
    public Map<String, Integer> keybinds = new LinkedHashMap<>();
    /** Per-module settings: moduleId -> (key -> value). Each module owns its own sub-map. */
    public Map<String, Map<String, String>> moduleSettings = new LinkedHashMap<>();
}
