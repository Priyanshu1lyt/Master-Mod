package com.creatorshq.master.client.ui;

import com.creatorshq.master.CreatorsHq;
import com.creatorshq.master.client.KeybindManager;
import com.creatorshq.master.core.ConfigManager;
import com.creatorshq.master.core.Theme;
import com.creatorshq.master.module.HqModule;
import com.creatorshq.master.module.ModuleRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Landing page: branding, welcome text and one card per registered module (enabled or not). */
public class DashboardScreen extends HqScreen {
    private record Card(int x, int y, int w, int h, HqModule module) {}
    private final List<Card> cards = new ArrayList<>();

    public DashboardScreen() { super("Dashboard", "dashboard"); }

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        drawBackdrop(g);
        Theme t = theme();
        cards.clear();

        g.drawString(font, brand(), 16, contentTop() + 4, t.accent());
        g.drawString(font, "Welcome back. Pick a tool below or press "
                + KeybindManager.get(KeybindManager.MASTER_ID).mapping().getTranslatedKeyMessage().getString()
                + " anywhere to open this hub.", 16, contentTop() + 18, t.textDim());
        g.drawString(font, "v" + CreatorsHq.VERSION + "  -  Minecraft " + CreatorsHq.MC_VERSION, 16, contentTop() + 30, t.textDim());

        int top = contentTop() + 48;
        int cw = 150, ch = 62, gap = 8;
        int perRow = Math.max(1, (width - 32 + gap) / (cw + gap));
        int i = 0;
        for (HqModule m : ModuleRegistry.all()) {
            int x = 16 + (i % perRow) * (cw + gap);
            int y = top + (i / perRow) * (ch + gap);
            i++;
            boolean enabled = ConfigManager.isEnabled(m.id());
            boolean hover = mx >= x && mx < x + cw && my >= y && my < y + ch;
            g.fill(x - 1, y - 1, x + cw + 1, y + ch + 1, hover && enabled ? t.accent() : t.border());
            g.fill(x, y, x + cw, y + ch, hover && enabled ? t.panelAlt() : t.panel());
            drawIcon(g, m.iconItemId(), x + 6, y + 6);
            g.drawString(font, font.plainSubstrByWidth(m.displayName(), cw - 34), x + 28, y + 8, enabled ? t.text() : t.textDim());
            String status = !enabled ? "Disabled" : m.isMigrated() ? "Enabled" : "Pending migration";
            int sc = !enabled ? t.danger() : m.isMigrated() ? t.success() : t.textDim();
            g.drawString(font, status, x + 28, y + 20, sc);
            g.drawString(font, font.plainSubstrByWidth(m.description(), cw - 12), x + 6, y + 36, t.textDim());
            g.drawString(font, "Open", x + 6, y + ch - 12, enabled ? t.accent() : t.textDim());
            cards.add(new Card(x, y, cw, ch, m));
        }
        drawChrome(g, mx, my);
    }

    private void drawIcon(GuiGraphics g, String itemId, int x, int y) {
        var item = BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId));
        g.renderItem(new ItemStack(item), x, y);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (handleChromeClick(event.x(), event.y(), event.button())) return true;
        if (event.button() == 0) {
            for (Card c : cards) {
                if (event.x() >= c.x() && event.x() < c.x() + c.w() && event.y() >= c.y() && event.y() < c.y() + c.h()
                        && ConfigManager.isEnabled(c.module().id())) {
                    minecraft.setScreen(c.module().createScreen());
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}
