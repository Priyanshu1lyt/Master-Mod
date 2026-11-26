package com.creatorshq.master.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/**
 * ALL calls whose name or signature is most likely to differ between 26.1.x builds live here.
 * UNVERIFIED against the real 26.1.2 jar (no network in the authoring sandbox): if the first compile
 * fails, fix it here once instead of in every screen. See HANDOFF_INSTRUCTIONS.md, "First task".
 */
public final class Compat {
    private Compat() {}

    public static Identifier id(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    public static KeyMapping.Category category(String namespace, String path) {
        return KeyMapping.Category.register(id(namespace, path));
    }

    public static KeyMapping newKey(String translationKey, int glfwKey, KeyMapping.Category cat) {
        return new KeyMapping(translationKey, InputConstants.Type.KEYSYM, glfwKey, cat);
    }

    public static void bindKey(KeyMapping mapping, int glfwKey) {
        mapping.setKey(glfwKey < 0 ? InputConstants.UNKNOWN : InputConstants.Type.KEYSYM.getOrCreate(glfwKey));
        KeyMapping.resetMapping();
        Minecraft.getInstance().options.save();
    }

    public static int boundCode(KeyMapping mapping) {
        return mapping.isUnbound() ? -1
                : net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper.getBoundKeyOf(mapping).getValue();
    }

    public static int defaultCode(KeyMapping mapping) { return mapping.getDefaultKey().getValue(); }

    public static String keyLabel(KeyMapping mapping) { return mapping.getTranslatedKeyMessage().getString(); }

    public static long windowHandle() { return Minecraft.getInstance().getWindow().handle(); }

    public static String playerName() { return Minecraft.getInstance().getUser().getName(); }

    public static void sendCommand(String commandWithoutSlash) {
        var conn = Minecraft.getInstance().getConnection();
        if (conn != null) conn.sendCommand(commandWithoutSlash);
    }

    public static void drawFace(GuiGraphics g, PlayerInfo info, int x, int y, int size) {
        PlayerFaceRenderer.draw(g, info.getSkin(), x, y, size);
    }
}
