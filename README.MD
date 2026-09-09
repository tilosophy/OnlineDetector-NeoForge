# Online Detector — unofficial NeoForge port

Port of [andrew0030's Online Detector](https://github.com/andrew0030/OnlineDetector), based on Forge version `1.20-6.0.0`, commit `4685e943eb6abc4a769b0bf7efb142cc1081960f`.

**Target:** Minecraft Java 1.21.1, NeoForge **21.1.248**, Java **21**.

## Status

Development port. See the branch's **Build and GameTests** workflow for compilation and engine-test results. A successful automated build does not establish full client rendering or multiplayer compatibility. Complete the manual checks in `TESTING.md` before deploying to a live server.

## Gameplay

- Basic detector binds to its placer. Advanced detector starts unassigned; right-click to select a currently online player (five per page).
- Sneak-right-click toggles inversion. Normal output is enabled when the target is online; inverted output is enabled when offline, including when unassigned.
- Online means connected to this server, regardless of dimension, distance, or AFK status.
- Checks every 20 ticks by default. Detectors do not load chunks and must be ticking to update.
- Output-enabled blocks emit light level 7 and redstone particles. Eye animation and portal particles indicate the target's online status independently of inversion.
- Normal breaking drops an unconfigured block. Basic detectors rebind when placed; advanced detectors reset. No owner-only access restriction is added.
- Original crafting ingredients, block IDs, textures, models, and translations are retained.

## Install

1. Back up the test world. Use Minecraft 1.21.1 with NeoForge 21.1.248 and Java 21.
2. Download the artifact from a successful GitHub Actions run and extract the ZIP.
3. Put `online_detector-1.21.1-7.0.0-neoforge.jar` in **both** the server and every player's `mods` folder. Do not install the `-sources.jar`, and do not install the old Forge JAR alongside it.
4. Restart the server and clients, then perform `TESTING.md` checks.

No world-generation changes are introduced; new chunks are not needed. This port does not promise safe whole-world migration from a Forge 1.20.1 modpack. Existing custom `OwnerID`/`OwnerName` fields retain their names, but legacy `OwnerHead` is ignored and rebuilt as client presentation.

## Configuration

- `config/online_detector-common.toml`: `[Frequency] onlineCheckFrequency = 20` (1–1200 ticks). The server's value controls detection. The ticker reads the current value rather than caching it in a static field.
- `config/online_detector-client.toml`: `[Particles] shouldShowRedstoneParticles = true`, `shouldShowPortalParticles = true`. Each client controls its own particles.

## Build

Install a 64-bit Java 21 JDK, then in the repository root run:

- Linux/macOS: `./gradlew build`
- Windows: `gradlew.bat build`
- Engine checks: `./gradlew runGameTestServer` (Windows: `gradlew.bat runGameTestServer`)
- Development client: `./gradlew runClient`

Output is in `build/libs/`. Gradle and required development dependencies are downloaded automatically. Development GameTests are excluded from the distributable mod JAR.

## Deliberate fixes and small changes

- Target changes explicitly mark the chunk for saving and synchronize UUID/name atomically.
- The server supplies the name for a connected target. Clients cannot submit arbitrary names or head item stacks.
- Selection requires a server-issued, single-use session for the same block entity (expires after 1200 ticks), normal interaction reach, build ability, and server interaction permission. Sessions clear on logout/server stop. Claims mods still require their own integration testing; permissions changed after opening a screen are not universally detectable.
- Head rendering uses 1.21.1 profile data components and a client cache. Rendering never sends selection/head packets.
- Both block types share one server-only ticker and use UUID lookup without allocating a player list.
- The detector explicitly advertises itself as a redstone signal source, improving dust connections. Directional `getSignal` values retain the original contract (`DOWN` query: zero; other queries: 15 when enabled). Strong power is not added.
- The player list is sorted by name. Only the current page's buttons are instantiated. Navigation uses standard accessible buttons.
- Eye animation uses world game time instead of dereferencing the local player.

## License

LGPL v3; see `LICENSE.txt`, `COPYING`, and `NOTICE.md`. This is an unofficial derivative, not an upstream release. Original author: andrew0030. Port repository: https://github.com/tilosophy/OnlineDetector-NeoForge.
