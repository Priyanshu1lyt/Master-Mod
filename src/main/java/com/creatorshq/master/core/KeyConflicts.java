package com.creatorshq.master.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Pure conflict detection between key bindings (GLFW key codes; -1 = unbound). */
public final class KeyConflicts {
    private KeyConflicts() {}

    /** @return key code -> ids sharing it; only codes used by 2+ bindings. */
    public static Map<Integer, List<String>> find(Map<String, Integer> bindings) {
        Map<Integer, List<String>> byKey = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : bindings.entrySet()) {
            int code = e.getValue() == null ? -1 : e.getValue();
            if (code < 0) continue;
            byKey.computeIfAbsent(code, k -> new ArrayList<>()).add(e.getKey());
        }
        byKey.values().removeIf(l -> l.size() < 2);
        return byKey;
    }

    public static boolean conflicts(Map<String, Integer> bindings, String id) {
        Integer mine = bindings.get(id);
        if (mine == null || mine < 0) return false;
        for (Map.Entry<String, Integer> e : bindings.entrySet()) {
            if (!e.getKey().equals(id) && mine.equals(e.getValue())) return true;
        }
        return false;
    }
}
