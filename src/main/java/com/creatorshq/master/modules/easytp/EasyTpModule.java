package com.creatorshq.master.modules.easytp;

import com.creatorshq.master.module.HqModule;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.gui.screens.Screen;

import java.util.Locale;

/** Easy TP, migrated from standalone mod "easytp" (1.21.11). Behaviour unchanged; default key P preserved. */
public final class EasyTpModule implements HqModule {
    public static final String ID = "easytp";

    @Override public String id() { return ID; }
    @Override public String displayName() { return "Easy TP"; }
    @Override public String description() { return "Teleport online players to another player in one click."; }
    @Override public String iconItemId() { return "minecraft:ender_pearl"; }
    @Override public int defaultKey() { return 80; } // GLFW_KEY_P (original default)
    @Override public Screen createScreen() { return new EasyTpScreen(); }

    @Override
    public void init() {
        // If the server answers our /tp with an error shortly after we sent it, surface it in the GUI.
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (System.currentTimeMillis() - EasyTpState.lastSendTime() > EasyTpState.ERROR_WINDOW_MS) return;
            String text = message.getString().toLowerCase(Locale.ROOT);
            if (text.contains("unknown or incomplete") || text.contains("permission")) {
                EasyTpState.setStatus("Server rejected /tp - you need OP / permission", true);
            } else if (text.contains("no player was found") || text.contains("no entity was found")) {
                EasyTpState.setStatus("Player not found on the server", true);
            }
        });
    }
}
