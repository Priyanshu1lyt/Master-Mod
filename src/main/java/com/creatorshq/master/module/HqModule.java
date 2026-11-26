package com.creatorshq.master.module;

import net.minecraft.client.gui.screens.Screen;

/**
 * Contract every feature module implements. To add a future module: implement this, then call
 * {@link ModuleRegistry#register(HqModule)} from CreatorsHqClient. Nothing else (nav, dashboard,
 * keybinds, settings list) needs editing.
 */
public interface HqModule {
    /** Stable lowercase id; used in config keys and keybind translation keys. */
    String id();
    String displayName();
    String description();
    /** Item id shown as the module icon, e.g. "minecraft:compass". */
    String iconItemId();
    /** GLFW key code for the module's own shortcut, or -1 for unbound. */
    int defaultKey();

    /** Main screen. Must extend HqScreen so the shared navigation bar is drawn. */
    Screen createScreen();

    /** Optional module-specific settings screen; null = module has none yet. */
    default Screen createSettingsScreen(Screen parent) { return null; }

    /** Called once at startup, isolated in try/catch by the registry. */
    default void init() {}
    /** Called when the module is disabled or the client stops. */
    default void shutdown() {}

    /** False while a module is only a registered placeholder awaiting migration. */
    default boolean isMigrated() { return true; }
}
