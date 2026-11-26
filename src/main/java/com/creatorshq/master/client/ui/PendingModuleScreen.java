package com.creatorshq.master.client.ui;

import com.creatorshq.master.module.HqModule;
import net.minecraft.client.gui.GuiGraphics;

/** Honest placeholder for a module whose 1.21.11 source has not been migrated yet. Replaced per module. */
public class PendingModuleScreen extends HqScreen {
    private final HqModule module;

    public PendingModuleScreen(HqModule module) {
        super(module.displayName(), module.id());
        this.module = module;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        drawBackdrop(g);
        int y = contentTop() + 20;
        g.drawCenteredString(font, module.displayName(), width / 2, y, theme().text());
        g.drawCenteredString(font, "Not migrated to Minecraft 26.1.2 yet.", width / 2, y + 16, theme().danger());
        g.drawCenteredString(font, "See TODO.md and HANDOFF_INSTRUCTIONS.md.", width / 2, y + 30, theme().textDim());
        drawChrome(g, mx, my);
    }
}
