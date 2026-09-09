# Verification checklist

Target Minecraft 1.21.1 / NeoForge 21.1.248 / Java 21. Record the exact commit and JAR used.

## Automated engine checks

`./gradlew build runGameTestServer` compiles client and common code and runs a dedicated GameTest server. Tests cover both blocks' output/light truth tables, advertised redstone source status, an actual adjacent lamp powered by an inverted unassigned detector, target serialization, chunk dirty marking, clearing absent identity fields, and correcting stale online state for an offline UUID.

These tests do not simulate real login/logout, client rendering, claim integrations, or a full disk save/restart. CI success is not a multiplayer certification.

## Manual acceptance on a test instance

1. Install the normal JAR on server and clients. Confirm dedicated-server startup and two real clients joining. Check logs for registry, recipe, model, and networking errors.
2. Craft both recipes. Confirm Redstone creative-tab entries, correct textures, face orientation, eye, light, and pickaxe drops.
3. Player A places the basic detector. Verify redstone enables within the configured interval. With the detector chunk still ticking near player B, have A log out and back in; verify off/on. Repeat with A in another dimension and AFK.
4. Test all combinations of online/offline and normal/inverted for both blocks. An unassigned advanced detector must power only while inverted. Check six neighboring positions using dust, repeaters, lamps, and the actual modpack machine; distinguish weak output from strong power through solid blocks. Test inversion with empty and occupied hands, including offhand items.
5. Select A then B on an advanced detector while both are online. Save, stop cleanly, restart, and verify B is retained. Also unload/reload the chunk. This tests saving when the active block state did not change.
6. Open the advanced screen with six connected players; select the sixth player from page two. Verify no hidden button receives the click. Reopen after join/leave changes. Verify escape/inventory-key closing and resizing.
7. Confirm selected player faces render online and offline, update on selection, and work with multiple viewers. Disable each particle setting and verify only the corresponding particles stop.
8. Move outside interaction reach after opening, change dimensions, remove/replace the detector, let the session expire, or let the target log out before selection. Confirm no invalid target change occurs. Reopen to get a fresh session. For claim mods, check denied interactions and permission changes while a screen is already open.
9. Keep a detector's chunk unloaded while its player disconnects, then return. It must correct on the next scheduled check; no force-loading is expected.
10. Break and replace each block. Confirm basic rebinding and advanced reset, and confirm no target data is copied into normal drops.

## Rollback

Use a copied world first. For a live rollout, stop the server and back up the entire world plus mod/config lists before installing. If rollback is needed, stop again and restore that backup and matching mod set. Removing a content mod from an already-saved world can remove its blocks/items; restoring the backup is the reliable rollback.
