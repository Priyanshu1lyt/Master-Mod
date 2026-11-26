package com.creatorshq.master.client.ui;

import com.creatorshq.master.CreatorsHq;
import com.creatorshq.master.client.Compat;
import com.creatorshq.master.client.KeybindManager;
import com.creatorshq.master.core.ConfigManager;
import com.creatorshq.master.core.HqConfig;
import com.creatorshq.master.core.Theme;
import com.creatorshq.master.core.ThemeManager;
import com.creatorshq.master.module.HqModule;
import com.creatorshq.master.module.ModuleRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.ArrayList;
import java.util.List;

/** Centralised settings: Appearance, Keybinds, General, About. Pure hit-test UI so it follows the active theme. */
public class SettingsScreen extends HqScreen {
    private enum Section { APPEARANCE, KEYBINDS, GENERAL, ABOUT }
    private record Hit(int x1, int y1, int x2, int y2, Runnable action) {}

    private static Section section = Section.APPEARANCE; // remembered while the game runs
    private final List<Hit> hits = new ArrayList<>();
    private String capturing = null; // keybind id waiting for a key press

    public SettingsScreen() { super("Settings", "settings"); }

    private void button(GuiGraphics g, int x, int y, int w, String label, boolean active, int mx, int my, Runnable r) {
        Theme t = theme();
        boolean hover = mx >= x && mx < x + w && my >= y && my < y + 16;
        g.fill(x, y, x + w, y + 16, active ? t.accent() : hover ? t.tint(t.panelAlt(), 0.25) : t.panelAlt());
        g.drawCenteredString(font, label, x + w / 2, y + 4, active ? t.background() : t.text());
        hits.add(new Hit(x, y, x + w, y + 16, r));
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        drawBackdrop(g);
        hits.clear();
        Theme t = theme();
        HqConfig c = ConfigManager.get();
        int top = contentTop();

        int sx = 12;
        for (Section s : Section.values()) {
            String label = s.name().charAt(0) + s.name().substring(1).toLowerCase();
            button(g, sx, top, 74, label, s == section, mx, my, () -> { section = s; capturing = null; });
            sx += 78;
        }
        int x = 16, y = top + 28;

        switch (section) {
            case APPEARANCE -> {
                g.drawString(font, "Theme (applies instantly, saved automatically)", x, y, t.textDim());
                int bx = x;
                for (Theme th : ThemeManager.all()) {
                    int w = font.width(th.name()) + 14;
                    button(g, bx, y + 12, w, th.name(), th.id().equals(t.id()), mx, my, () -> ThemeManager.select(th.id()));
                    bx += w + 4;
                }
                y += 40;
                g.drawString(font, "UI scale: " + c.uiScalePercent + "%", x, y + 4, t.text());
                button(g, x + 90, y, 20, "-", false, mx, my, () -> { c.uiScalePercent = Math.max(75, c.uiScalePercent - 5); ConfigManager.save(); });
                button(g, x + 114, y, 20, "+", false, mx, my, () -> { c.uiScalePercent = Math.min(150, c.uiScalePercent + 5); ConfigManager.save(); });
                y += 24;
                button(g, x, y, 150, "Animations: " + (c.animations ? "ON" : "OFF"), c.animations, mx, my, () -> { c.animations = !c.animations; ConfigManager.save(); });
                y += 22;
                button(g, x, y, 150, "Layout: " + (c.compactLayout ? "Compact" : "Comfortable"), false, mx, my, () -> { c.compactLayout = !c.compactLayout; ConfigManager.save(); });
            }
            case KEYBINDS -> {
                g.drawString(font, capturing == null ? "Click a key to rebind. ESC cancels, Delete unbinds." : "Press the new key for '" + capturing + "'...", x, y, t.textDim());
                y += 14;
                for (KeybindManager.Entry e : KeybindManager.entries()) {
                    boolean conflict = KeybindManager.hasConflict(e.id());
                    g.drawString(font, e.label(), x, y + 4, t.text());
                    String k = e.id().equals(capturing) ? "> press key <" : Compat.keyLabel(e.mapping());
                    button(g, x + 170, y, 90, k, e.id().equals(capturing), mx, my, () -> capturing = e.id());
                    if (conflict) g.drawString(font, "Conflict!", x + 266, y + 4, t.danger());
                    y += 19;
                }
                button(g, x, y + 4, 130, "Restore default keys", false, mx, my, KeybindManager::resetAll);
            }
            case GENERAL -> {
                g.drawString(font, "Modules", x, y, t.textDim());
                y += 12;
                for (HqModule m : ModuleRegistry.all()) {
                    boolean on = ConfigManager.isEnabled(m.id());
                    g.drawString(font, m.displayName(), x, y + 4, t.text());
                    button(g, x + 170, y, 50, on ? "ON" : "OFF", on, mx, my, () -> ModuleRegistry.setEnabled(m.id(), !on));
                    button(g, x + 224, y, 90, "Reset config", false, mx, my, () -> ConfigManager.resetModule(m.id()));
                    y += 19;
                }
                y += 6;
                button(g, x, y, 170, "Restore ALL default settings", false, mx, my, () -> { ConfigManager.resetAll(); KeybindManager.resetAll(); });
            }
            case ABOUT -> {
                String[] lines = {
                        CreatorsHq.NAME, "Version " + CreatorsHq.VERSION,
                        "Minecraft " + CreatorsHq.MC_VERSION + " (Fabric, client-side)",
                        "Authors: Priyanshu1lyt, NotHeartYT, Praveen_god_67, Nebrion_Verse",
                        "Seven tools, one hub: Announce, Trim & Ore, TP, Gamemode, Enchant, Give, Potions."};
                for (String l : lines) { g.drawString(font, l, x, y, t.text()); y += 13; }
            }
        }
        drawChrome(g, mx, my);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (handleChromeClick(event.x(), event.y(), event.button())) return true;
        if (event.button() == 0) {
            for (int i = hits.size() - 1; i >= 0; i--) {
                Hit h = hits.get(i);
                if (event.x() >= h.x1() && event.x() < h.x2() && event.y() >= h.y1() && event.y() < h.y2()) {
                    h.action().run();
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (capturing != null) {
            int key = event.key();
            if (key == 256) { capturing = null; return true; }               // ESC: cancel
            KeybindManager.rebind(capturing, key == 261 ? -1 : key);         // Delete: unbind
            capturing = null;
            return true;
        }
        return super.keyPressed(event);
    }
}
