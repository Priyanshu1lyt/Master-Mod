# Changelog

## 0.1.0-dev — Milestone 1 (2026-11-27)
- Analysed all seven 1.21.11 source projects; wrote `INTEGRATION_MAP.md`.
- New unified project for Minecraft 26.1.2 (Loom `net.fabricmc.fabric-loom`, no mappings, Java 25).
- Core: 5 themes + ThemeManager, JSON config with validation/corruption backup, key-conflict detection.
- Module registry/contract; dashboard + dynamic nav bar + settings (Appearance, Keybinds, General, About).
- Keybinds: master (RIGHT SHIFT) + one per module; default G conflict between Gamemode/Enchant/Give resolved (G/H/J).
- Easy TP ported to Mojang names as the reference module.
- Original sources preserved under `reference/original-1.21.11/` (local copy; see HANDOFF).
