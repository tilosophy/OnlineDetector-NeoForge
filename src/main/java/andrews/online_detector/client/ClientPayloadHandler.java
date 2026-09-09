package andrews.online_detector.client;

import andrews.online_detector.block_entities.AdvancedOnlineDetectorBlockEntity;
import andrews.online_detector.network.ODNetwork;
import andrews.online_detector.screens.menus.AdvancedOnlineDetectorScreen;
import net.minecraft.client.Minecraft;

public final class ClientPayloadHandler {
    public static void open(ODNetwork.OpenScreen payload) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.level.getBlockEntity(payload.pos()) instanceof AdvancedOnlineDetectorBlockEntity detector) {
            minecraft.setScreen(new AdvancedOnlineDetectorScreen(detector, payload.token()));
        }
    }
    private ClientPayloadHandler() {}
}
