package com.creatorshq.master.client;

import com.creatorshq.master.CreatorsHq;
import com.creatorshq.master.client.ui.DashboardScreen;
import com.creatorshq.master.core.ConfigManager;
import com.creatorshq.master.module.HqModule;
import com.creatorshq.master.module.ModuleRegistry;
import com.creatorshq.master.modules.easytp.EasyTpModule;
import com.creatorshq.master.modules.pending.PendingModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

public class CreatorsHqClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ConfigManager.init(FabricLoader.getInstance().getConfigDir());

        // ---- module registration: the ONLY place a new module needs to be added ----
        ModuleRegistry.register(new PendingModule("easyannounce", "Easy Announce",
                "Titles, subtitles, actionbar, chat, countdowns, bossbar.", "minecraft:bell", 75));          // K
        ModuleRegistry.register(new PendingModule("trimore", "Armour Trim & Ore Customiser",
                "Customise armour trims and ore appearance.", "minecraft:netherite_chestplate", 77));        // M
        ModuleRegistry.register(new EasyTpModule());                                                          // P
        ModuleRegistry.register(new PendingModule("easygamemode", "Easy Gamemode",
                "One-click gamemode for you or everyone.", "minecraft:grass_block", 71));                   // G
        ModuleRegistry.register(new PendingModule("easyenchant", "Easy Enchant",
                "Enchant, rename and disenchant held items.", "minecraft:enchanted_book", 72));             // H (was G)
        ModuleRegistry.register(new PendingModule("easygive", "Easy Give",
                "Browse every item and give it to players.", "minecraft:chest", 74));                       // J (was G)
        ModuleRegistry.register(new PendingModule("easypotions", "Easy Potions",
                "Apply effects with duration and amplifier.", "minecraft:potion", 79));                     // O

        KeybindManager.registerAll();

        ClientTickEvents.END_CLIENT_TICK.register(CreatorsHqClient::onTick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> {
            for (HqModule m : ModuleRegistry.all()) {
                try { m.shutdown(); } catch (Throwable t) { CreatorsHq.LOGGER.error("shutdown failed: {}", m.id(), t); }
            }
            ConfigManager.save();
        });
        CreatorsHq.LOGGER.info("{} {} loaded with {} modules", CreatorsHq.NAME, CreatorsHq.VERSION, ModuleRegistry.all().size());
    }

    private static void onTick(Minecraft mc) {
        if (mc.player == null || mc.screen != null) {
            // still drain presses so they don't fire later
            for (KeybindManager.Entry e : KeybindManager.entries()) while (e.mapping().consumeClick()) { /* discard */ }
            return;
        }
        while (KeybindManager.get(KeybindManager.MASTER_ID).mapping().consumeClick()) {
            mc.setScreen(new DashboardScreen());
        }
        for (HqModule m : ModuleRegistry.enabled()) {
            while (KeybindManager.get(m.id()).mapping().consumeClick()) {
                try {
                    mc.setScreen(m.createScreen());
                } catch (Throwable t) { // isolate a broken module
                    CreatorsHq.LOGGER.error("Module '{}' failed to open", m.id(), t);
                }
            }
        }
    }
}
