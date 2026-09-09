package andrews.online_detector.network;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import andrews.online_detector.block_entities.AdvancedOnlineDetectorBlockEntity;
import andrews.online_detector.client.ClientPayloadHandler;
import andrews.online_detector.util.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ODNetwork {
    // Main-thread-only, one short-lived selection session per player.
    private static final Map<ServerPlayer, Session> SESSIONS = new WeakHashMap<>();
    private record Session(AdvancedOnlineDetectorBlockEntity detector, UUID token, long expiresAt) {}
    private ODNetwork() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("2");
        registrar.playToClient(OpenScreen.TYPE, OpenScreen.STREAM_CODEC,
                (payload, context) -> ClientPayloadHandler.open(payload));
        registrar.playToServer(SelectPlayer.TYPE, SelectPlayer.STREAM_CODEC, ODNetwork::select);
    }
    public static boolean canEdit(ServerPlayer player, BlockPos pos) {
        var level = player.serverLevel();
        return level.hasChunkAt(pos) && !player.isSpectator() && player.getAbilities().mayBuild
                && player.canInteractWithBlock(pos, 0.0) && level.mayInteract(player, pos)
                && level.getBlockEntity(pos) instanceof AdvancedOnlineDetectorBlockEntity;
    }
    public static void open(ServerPlayer player, BlockPos pos) {
        if (!canEdit(player, pos)) return;
        var detector = (AdvancedOnlineDetectorBlockEntity) player.serverLevel().getBlockEntity(pos);
        UUID token = UUID.randomUUID();
        SESSIONS.put(player, new Session(detector, token, player.serverLevel().getGameTime() + 1200));
        PacketDistributor.sendToPlayer(player, new OpenScreen(pos, token));
    }
    private static void select(SelectPlayer payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        Session session = SESSIONS.remove(player);
        if (session == null || !session.token().equals(payload.token()) || !canEdit(player, payload.pos())) return;
        var level = player.serverLevel();
        if (level.getGameTime() > session.expiresAt() || session.detector() != level.getBlockEntity(payload.pos())) return;
        var target = level.getServer().getPlayerList().getPlayer(payload.playerId());
        if (target == null) return; // The selection list only offers connected players.
        session.detector().setOwner(target.getUUID(), target.getGameProfile().getName());
    }
    public static void forget(ServerPlayer player) { SESSIONS.remove(player); }
    public static void clearSessions() { SESSIONS.clear(); }

    public record OpenScreen(BlockPos pos, UUID token) implements CustomPacketPayload {
        public static final Type<OpenScreen> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "open_screen"));
        public static final StreamCodec<FriendlyByteBuf, OpenScreen> STREAM_CODEC = StreamCodec.of(
                (buf, value) -> { buf.writeBlockPos(value.pos()); buf.writeUUID(value.token()); },
                buf -> new OpenScreen(buf.readBlockPos(), buf.readUUID()));
        @Override public Type<OpenScreen> type() { return TYPE; }
    }
    public record SelectPlayer(BlockPos pos, UUID playerId, UUID token) implements CustomPacketPayload {
        public static final Type<SelectPlayer> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "select_player"));
        public static final StreamCodec<FriendlyByteBuf, SelectPlayer> STREAM_CODEC = StreamCodec.of(
                (buf, value) -> { buf.writeBlockPos(value.pos()); buf.writeUUID(value.playerId()); buf.writeUUID(value.token()); },
                buf -> new SelectPlayer(buf.readBlockPos(), buf.readUUID(), buf.readUUID()));
        @Override public Type<SelectPlayer> type() { return TYPE; }
    }
}
