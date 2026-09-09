package andrews.online_detector.registry;

import andrews.online_detector.util.Reference;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ODItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Reference.MODID);
    public static final DeferredItem<BlockItem> ONLINE_DETECTOR = ITEMS.registerSimpleBlockItem(ODBlocks.ONLINE_DETECTOR);
    public static final DeferredItem<BlockItem> ADVANCED_ONLINE_DETECTOR = ITEMS.registerSimpleBlockItem(ODBlocks.ADVANCED_ONLINE_DETECTOR);
    private ODItems() {}
}
