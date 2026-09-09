package andrews.online_detector.registry;

import andrews.online_detector.block_entities.OnlineDetectorBlockEntity;
import andrews.online_detector.block_entities.AdvancedOnlineDetectorBlockEntity;
import andrews.online_detector.util.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ODBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Reference.MODID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OnlineDetectorBlockEntity>> ONLINE_DETECTOR =
            BLOCK_ENTITY_TYPES.register("online_detector", () -> BlockEntityType.Builder.of(
                    OnlineDetectorBlockEntity::new, ODBlocks.ONLINE_DETECTOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedOnlineDetectorBlockEntity>> ADVANCED_ONLINE_DETECTOR =
            BLOCK_ENTITY_TYPES.register("advanced_online_detector", () -> BlockEntityType.Builder.of(
                    AdvancedOnlineDetectorBlockEntity::new, ODBlocks.ADVANCED_ONLINE_DETECTOR.get()).build(null));
    private ODBlockEntities() {}
}
