package andrews.online_detector.block_entities;

import andrews.online_detector.registry.ODBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class AdvancedOnlineDetectorBlockEntity extends OnlineDetectorBlockEntity {
    public AdvancedOnlineDetectorBlockEntity(BlockPos pos, BlockState state) {
        super(ODBlockEntities.ADVANCED_ONLINE_DETECTOR.get(), pos, state);
    }
}
