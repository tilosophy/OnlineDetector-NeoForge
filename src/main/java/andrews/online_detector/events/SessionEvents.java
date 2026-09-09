package andrews.online_detector.events;

import andrews.online_detector.network.ODNetwork;
import andrews.online_detector.util.Reference;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

@EventBusSubscriber(modid = Reference.MODID)
public final class SessionEvents {
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) ODNetwork.forget(player);
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { ODNetwork.clearSessions(); }
}
