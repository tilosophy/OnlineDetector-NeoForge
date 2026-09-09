package andrews.online_detector.gametest;

import java.util.UUID;
import andrews.online_detector.block_entities.OnlineDetectorBlockEntity;
import andrews.online_detector.objects.blocks.OnlineDetectorBlock;
import andrews.online_detector.registry.ODBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("online_detector")
@PrefixGameTestTemplate(false)
public final class DetectorGameTests {
    @GameTest(template = "empty")
    public static void signalAndLight(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        var level = helper.getLevel();
        for (var block : new OnlineDetectorBlock[] {ODBlocks.ONLINE_DETECTOR.get(), ODBlocks.ADVANCED_ONLINE_DETECTOR.get()}) {
            for (boolean active : new boolean[] {false, true}) {
                for (boolean inverted : new boolean[] {false, true}) {
                    var state = block.defaultBlockState().setValue(OnlineDetectorBlock.IS_ACTIVE, active)
                            .setValue(OnlineDetectorBlock.IS_INVERTED, inverted);
                    level.setBlock(pos, state, Block.UPDATE_ALL);
                    for (Direction direction : Direction.values()) {
                        int expected = active != inverted && direction != Direction.DOWN ? 15 : 0;
                        helper.assertTrue(state.getSignal(level, pos, direction) == expected, "Incorrect directional redstone output");
                    }
                    helper.assertTrue(state.getLightEmission(level, pos) == (active != inverted ? 7 : 0), "Incorrect emitted light");
                    helper.assertTrue(state.isSignalSource(), "Detector must advertise its redstone connection");
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void unassignedInvertedPowersLamp(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        level.setBlock(pos.east(), Blocks.REDSTONE_LAMP.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(pos, ODBlocks.ADVANCED_ONLINE_DETECTOR.get().defaultBlockState()
                .setValue(OnlineDetectorBlock.IS_INVERTED, true), Block.UPDATE_ALL);
        helper.succeedWhen(() -> helper.assertTrue(level.getBlockState(pos.east()).getValue(RedstoneLampBlock.LIT),
                "Inverted unassigned detector should power adjacent lamp"));
    }

    @GameTest(template = "empty")
    public static void targetPersistenceAndStaleState(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        var state = ODBlocks.ADVANCED_ONLINE_DETECTOR.get().defaultBlockState().setValue(OnlineDetectorBlock.IS_ACTIVE, true);
        level.setBlock(pos, state, Block.UPDATE_ALL);
        var detector = (OnlineDetectorBlockEntity) level.getBlockEntity(pos);
        UUID target = UUID.fromString("12345678-1234-1234-1234-123456789012");
        level.getChunkAt(pos).setUnsaved(false);
        detector.setOwner(target, "TestPlayer");
        helper.assertTrue(level.getChunkAt(pos).isUnsaved(), "Target edits must mark the chunk for saving");
        CompoundTag saved = detector.saveWithoutMetadata(level.registryAccess());
        level.removeBlock(pos, false);
        level.setBlock(pos, state, Block.UPDATE_ALL);
        var restored = (OnlineDetectorBlockEntity) level.getBlockEntity(pos);
        restored.loadWithComponents(saved, level.registryAccess());
        helper.assertTrue(target.equals(restored.getOwnerUUID()) && "TestPlayer".equals(restored.getOwnerName()), "Target must survive serialization");
        restored.refreshOnlineState();
        helper.assertTrue(!level.getBlockState(pos).getValue(OnlineDetectorBlock.IS_ACTIVE), "Offline target must clear stale active state");
        restored.loadWithComponents(new CompoundTag(), level.registryAccess());
        helper.assertTrue(restored.getOwnerUUID() == null && restored.getOwnerName() == null, "Missing fields must clear previous identity");
        helper.succeed();
    }
}
