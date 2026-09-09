package andrews.online_detector.client;

import andrews.online_detector.block_entities.model.EyeModel;
import andrews.online_detector.block_entities.render.OnlineDetectorBlockEntityRenderer;
import andrews.online_detector.registry.ODBlockEntities;
import andrews.online_detector.util.Reference;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Reference.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEvents {
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ODBlockEntities.ONLINE_DETECTOR.get(), OnlineDetectorBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ODBlockEntities.ADVANCED_ONLINE_DETECTOR.get(), OnlineDetectorBlockEntityRenderer::new);
    }
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(EyeModel.EYE_LAYER_LOCATION, EyeModel::createBodyLayer);
    }
}
