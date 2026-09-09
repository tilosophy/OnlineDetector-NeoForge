package andrews.online_detector.screens.buttons;

import andrews.online_detector.block_entities.AdvancedOnlineDetectorBlockEntity;
import andrews.online_detector.network.ODNetwork;
import andrews.online_detector.screens.menus.AdvancedOnlineDetectorScreen;
import andrews.online_detector.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public final class AvailablePlayerButton extends Button {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/menus/advanced_online_detector_menu.png");
    private final PlayerInfo info;
    public AvailablePlayerButton(AdvancedOnlineDetectorBlockEntity detector, PlayerInfo info, int x, int y, AdvancedOnlineDetectorScreen screen) {
        super(x, y, 165, 12, Component.literal(info.getProfile().getName()), button -> {
            PacketDistributor.sendToServer(new ODNetwork.SelectPlayer(detector.getBlockPos(), info.getProfile().getId(), screen.getSessionToken()));
            screen.onClose();
        }, DEFAULT_NARRATION);
        this.info = info;
    }
    @Override public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(TEXTURE, getX(), getY(), 0, isHoveredOrFocused() ? 143 : 131, width, height);
        graphics.drawString(Minecraft.getInstance().font, getMessage(), getX() + 12, getY() + 2, 0, false);
        graphics.blit(info.getSkin().texture(), getX() + 1, getY() + 1, 10, 10, 8, 8, 8, 8, 64, 64);
    }
}
