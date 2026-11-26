package com.creatorshq.master.module;

import com.creatorshq.master.CreatorsHq;
import com.creatorshq.master.core.ConfigManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Ordered registry. Navigation, dashboard and settings are all generated from this list. */
public final class ModuleRegistry {
    private static final Map<String, HqModule> MODULES = new LinkedHashMap<>();
    private static final List<String> FAILED = new ArrayList<>();

    private ModuleRegistry() {}

    public static void register(HqModule m) {
        if (MODULES.putIfAbsent(m.id(), m) != null) {
            throw new IllegalStateException("Duplicate module id: " + m.id());
        }
        try {
            m.init();
        } catch (Throwable t) { // one module failing must not take the master mod down
            FAILED.add(m.id());
            ConfigManager.setEnabled(m.id(), false);
            CreatorsHq.LOGGER.error("Module '{}' failed to initialise and was disabled", m.id(), t);
        }
    }

    public static List<HqModule> all() { return List.copyOf(MODULES.values()); }

    public static List<HqModule> enabled() {
        return MODULES.values().stream().filter(m -> ConfigManager.isEnabled(m.id())).toList();
    }

    public static Optional<HqModule> get(String id) { return Optional.ofNullable(MODULES.get(id)); }
    public static boolean failed(String id) { return FAILED.contains(id); }

    public static void setEnabled(String id, boolean on) {
        HqModule m = MODULES.get(id);
        if (m == null) return;
        ConfigManager.setEnabled(id, on);
        if (!on) {
            try { m.shutdown(); } catch (Throwable t) { CreatorsHq.LOGGER.error("Module '{}' shutdown failed", id, t); }
        }
    }
}
