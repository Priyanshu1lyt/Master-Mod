package com.creatorshq.master.modules.pending;

import com.creatorshq.master.client.ui.PendingModuleScreen;
import com.creatorshq.master.module.HqModule;
import net.minecraft.client.gui.screens.Screen;

/** Registers a module's tab/keybind/dashboard card before its real implementation is migrated. */
public final class PendingModule implements HqModule {
    private final String id, name, description, icon;
    private final int key;

    public PendingModule(String id, String name, String description, String icon, int key) {
        this.id = id; this.name = name; this.description = description; this.icon = icon; this.key = key;
    }
    @Override public String id() { return id; }
    @Override public String displayName() { return name; }
    @Override public String description() { return description; }
    @Override public String iconItemId() { return icon; }
    @Override public int defaultKey() { return key; }
    @Override public Screen createScreen() { return new PendingModuleScreen(this); }
    @Override public boolean isMigrated() { return false; }
}
