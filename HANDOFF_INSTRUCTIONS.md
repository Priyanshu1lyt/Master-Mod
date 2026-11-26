# HANDOFF_INSTRUCTIONS — read this first

## 1. What this is
**CREATORS HQ MASTER MOD**: ONE client-side Fabric mod for **Minecraft 26.1.2** that merges seven of the owner's mods behind a single dashboard/nav bar/settings/theme/keybind system. Future tools are added as new modules, never as new mods.

Owner's rules: preserve every original feature, no dummy implementations, do not claim compile/tests that did not happen, keep handing back the full project at every stage, ONE jar, never downgrade to 1.21.11.

## 2. Source mods (Minecraft 1.21.11, Yarn) → integration status
See `INTEGRATION_MAP.md`. The originals are NOT in this repo; the owner has them in `backup.zip` (upload it to the next Claude session together with this repo).
| Module | Status |
|---|---|
| Easy TP | Ported (uncompiled) |
| Easy Gamemode, Trim & Ore, Easy Potions, Easy Enchant, Easy Give, Easy Announce | **Not ported** — placeholder `PendingModule` registered in `CreatorsHqClient` |

## 3. Architecture
```
com.creatorshq.master
  CreatorsHq                 constants + logger
  core/                      PURE JAVA (no Minecraft imports; unit-tested)
    Theme, ThemeManager      5 themes; screens call ThemeManager.current() every frame -> instant switch
    HqConfig, ConfigValidator, ConfigManager (creators-hq.json, atomic save, corrupt backup)
    KeyConflicts
  module/  HqModule (contract), ModuleRegistry (ordered, failure-isolated)
  client/
    CreatorsHqClient         entrypoint; THE ONLY place modules are registered; tick -> keybinds
    KeybindManager           master key (RIGHT SHIFT) + one per module, registered once
    Compat                   every name-sensitive Minecraft call (fix compile errors HERE first)
    ui/ HqScreen (nav bar chrome, base of every screen), DashboardScreen, SettingsScreen, PendingModuleScreen
  modules/easytp/            reference module port (EasyTpModule, EasyTpScreen, EasyTpState)
  modules/pending/           PendingModule placeholder
```
Module screens **extend `HqScreen`**, keep content below `NAV_H`, call `drawChrome(...)` LAST in `render`, and call `handleChromeClick`/`super.mouseClicked` first in click handling. Tab switching = `minecraft.setScreen(newScreen)`.

## 4. Decisions made
- Client-only (`environment: client`); commands still go through vanilla `sendCommand`, server permissions unchanged.
- Vanilla `options.txt` is authoritative for key bindings; `creators-hq.json` mirrors them.
- Default-key conflict (G x3): Gamemode=G, Enchant=H, Give=J. Master=RIGHT SHIFT.
- Module config = `ConfigManager.moduleSettings(id)` (string map); richer configs (Announce/Enchant/Give) may keep their own file and migrate it; **never overwrite old configs**.
- Easy Give's own theme system must be replaced by the master `ThemeManager`.
- Unknown config keys (future modules) are preserved, not deleted.

## 5. Build setup (UNVERIFIED)
`build.gradle`: plugin id `net.fabricmc.fabric-loom`, no `mappings`, `implementation` not `modImplementation`, Java 25. `gradle.properties`: loader 0.19.3, Fabric API 0.155.0+26.1.2, Loom 1.15-SNAPSHOT. Confirm on fabricmc.net/develop. **No Gradle wrapper is included**: run `gradle wrapper --gradle-version <latest 9.x>` once.
```
./gradlew build          # jar -> build/libs/
./gradlew runClient
```

## 6. Yarn → Mojang name cheatsheet used
MinecraftClient→Minecraft, DrawContext→GuiGraphics, Screen(client→minecraft, textRenderer→font, currentScreen→screen), ButtonWidget→Button (`.dimensions`→`.bounds`), TextFieldWidget→EditBox (getText/setText→getValue/setValue), addDrawableChild→addRenderableWidget, clearChildren→clearWidgets, close()→onClose(), shouldPause→isPauseScreen, Text→Component, Formatting→ChatFormatting, KeyBinding→KeyMapping (wasPressed→consumeClick), PlayerListEntry→PlayerInfo, ClientPlayNetworkHandler→ClientPacketListener (sendChatCommand→sendCommand, getPlayerList→getOnlinePlayers), Identifier via `Compat.id`, `drawTextWithShadow/drawText`→`drawString`, GameMode→GameType, `BuiltInRegistries` for registries. Easy TP was hand-ported. For the three big mods (Announce, Enchant, Give) consider Fabric's recommended path: run Loom 1.21.11 `migrateMappings --mappings "net.minecraft:mappings:1.21.11"` on a COPY of each original (Yarn→Mojang), then apply the 26.1 changes.

## 7. Current compilation errors
Unknown — never compiled (see BUILD_STATUS.md).

## 8. EXACT NEXT TASK
1. On a machine WITH network: add the Gradle wrapper, run `./gradlew build`.
2. Fix compile errors, starting in `client/Compat.java`, then `HqScreen`/`DashboardScreen`/`SettingsScreen`/`EasyTpScreen`. Do not rewrite architecture.
3. `./gradlew runClient`; verify: RIGHT SHIFT opens Dashboard; tab switching; theme persistence; Easy TP works on a server.
4. Update BUILD_STATUS.md truthfully, then port the next module: Easy Gamemode → Potions → Trim & Ore → Enchant → Give → Announce. For each: take the original from backup.zip, apply the cheatsheet, extend `HqScreen`, replace the `PendingModule` line in `CreatorsHqClient` with the real module, update docs.
