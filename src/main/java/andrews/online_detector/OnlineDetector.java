package andrews.online_detector;

import andrews.online_detector.config.ODConfigs;
import andrews.online_detector.network.ODNetwork;
import andrews.online_detector.registry.ODBlocks;
import andrews.online_detector.registry.ODBlockEntities;
import andrews.online_detector.registry.ODItems;
import andrews.online_detector.util.Reference;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(Reference.MODID)
public final class OnlineDetector {
    public OnlineDetector(IEventBus bus, ModContainer container) {
        ODBlocks.BLOCKS.register(bus);
        ODItems.ITEMS.register(bus);
        ODBlockEntities.BLOCK_ENTITY_TYPES.register(bus);
        bus.addListener(ODNetwork::register);
        bus.addListener(OnlineDetector::addCreativeItems);
        ODConfigs.register(container);
    }
    private static void addCreativeItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.REDSTONE_BLOCKS)) {
            event.accept(ODBlocks.ONLINE_DETECTOR.get());
            event.accept(ODBlocks.ADVANCED_ONLINE_DETECTOR.get());
        }
    }
}
