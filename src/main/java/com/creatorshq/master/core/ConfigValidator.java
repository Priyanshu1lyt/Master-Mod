package com.creatorshq.master.core;

import java.util.LinkedHashMap;
import java.util.Set;

/** Repairs a loaded config in place. Never throws; unknown module data is preserved, not deleted. */
public final class ConfigValidator {
    private ConfigValidator() {}

    /** @return number of fields that had to be repaired. */
    public static int sanitize(HqConfig c) {
        int fixes = 0;
        if (!ThemeManager.exists(c.theme)) { c.theme = ThemeManager.DEFAULT_ID; fixes++; }
        if (c.uiScalePercent < 75 || c.uiScalePercent > 150) {
            c.uiScalePercent = Math.max(75, Math.min(150, c.uiScalePercent == 0 ? 100 : c.uiScalePercent));
            fixes++;
        }
        if (c.moduleEnabled == null) { c.moduleEnabled = new LinkedHashMap<>(); fixes++; }
        if (c.keybinds == null) { c.keybinds = new LinkedHashMap<>(); fixes++; }
        if (c.moduleSettings == null) { c.moduleSettings = new LinkedHashMap<>(); fixes++; }
        if (c.schemaVersion > HqConfig.CURRENT_SCHEMA) {
            // Written by a newer version: keep everything, just don't downgrade the marker.
        } else if (c.schemaVersion < HqConfig.CURRENT_SCHEMA) {
            c.schemaVersion = HqConfig.CURRENT_SCHEMA; // migration hook: add steps per old version here
            fixes++;
        }
        return fixes;
    }

    /** Drops nothing; only reports ids in config that no module claims (kept for future modules / downgrade safety). */
    public static Set<String> unknownModules(HqConfig c, Set<String> known) {
        Set<String> out = new java.util.LinkedHashSet<>(c.moduleSettings.keySet());
        out.addAll(c.moduleEnabled.keySet());
        out.removeAll(known);
        return out;
    }
}
