package com.creatorshq.master.client;

import com.creatorshq.master.CreatorsHq;
import com.creatorshq.master.core.ConfigManager;
import com.creatorshq.master.core.KeyConflicts;
import com.creatorshq.master.module.HqModule;
import com.creatorshq.master.module.ModuleRegistry;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** One master key + one key per module. Registered exactly once; bindings live in vanilla options.txt. */
public final class KeybindManager {
    public static final String MASTER_ID = "master";
    public static final int MASTER_DEFAULT = 344; // GLFW_KEY_RIGHT_SHIFT

    public record Entry(String id, String label, KeyMapping mapping) {}

    private static final Map<String, Entry> ENTRIES = new LinkedHashMap<>();
    private static KeyMapping.Category category;

    private KeybindManager() {}

    public static void registerAll() {
        category = Compat.category(CreatorsHq.MOD_ID, "main");
        add(MASTER_ID, "Master Menu", MASTER_DEFAULT);
        for (HqModule m : ModuleRegistry.all()) add(m.id(), m.displayName(), m.defaultKey());
        mirrorToConfig();
    }

    private static void add(String id, String label, int key) {
        if (ENTRIES.containsKey(id)) throw new IllegalStateException("Duplicate keybind id: " + id);
        KeyMapping km = KeyMappingHelper.registerKeyMapping(Compat.newKey("key." + CreatorsHq.MOD_ID + "." + id, key, category));
        ENTRIES.put(id, new Entry(id, label, km));
    }

    public static List<Entry> entries() { return List.copyOf(ENTRIES.values()); }
    public static Entry get(String id) { return ENTRIES.get(id); }

    public static Map<String, Integer> currentCodes() {
        Map<String, Integer> out = new LinkedHashMap<>();
        for (Entry e : ENTRIES.values()) out.put(e.id(), Compat.boundCode(e.mapping()));
        return out;
    }

    public static boolean hasConflict(String id) { return KeyConflicts.conflicts(currentCodes(), id); }

    public static void rebind(String id, int glfwKey) {
        Entry e = ENTRIES.get(id);
        if (e == null) return;
        Compat.bindKey(e.mapping(), glfwKey);
        mirrorToConfig();
    }

    public static void resetAll() {
        for (Entry e : ENTRIES.values()) Compat.bindKey(e.mapping(), Compat.defaultCode(e.mapping()));
        mirrorToConfig();
    }

    private static void mirrorToConfig() {
        ConfigManager.get().keybinds = currentCodes();
        ConfigManager.save();
    }
}
