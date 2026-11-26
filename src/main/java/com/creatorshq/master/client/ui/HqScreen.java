package com.creatorshq.master.client.ui;

import com.creatorshq.master.CreatorsHq;
import com.creatorshq.master.core.Theme;
import com.creatorshq.master.core.ThemeManager;
import com.creatorshq.master.module.HqModule;
import com.creatorshq.master.module.ModuleRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for every master-mod screen. Draws the shared top navigation bar (tabs generated from the
 * ModuleRegistry + settings button) and handles tab clicks. Subclasses draw below {@link #NAV_H}.
 * Switching tabs swaps the screen with no flicker: the new screen draws the same bar immediately.
 */
public abstract class HqScreen extends Screen {
    public static final int NAV_H = 26;
    private static final int TAB_PAD = 8;

    private record Tab(int x1, int x2, Runnable action) {}
    private final List<Tab> tabs = new ArrayList<>();
    private final String activeId; // "dashboard", "settings" or a module id

    protected HqScreen(String title, String activeId) {
        super(Component.literal(title));
        this.activeId = activeId;
    }

    protected final Theme theme() { return ThemeManager.current(); }
    protected final int contentTop() { return NAV_H + 6; }

    @Override
    public boolean isPauseScreen() { return false; } // keep the integrated server ticking so commands run

    protected final void drawChrome(GuiGraphics g, int mx, int my) {
        Theme t = theme();
        tabs.clear();
        g.fill(0, 0, width, NAV_H, t.navBar());
        g.fill(0, NAV_H - 1, width, NAV_H, t.border());

        int x = 6;
        x = tab(g, x, "Dashboard", "dashboard", mx, my, () -> minecraft.setScreen(new DashboardScreen()));
        for (HqModule m : ModuleRegistry.enabled()) {
            x = tab(g, x, m.displayName(), m.id(), mx, my, () -> minecraft.setScreen(m.createScreen()));
        }
        String gear = "Settings";
        int gw = font.width(gear) + TAB_PAD * 2;
        int gx = width - gw - 6;
        drawTab(g, gx, gw, gear, "settings".equals(activeId), mx, my);
        tabs.add(new Tab(gx, gx + gw, () -> minecraft.setScreen(new SettingsScreen())));
    }

    private int tab(GuiGraphics g, int x, String label, String id, int mx, int my, Runnable action) {
        int w = font.width(label) + TAB_PAD * 2;
        if (x + w > width - 90) return x; // out of room: remaining modules stay reachable from the Dashboard
        drawTab(g, x, w, label, id.equals(activeId), mx, my);
        tabs.add(new Tab(x, x + w, action));
        return x + w + 2;
    }

    private void drawTab(GuiGraphics g, int x, int w, String label, boolean active, int mx, int my) {
        Theme t = theme();
        boolean hover = mx >= x && mx < x + w && my >= 0 && my < NAV_H;
        if (active) {
            g.fill(x, 3, x + w, NAV_H - 1, t.panelAlt());
            g.fill(x, NAV_H - 3, x + w, NAV_H - 1, t.accent());
        } else if (hover) {
            g.fill(x, 3, x + w, NAV_H - 1, t.tint(t.navBar(), 0.15));
        }
        g.drawString(font, label, x + TAB_PAD, 9, active ? t.text() : t.textDim());
    }

    protected final boolean handleChromeClick(double mx, double my, int button) {
        if (button != 0 || my >= NAV_H) return false;
        for (Tab tab : tabs) {
            if (mx >= tab.x1() && mx < tab.x2()) {
                tab.action().run();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (handleChromeClick(event.x(), event.y(), event.button())) return true;
        return super.mouseClicked(event, doubleClick);
    }

    protected final void drawBackdrop(GuiGraphics g) {
        g.fill(0, 0, width, height, theme().background());
    }

    protected static String brand() { return CreatorsHq.NAME; }
}
