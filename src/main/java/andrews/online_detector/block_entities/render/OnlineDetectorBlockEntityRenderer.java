package andrews.online_detector.block_entities.render;

import andrews.online_detector.block_entities.OnlineDetectorBlockEntity;
import andrews.online_detector.block_entities.model.EyeModel;
import andrews.online_detector.objects.blocks.OnlineDetectorBlock;
import andrews.online_detector.util.Reference;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;


public class OnlineDetectorBlockEntityRenderer<T extends OnlineDetectorBlockEntity> implements BlockEntityRenderer<T>
{
	public static final ResourceLocation EYE_TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/block/eye.png");
	private final EyeModel eyeModel;
    private final java.util.Map<OnlineDetectorBlockEntity, Head> heads = new java.util.WeakHashMap<>();
    private record Head(java.util.UUID id, String name, ItemStack stack) {}
	
	public OnlineDetectorBlockEntityRenderer(BlockEntityRendererProvider.Context context)
	{
		eyeModel = new EyeModel(context.bakeLayer(EyeModel.EYE_LAYER_LOCATION));
	}

	@Override
	public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay)
	{
		Direction facing = Direction.NORTH;
		if(blockEntity.hasLevel())
		{
			BlockState blockstate = blockEntity.getLevel().getBlockState(blockEntity.getBlockPos());
			if(blockstate.getBlock() instanceof OnlineDetectorBlock)
			{
				facing = blockstate.getValue(OnlineDetectorBlock.HORIZONTAL_FACING);
			}
		}

		poseStack.pushPose();
		poseStack.translate(0.5D, 0D, 0.5D);
		// Rotates the Head based on the Blocks facing direction
		switch(facing)
		{
			default:
			case NORTH:
				poseStack.mulPose(Axis.YN.rotationDegrees(90.0F));
				break;
			case SOUTH:
				poseStack.mulPose(Axis.YN.rotationDegrees(270.0F));
				break;
			case WEST:
				break;
			case EAST:
				poseStack.mulPose(Axis.YN.rotationDegrees(180.0F));
		}

		if(blockEntity.hasLevel())
		{
			BlockState blockstate = blockEntity.getLevel().getBlockState(blockEntity.getBlockPos());
			if(blockstate.getBlock() instanceof OnlineDetectorBlock)
			{
				renderEye(blockEntity.getLevel(), blockstate, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
			}
		}
		else
		{
			renderEye(blockEntity.getLevel(), partialTick, poseStack, bufferSource, packedLight, packedOverlay);
		}

		// Moves the player head to the center of the Blocks side
		poseStack.translate(0D, 0.0625 * 8, 0.0625 * -4);
		renderPlayerFace(blockEntity, poseStack, bufferSource, packedLight, packedOverlay);
		poseStack.popPose();
	}
	
	/**
	 * Renders the Ender Eye on top of the Online Detector Block
	 */
	private void renderEye(Level level, BlockState state, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay)
	{
		poseStack.pushPose();
		poseStack.scale(1.0F, -1.0F, -1.0F);
		
		if(state.getValue(OnlineDetectorBlock.IS_ACTIVE))
		{
			poseStack.mulPose(Axis.XP.rotationDegrees((float) Math.cos((level.getGameTime() + partialTick) / 4) * 2));
			poseStack.mulPose(Axis.ZP.rotationDegrees((float) Math.sin((level.getGameTime() + partialTick) / 4) * 2));
		}
		
		poseStack.translate(0.0D, -1.0D, 0.0D);
		poseStack.translate(0D, 0.0625 * -21, 0D);
		VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entitySolid(EYE_TEXTURE));
		eyeModel.renderToBuffer(poseStack, vertexconsumer, packedLight, packedOverlay, -1);
		poseStack.popPose();
	}
	
	/**
	 * Renders the Ender Eye on top of the Online Detector Block
	 */
	private void renderEye(Level level, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay)
	{
		poseStack.pushPose();
		poseStack.scale(1.0F, -1.0F, -1.0F);
		poseStack.translate(0.0D, -1.0D, 0.0D);
		poseStack.translate(0D, 0.0625 * -21, 0D);
		VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entitySolid(EYE_TEXTURE));
		eyeModel.renderToBuffer(poseStack, vertexconsumer, packedLight, packedOverlay, -1);
		poseStack.popPose();
	}
	
	/**
	 * Renders the Player face
	 */
    private void renderPlayerFace(OnlineDetectorBlockEntity detector, PoseStack poseStack,
            MultiBufferSource buffers, int light, int overlay) {
        var id = detector.getOwnerUUID();
        var name = detector.getOwnerName();
        if (id == null || name == null) {
            heads.remove(detector);
            return;
        }
        Head head = heads.get(detector);
        if (head == null || !id.equals(head.id()) || !name.equals(head.name())) {
            var profile = new com.mojang.authlib.GameProfile(id, name);
            var connection = Minecraft.getInstance().getConnection();
            var info = connection == null ? null : connection.getPlayerInfo(id);
            if (info != null) profile = info.getProfile();
            ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
            stack.set(net.minecraft.core.component.DataComponents.PROFILE,
                    new net.minecraft.world.item.component.ResolvableProfile(profile));
            head = new Head(id, name, stack);
            heads.put(detector, head);
        }
        Minecraft.getInstance().getItemRenderer().renderStatic(head.stack(), ItemDisplayContext.FIXED,
                light, overlay, poseStack, buffers, detector.getLevel(), (int) detector.getBlockPos().asLong());
    }
}
