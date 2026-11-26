package com.creatorshq.master.core;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Central theme registry. Screens never hardcode colours: they call {@link #current()}.
 * Switching is instant because screens read the theme on every render.
 */
public final class ThemeManager {
    public static final String DEFAULT_ID = "midnight";
    private static final Map<String, Theme> THEMES = new LinkedHashMap<>();
    private static Theme current;
    private static Consumer<String> persistence = id -> {};

    static {
        register(new Theme("midnight", "Midnight Dark",
                0xFF0B0D14, 0xFF12151F, 0xFF1A1E2C, 0xFF2A3045,
                0xFF4C8DFF, 0xFF74A6FF, 0xFFE8ECF7, 0xFF8C93A8,
                0xFF0F121B, 0xFF0D1018, 0xFFE5534B, 0xFF4CC38A));
        register(new Theme("crimson", "Crimson",
                0xFF0C0909, 0xFF171010, 0xFF221616, 0xFF3A2224,
                0xFFD0343C, 0xFFEE5760, 0xFFF4E9E9, 0xFF9C8586,
                0xFF110B0B, 0xFF100A0A, 0xFFFF6B6B, 0xFF4CC38A));
        register(new Theme("ocean", "Ocean",
                0xFF07111C, 0xFF0C1B2B, 0xFF132740, 0xFF1E3A5C,
                0xFF22D3EE, 0xFF67E8F9, 0xFFE6F6FB, 0xFF86A5B8,
                0xFF0A1624, 0xFF08131F, 0xFFF0626B, 0xFF4CC38A));
        register(new Theme("purple", "Purple Haze",
                0xFF0E0A16, 0xFF171124, 0xFF221A35, 0xFF352A54,
                0xFFA855F7, 0xFFC084FC, 0xFFF1EAFB, 0xFF9A8DB5,
                0xFF120D1D, 0xFF0F0B18, 0xFFF0626B, 0xFF4CC38A));
        register(new Theme("emerald", "Emerald",
                0xFF08110D, 0xFF0E1B15, 0xFF15291F, 0xFF1F3F30,
                0xFF22C55E, 0xFF4ADE80, 0xFFE7F7EE, 0xFF86A898,
                0xFF0B1610, 0xFF09130E, 0xFFF0626B, 0xFF4ADE80));
        current = THEMES.get(DEFAULT_ID);
    }

    private ThemeManager() {}

    public static void register(Theme t) { THEMES.put(t.id(), t); }
    public static Theme current() { return current; }
    public static List<Theme> all() { return List.copyOf(THEMES.values()); }
    public static boolean exists(String id) { return id != null && THEMES.containsKey(id); }

    /** Hook used by the config layer so a change is saved immediately. */
    public static void setPersistence(Consumer<String> saver) { persistence = saver == null ? id -> {} : saver; }

    /** Applies a theme without saving (used while loading config). */
    public static void apply(String id) { current = THEMES.getOrDefault(id, THEMES.get(DEFAULT_ID)); }

    /** Applies a theme and persists the choice. */
    public static void select(String id) {
        apply(id);
        persistence.accept(current.id());
    }
}
