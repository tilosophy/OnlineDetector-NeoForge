package andrews.online_detector.registry;

import andrews.online_detector.objects.blocks.OnlineDetectorBlock;
import andrews.online_detector.objects.blocks.AdvancedOnlineDetectorBlock;
import andrews.online_detector.util.Reference;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ODBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Reference.MODID);
    public static final DeferredBlock<OnlineDetectorBlock> ONLINE_DETECTOR =
            BLOCKS.register("online_detector", () -> new OnlineDetectorBlock(OnlineDetectorBlock.properties()));
    public static final DeferredBlock<AdvancedOnlineDetectorBlock> ADVANCED_ONLINE_DETECTOR =
            BLOCKS.register("advanced_online_detector", () -> new AdvancedOnlineDetectorBlock(OnlineDetectorBlock.properties()));
    private ODBlocks() {}
}
