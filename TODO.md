# TODO

## Milestone 2 — make it compile (BLOCKER for everything else)
- [ ] Add Gradle wrapper (`gradle wrapper --gradle-version <9.x>`), confirm Loom/loader/Fabric API versions on fabricmc.net/develop
- [ ] `./gradlew build` on a machine with network; fix errors, starting with `client/Compat.java`
- [ ] Launch client on 26.1.2: master key opens Dashboard, tabs switch, theme persists

## Modules
- [x] Framework: registry, themes, config, keybinds, dashboard, settings
- [x] Easy TP (ported, **untested in game**)
- [ ] Easy Gamemode
- [ ] Armour Trim & Ore Customiser
- [ ] Easy Potions
- [ ] Easy Enchant
- [ ] Easy Give (merge its ThemeManager into master theme)
- [ ] Easy Announce

## Framework gaps
- [ ] Per-module settings screens (`createSettingsScreen`) + button on each module screen
- [ ] Actually apply UI scale / animations / compact layout (values are stored but unused)
- [ ] Config import/export
- [ ] Migrate old per-mod configs (Announce, Enchant, Give) — never overwrite
- [ ] Nav bar overflow (tabs beyond width are hidden; reachable via Dashboard)
- [ ] Module screens must keep content below `HqScreen.NAV_H`
- [ ] Full test pass (spec section 15, items 1–17)
