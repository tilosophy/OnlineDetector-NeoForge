package andrews.online_detector.screens.menus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import andrews.online_detector.block_entities.AdvancedOnlineDetectorBlockEntity;
import andrews.online_detector.screens.buttons.AvailablePlayerButton;
import andrews.online_detector.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class AdvancedOnlineDetectorScreen extends Screen {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/menus/advanced_online_detector_menu.png");
    private final AdvancedOnlineDetectorBlockEntity detector;
    private final UUID sessionToken;
    private List<PlayerInfo> players = List.of();
    private int currentPage = 1;
    public AdvancedOnlineDetectorScreen(AdvancedOnlineDetectorBlockEntity detector, UUID sessionToken) {
        super(Component.translatable("gui.online_detector.advanced_online_detector"));
        this.detector = detector;
        this.sessionToken = sessionToken;
    }
    @Override public boolean isPauseScreen() { return false; }
    @Override protected void init() {
        var connection = Minecraft.getInstance().getConnection();
        players = connection == null ? List.of() : new ArrayList<>(connection.getOnlinePlayers());
        if (!players.isEmpty()) players.sort(Comparator.comparing(p -> p.getProfile().getName(), String.CASE_INSENSITIVE_ORDER));
        currentPage = Math.min(currentPage, getTotalPages());
        rebuildButtons();
    }
    private void rebuildButtons() {
        clearWidgets();
        int x = (width - 177) / 2;
        int y = (height - 131) / 2;
        int start = (currentPage - 1) * 5;
        for (int i = start; i < Math.min(start + 5, players.size()); i++) {
            addRenderableWidget(new AvailablePlayerButton(detector, players.get(i), x + 6,
                    y + 51 + (i - start) * 12, this));
        }
        var previous = addRenderableWidget(Button.builder(Component.literal("<"), button -> {
            currentPage--; rebuildButtons();
        }).bounds(x + 5, y + 113, 14, 14).build());
        previous.active = currentPage > 1;
        var next = addRenderableWidget(Button.builder(Component.literal(">"), button -> {
            currentPage++; rebuildButtons();
        }).bounds(x + 158, y + 113, 14, 14).build());
        next.active = currentPage < getTotalPages();
    }
    public UUID getSessionToken() { return sessionToken; }
    private int getTotalPages() { return Math.max(1, (players.size() + 4) / 5); }
    @Override public void tick() {
        if (minecraft.level == null || minecraft.level.getBlockEntity(detector.getBlockPos()) != detector) onClose();
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = (width - 177) / 2;
        int y = (height - 131) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, 177, 131);
        graphics.drawCenteredString(font, title, width / 2, y + 6, 4210752);
        graphics.drawString(font, Component.translatable("gui.online_detector.tracking"), x + 5, y + 18, 0, false);
        if (detector.getOwnerName() != null) graphics.drawCenteredString(font, detector.getOwnerName(), width / 2, y + 29, 0xffffff);
        graphics.drawString(font, Component.translatable("gui.online_detector.available"), x + 5, y + 41, 0, false);
        graphics.drawCenteredString(font, currentPage + "/" + getTotalPages(), width / 2, y + 116, 0);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (minecraft.options.keyInventory.matches(keyCode, scanCode)) { onClose(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
