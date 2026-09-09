package andrews.online_detector.objects.blocks;

import andrews.online_detector.block_entities.AdvancedOnlineDetectorBlockEntity;
import andrews.online_detector.block_entities.OnlineDetectorBlockEntity;
import andrews.online_detector.network.ODNetwork;
import andrews.online_detector.registry.ODBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class AdvancedOnlineDetectorBlock extends OnlineDetectorBlock {
    public static final MapCodec<AdvancedOnlineDetectorBlock> CODEC = simpleCodec(AdvancedOnlineDetectorBlock::new);
    public AdvancedOnlineDetectorBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<AdvancedOnlineDetectorBlock> codec() { return CODEC; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedOnlineDetectorBlockEntity(pos, state);
    }
    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ODBlockEntities.ADVANCED_ONLINE_DETECTOR.get(), OnlineDetectorBlockEntity::tick);
    }
    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        // Advanced detectors deliberately start unassigned.
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) return super.useWithoutItem(state, level, pos, player, hit);
        if (player instanceof ServerPlayer serverPlayer && ODNetwork.canEdit(serverPlayer, pos)) {
            ODNetwork.open(serverPlayer, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
