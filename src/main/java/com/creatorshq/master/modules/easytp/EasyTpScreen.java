package com.creatorshq.master.modules.easytp;

import com.creatorshq.master.client.Compat;
import com.creatorshq.master.client.ui.HqScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Easy TP GUI (port of net.easytp.EasyTpScreen).
 * LEFT = WHO gets teleported (EVERYONE row on top, multi-select). RIGHT = WHERE (one destination).
 * Left list: click = teleport now; Shift+click = toggle selection; click a selected player (2+ selected) = teleport all.
 */
public class EasyTpScreen extends HqScreen {
    private static final int ROW_H = 22;
    private static final int HEAD = 12;
    private static final int COLOR_PANEL = 0xCC101014;
    private static final int COLOR_TARGET = 0x6638C060, COLOR_TARGET_BORDER = 0xFF4ADE80;
    private static final int COLOR_DEST = 0x663B82F6, COLOR_DEST_BORDER = 0xFF60A5FA;
    private static final int COLOR_EVERYONE_IDLE = 0x33FFC83D, COLOR_EVERYONE_ACTIVE = 0x99FFC83D, COLOR_EVERYONE_BORDER = 0xFFFFC83D;

    /** name == null means the EVERYONE row. */
    private record Row(String name, PlayerInfo entry, int x, int y, int w, boolean left) {}

    private final List<PlayerInfo> players = new ArrayList<>();
    private final List<String> playerNames = new ArrayList<>();
    private final List<Row> rows = new ArrayList<>();
    private String destination = null;
    private final Set<String> targets = new LinkedHashSet<>();
    private int leftScroll = 0, rightScroll = 0;
    private int colW, leftX, rightX, listTop, listBottom, visibleRows;

    public EasyTpScreen() { super("Easy TP", EasyTpModule.ID); }

    @Override
    protected void init() {
        colW = Math.max(100, Math.min(180, (width - 60) / 2));
        int gap = 20;
        leftX = width / 2 - gap / 2 - colW;
        rightX = width / 2 + gap / 2;
        listTop = NAV_H + 50;
        listBottom = Math.max(listTop + ROW_H, height - 60);
        visibleRows = Math.max(1, (listBottom - listTop) / ROW_H);
        refreshPlayers();
        rebuild();
    }

    private void rebuild() {
        clearWidgets();
        rows.clear();
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
                .bounds(width / 2 - 40, height - 30, 80, 20).build());
        buildColumn(true);
        buildColumn(false);
    }

    private int itemCount(boolean left) {
        if (players.isEmpty()) return 0;
        return left ? players.size() + 1 : players.size();
    }

    private void buildColumn(boolean left) {
        int count = itemCount(left);
        int offset = left ? leftScroll : rightScroll;
        int x = left ? leftX : rightX;
        for (int i = 0; i < visibleRows; i++) {
            int idx = offset + i;
            if (idx >= count) break;
            int y = listTop + i * ROW_H;
            final String name = left ? (idx == 0 ? null : playerNames.get(idx - 1)) : playerNames.get(idx);
            final PlayerInfo entry = name == null ? null : players.get(left ? idx - 1 : idx);
            Button button = Button.builder(Component.empty(), b -> {
                        if (!left) onDestinationClicked(name);
                        else if (name == null) onEveryoneClicked();
                        else onTargetClicked(name);
                    }).bounds(x, y, colW, ROW_H - 2).build();
            addRenderableWidget(button);
            rows.add(new Row(name, entry, x, y, colW, left));
        }
    }

    /** @return true if the online player list changed. */
    private boolean refreshPlayers() {
        ClientPacketListener handler = Minecraft.getInstance().getConnection();
        List<PlayerInfo> fresh = new ArrayList<>();
        if (handler != null) fresh.addAll(handler.getOnlinePlayers());
        fresh.sort(Comparator.comparing(e -> e.getProfile().name().toLowerCase()));
        List<String> freshNames = new ArrayList<>();
        for (PlayerInfo e : fresh) freshNames.add(e.getProfile().name());
        if (freshNames.equals(playerNames)) return false;

        players.clear(); players.addAll(fresh);
        playerNames.clear(); playerNames.addAll(freshNames);
        if (destination != null && !playerNames.contains(destination)) destination = null;
        targets.retainAll(playerNames);
        leftScroll = clampScroll(leftScroll, itemCount(true));
        rightScroll = clampScroll(rightScroll, itemCount(false));
        return true;
    }

    private int clampScroll(int value, int count) {
        return Math.max(0, Math.min(Math.max(0, count - visibleRows), value));
    }

    private boolean everyoneSelected() { return !playerNames.isEmpty() && targets.containsAll(playerNames); }

    @Override
    public void tick() {
        super.tick();
        if (refreshPlayers()) rebuild();
    }

    private static boolean isShiftDown() {
        long window = GLFW.glfwGetCurrentContext();
        if (window == 0L) return false;
        return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
    }

    private void onDestinationClicked(String name) {
        if (!playerNames.contains(name)) return;
        destination = name.equals(destination) ? null : name;
        EasyTpState.clearStatus();
    }

    private void onTargetClicked(String name) {
        if (!playerNames.contains(name)) return;
        if (isShiftDown()) {
            if (!targets.remove(name)) targets.add(name);
            return;
        }
        if (targets.contains(name) && targets.size() > 1) teleport(new ArrayList<>(targets));
        else teleport(List.of(name));
    }

    private void onEveryoneClicked() {
        if (playerNames.isEmpty()) return;
        if (isShiftDown()) {
            if (everyoneSelected()) targets.clear(); else targets.addAll(playerNames);
            return;
        }
        targets.addAll(playerNames);
        teleport(new ArrayList<>(playerNames));
    }

    private void teleport(List<String> names) {
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() == null || client.player == null) return;
        if (destination == null) { EasyTpState.setStatus("Select a destination on the right first", true); return; }
        if (!playerNames.contains(destination)) {
            destination = null;
            EasyTpState.setStatus("Destination player is no longer online", true);
            return;
        }
        if (names.isEmpty()) { EasyTpState.setStatus("Select a player to teleport", true); return; }

        int sent = 0;
        String last = null;
        for (String target : new LinkedHashSet<>(names)) { // LinkedHashSet: never send the same command twice
            if (!playerNames.contains(target)) continue;      // went offline
            if (target.equals(destination)) continue;         // already there: harmless no-op
            EasyTpState.markCommandSent();
            Compat.sendCommand("tp " + target + " " + destination);
            sent++;
            last = target;
        }
        if (sent == 1) EasyTpState.setStatus("/tp " + last + " " + destination, false);
        else if (sent > 1) EasyTpState.setStatus("Sent " + sent + " teleports to " + destination, false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount == 0) return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        int delta = verticalAmount > 0 ? -1 : 1;
        boolean left = mouseX < width / 2.0;
        int count = itemCount(left);
        if (count <= visibleRows) return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        if (left) leftScroll = clampScroll(leftScroll + delta, count);
        else rightScroll = clampScroll(rightScroll + delta, count);
        rebuild();
        return true;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float delta) {
        drawBackdrop(g);
        drawPanel(g, leftX, "Teleport");
        drawPanel(g, rightX, "Destination");
    }

    private void drawPanel(GuiGraphics g, int x, String title) {
        g.fill(x - 4, listTop - 20, x + colW + 4, listBottom + 4, COLOR_PANEL);
        g.drawString(font, Component.literal(title), x + 2, listTop - 14, 0xFFFFFFFF);
    }

    private static void drawOutline(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y + 1, x + 1, y + h - 1, color);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);

        Component hint = Component.literal("Pick a destination on the right, then click who to teleport (Shift+click = multi-select)");
        g.drawString(font, hint, width / 2 - font.width(hint) / 2, NAV_H + 8, 0xFFAAAAAA);

        String self = Compat.playerName();
        int h = ROW_H - 2;
        for (Row row : rows) {
            if (row.name() == null) { // EVERYONE row
                boolean active = everyoneSelected();
                g.fill(row.x(), row.y(), row.x() + row.w(), row.y() + h, active ? COLOR_EVERYONE_ACTIVE : COLOR_EVERYONE_IDLE);
                drawOutline(g, row.x(), row.y(), row.w(), h, COLOR_EVERYONE_BORDER);
                Component label = Component.literal("EVERYONE (" + playerNames.size() + ")")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
                g.drawString(font, label, row.x() + row.w() / 2 - font.width(label) / 2, row.y() + (h - 8) / 2, 0xFFFFFFFF);
                continue;
            }
            boolean selected = row.left() ? targets.contains(row.name()) : row.name().equals(destination);
            if (selected) {
                g.fill(row.x(), row.y(), row.x() + row.w(), row.y() + h, row.left() ? COLOR_TARGET : COLOR_DEST);
                drawOutline(g, row.x(), row.y(), row.w(), h, row.left() ? COLOR_TARGET_BORDER : COLOR_DEST_BORDER);
            }
            Compat.drawFace(g, row.entry(), row.x() + 5, row.y() + (h - HEAD) / 2, HEAD);
            String label = row.name().equalsIgnoreCase(self) ? row.name() + " (you)" : row.name();
            g.drawString(font, Component.literal(label), row.x() + 5 + HEAD + 5, row.y() + (h - 8) / 2, 0xFFFFFFFF);
        }

        if (players.isEmpty()) {
            Component empty = Component.literal("No players online");
            g.drawString(font, empty, leftX + colW / 2 - font.width(empty) / 2, listTop + 8, 0xFF888888);
            g.drawString(font, empty, rightX + colW / 2 - font.width(empty) / 2, listTop + 8, 0xFF888888);
        }
        drawScrollbar(g, leftX, leftScroll, itemCount(true));
        drawScrollbar(g, rightX, rightScroll, itemCount(false));

        String status = EasyTpState.statusText();
        if (!status.isEmpty()) {
            Component c = Component.literal(status);
            g.drawString(font, c, width / 2 - font.width(c) / 2, height - 48, EasyTpState.isStatusError() ? 0xFFFF5555 : 0xFF55FF55);
        }
        drawChrome(g, mouseX, mouseY); // last, so the nav bar is never covered
    }

    private void drawScrollbar(GuiGraphics g, int x, int scroll, int count) {
        if (count <= visibleRows) return;
        int trackH = visibleRows * ROW_H;
        int barH = Math.max(12, trackH * visibleRows / count);
        int barY = listTop + (trackH - barH) * scroll / (count - visibleRows);
        g.fill(x + colW + 1, barY, x + colW + 3, barY + barH, 0xFF888888);
    }
}
