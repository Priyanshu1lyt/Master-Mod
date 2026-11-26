# BUILD_STATUS

| Item | Status |
|---|---|
| Last build date | **none — never built** |
| Build command | `./gradlew build` (wrapper not included; see below) |
| Compilation succeeded | **No / not attempted.** Authoring sandbox had no network, so Minecraft 26.1.2, Loom and Fabric API could not be downloaded |
| JAR generated | **No** |
| Minecraft launch tested | **No** |

## What WAS verified
`src/main/java/com/creatorshq/master/core/{Theme,ThemeManager,KeyConflicts,HqConfig,ConfigValidator}.java` compile with JDK 21 and `tests/CoreLogicTest.java` passes 13/13:

    java -m jdk.compiler/com.sun.tools.javac.Main -d /tmp/out <those 5 files> tests/CoreLogicTest.java
    java -cp /tmp/out CoreLogicTest

## What is NOT verified
Everything that imports `net.minecraft.*` or `net.fabricmc.*` and `ConfigManager` (Gson). Class/method names were written from knowledge of Mojang mappings and Fabric's 26.1 porting notes; `Identifier`, `KeyMapping.Category.register`, `Window.handle()`, `ClientPacketListener.getOnlinePlayers()/sendCommand`, `PlayerInfo.getSkin()`, `PlayerFaceRenderer.draw`, `MouseButtonEvent`/`KeyEvent` and the GuiGraphics draw methods are the likeliest to need adjustment.
Dependency versions in `gradle.properties` come from a single third-party README and Fabric's porting docs — confirm them.
