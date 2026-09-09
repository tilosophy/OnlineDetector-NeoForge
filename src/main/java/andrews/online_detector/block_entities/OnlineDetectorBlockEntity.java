package andrews.online_detector.block_entities;

import java.util.Objects;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import andrews.online_detector.config.ODConfigs;
import andrews.online_detector.objects.blocks.OnlineDetectorBlock;
import andrews.online_detector.registry.ODBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class OnlineDetectorBlockEntity extends BlockEntity {
    @Nullable private UUID ownerID;
    @Nullable private String ownerName;

    public OnlineDetectorBlockEntity(BlockPos pos, BlockState state) {
        this(ODBlockEntities.ONLINE_DETECTOR.get(), pos, state);
    }
    protected OnlineDetectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    public static void tick(Level level, BlockPos pos, BlockState state, OnlineDetectorBlockEntity detector) {
        if (level instanceof ServerLevel && level.getGameTime() % ODConfigs.ONLINE_CHECK_FREQUENCY.get() == 0) {
            detector.refreshOnlineState();
        }
    }
    public void refreshOnlineState() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        boolean online = ownerID != null && serverLevel.getServer().getPlayerList().getPlayer(ownerID) != null;
        BlockState state = getBlockState();
        if (state.getValue(OnlineDetectorBlock.IS_ACTIVE) != online) {
            serverLevel.setBlock(worldPosition, state.setValue(OnlineDetectorBlock.IS_ACTIVE, online), Block.UPDATE_ALL);
        }
    }
    /** Assign identity atomically. Never accept a client-supplied name or head item. */
    public void setOwner(UUID id, String name) {
        if (Objects.equals(ownerID, id) && Objects.equals(ownerName, name)) return;
        ownerID = id;
        ownerName = name;
        setChanged();
        if (level instanceof ServerLevel) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
    @Nullable public UUID getOwnerUUID() { return ownerID; }
    @Nullable public String getOwnerName() { return ownerName; }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag values = new CompoundTag();
        if (ownerID != null) values.putUUID("OwnerID", ownerID);
        if (ownerName != null) values.putString("OwnerName", ownerName);
        tag.put("OnlineDetectorValues", values);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        CompoundTag values = tag.getCompound("OnlineDetectorValues");
        // Clear missing values too, so a client cannot retain a previous target.
        ownerID = values.hasUUID("OwnerID") ? values.getUUID("OwnerID") : null;
        ownerName = values.contains("OwnerName", 8) ? values.getString("OwnerName") : null;
        // Legacy OwnerHead is intentionally ignored: it is presentation, not authoritative data.
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
