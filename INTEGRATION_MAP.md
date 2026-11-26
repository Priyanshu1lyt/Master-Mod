# Integration map (source mods → master modules)

All sources are Fabric, Minecraft 1.21.11, **Yarn** mappings, Java 21.

| # | Module id | Source | Java lines | Default key (orig) | Master default key | Notes |
|---|---|---|---|---|---|---|
| 1 | easyannounce | com.easyannounce (easy-announce-template) | ~3190 | K | K | Many screens (Main, Normal, Countdown, StaticDisplay, Sound, ColorPicker), own config, own commands/bossbar/scoreboard managers. Own `ConfigManager`/`EasyAnnounceConfig` → move under `modules/easyannounce`, keep file format, add migration from old config path. |
| 2 | trimore | com.armortrim.customizer (Trim-set) | ~510 | M | M | 2 classes. |
| 3 | easytp | net.easytp (W-TP) | ~500 | P | P | **DONE** (ported). |
| 4 | easygamemode | com.notheartyt.easygamemode (W-GameMode) | ~510 | G | G | Controller + screen. Uses `Click`/`KeyInput` (1.21.9+ input API) already. |
| 5 | easyenchant | com.nebrion.easyenchants | ~2340 | G | **H** | Has history, selection, planner, own `EasyConfig`, FlatButton/Draw helpers. |
| 6 | easygive | com.easygive | ~2650 | G | **J** | Has own ThemeManager + SettingsScreen + EasyGiveConfig: **map its theme to the master ThemeManager**, keep its settings as the module settings. Extra `cancelKey` (unbound) → module-owned secondary key. |
| 7 | easypotions | com.easypotions (Postion-Easy) | ~560 | O | O | EasyScreen + client. |

## Conflicts found
- **Default key G is used by Gamemode, Enchant AND Give.** Resolved: Gamemode keeps G, Enchant → H, Give → J.
- Each mod has its own `Draw`/button helpers — different packages, so no class clash once moved under `modules/<id>/`.
- Language keys / asset namespaces differ (`easy-announce`, `easyenchant`, `easygive`…).
- Each mod defines its own `fabric.mod.json`, entrypoint, and (Announce/Give) a main `ModInitializer` — fold into `Module.init()`.

## Dependencies
Only Fabric Loader + Fabric API in every source. No third-party libs found.
