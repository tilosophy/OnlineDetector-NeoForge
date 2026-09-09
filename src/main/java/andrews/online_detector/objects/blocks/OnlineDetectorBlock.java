package andrews.online_detector.objects.blocks;

import andrews.online_detector.block_entities.OnlineDetectorBlockEntity;
import andrews.online_detector.config.ODConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToIntFunction;

public class OnlineDetectorBlock extends BaseEntityBlock
{
    public static final com.mojang.serialization.MapCodec<OnlineDetectorBlock> CODEC = simpleCodec(OnlineDetectorBlock::new);
    @Override protected com.mojang.serialization.MapCodec<? extends OnlineDetectorBlock> codec() { return CODEC; }

    // Advertise weak redstone output, including connections to redstone dust.
    @Override public boolean isSignalSource(BlockState state) { return true; }

    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack, BlockState state,
            Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            useWithoutItem(state, level, pos, player, hit);
            return net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

	public static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty IS_ACTIVE = BooleanProperty.create("is_active");
	public static final BooleanProperty IS_INVERTED = BooleanProperty.create("is_inverted");
	protected static final VoxelShape BOTTOM_PART = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 13.0D, 16.0D);
	protected static final VoxelShape TOP_PART = Block.box(3.0D, 13.0D, 3.0D, 13.0D, 14.0D, 13.0D);
	protected static final VoxelShape AABB = Shapes.or(BOTTOM_PART, TOP_PART);
	private static final float PIXEL_SIZE = 0.0625F;
	
	public OnlineDetectorBlock(Properties properties)
	{
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(HORIZONTAL_FACING, Direction.NORTH).setValue(IS_ACTIVE, false).setValue(IS_INVERTED, false));
	}
	
	/**
	 * @return - The properties for this Block
	 */
	public static Properties properties()
	{
		Properties properties = Block.Properties.of();
		properties.mapColor(MapColor.STONE);
		properties.strength(1.5F, 6.0F);
		properties.requiresCorrectToolForDrops();
		properties.lightLevel(getLightValueLit(7));
		
		return properties;
	}

	@Override
	public RenderShape getRenderShape(BlockState pState)
	{
		return RenderShape.MODEL;
	}

	/**
	 * @param lightValue - The light value this Block should have if its active.
	 * @return - The light value this Block should have based on if its active or not.
	 */
	private static ToIntFunction<BlockState> getLightValueLit(int lightValue)
	{
		return (state) -> (state.getValue(IS_ACTIVE) && !state.getValue(IS_INVERTED)) || (!state.getValue(IS_ACTIVE) && state.getValue(IS_INVERTED)) ? lightValue : 0;
	}

	@Override
	public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction)
	{
		if((state.getValue(IS_ACTIVE) && !state.getValue(IS_INVERTED)) || (!state.getValue(IS_ACTIVE) && state.getValue(IS_INVERTED)))
		{
			return switch (direction)
			{
				case DOWN -> 0;
				case UP, NORTH, SOUTH, EAST, WEST -> 15;
			};
		}
		else
		{
			return 0;
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit)
	{
		if(player.isShiftKeyDown())
		{
			if (!level.isClientSide && level.mayInteract(player, pos) && player.getAbilities().mayBuild)
                level.setBlockAndUpdate(pos, state.cycle(IS_INVERTED));
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
	{
		if(!level.isClientSide && placer instanceof Player player)
		{
			BlockEntity blockEntity = level.getBlockEntity(pos);

			if(blockEntity instanceof OnlineDetectorBlockEntity onlineDetectorBlockEntity)
			{
				onlineDetectorBlockEntity.setOwner(player.getUUID(), player.getGameProfile().getName());
			}
		}
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new OnlineDetectorBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType)
	{
		return level.isClientSide ? null : createTickerHelper(blockEntityType, andrews.online_detector.registry.ODBlockEntities.ONLINE_DETECTOR.get(), OnlineDetectorBlockEntity::tick);
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
	{
		return AABB;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		return this.defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getClockWise()).setValue(IS_ACTIVE, false).setValue(IS_INVERTED, false);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(HORIZONTAL_FACING, IS_ACTIVE, IS_INVERTED);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random)
	{
		if((state.getValue(IS_ACTIVE) && !state.getValue(IS_INVERTED)) || (!state.getValue(IS_ACTIVE) && state.getValue(IS_INVERTED)))
		{
			if(ODConfigs.REDSTONE_PARTICLES.get())
			{
				level.addParticle(DustParticleOptions.REDSTONE, pos.getX() + PIXEL_SIZE * 8, pos.getY() + PIXEL_SIZE * 2, pos.getZ() - PIXEL_SIZE, 0.0D, 0.0D, 0.0D);
				level.addParticle(DustParticleOptions.REDSTONE, pos.getX() + PIXEL_SIZE * 8, pos.getY() + PIXEL_SIZE * 2, pos.getZ() + PIXEL_SIZE * 17, 0.0D, 0.0D, 0.0D);
				level.addParticle(DustParticleOptions.REDSTONE, pos.getX() - PIXEL_SIZE, pos.getY() + PIXEL_SIZE * 2, pos.getZ() + PIXEL_SIZE * 8, 0.0D, 0.0D, 0.0D);
				level.addParticle(DustParticleOptions.REDSTONE, pos.getX() + PIXEL_SIZE * 17, pos.getY() + PIXEL_SIZE * 2, pos.getZ() + PIXEL_SIZE * 8, 0.0D, 0.0D, 0.0D);
				level.addParticle(DustParticleOptions.REDSTONE, pos.getX() + PIXEL_SIZE * 8, pos.getY() - PIXEL_SIZE, pos.getZ() + PIXEL_SIZE * 8, 0.0D, 0.0D, 0.0D);
			}
		}

		if(state.getValue(IS_ACTIVE))
		{
			if(ODConfigs.PORTAL_PARTICLES.get())
			{
				for(int i = 0; i < 3; ++i)
				{
					int XAxisRandom = random.nextInt(2) * 2 - 1;
					int ZAxisRandom = random.nextInt(2) * 2 - 1;
					double xPos = (double) pos.getX() + 0.5D + 0.25D * (double) XAxisRandom;
					double yPos = (double) ((float) pos.getY() + 0.5F + random.nextFloat());
					double zPos = (double) pos.getZ() + 0.5D + 0.25D * (double) ZAxisRandom;
					double xMotion = (double) (random.nextFloat() * (float) XAxisRandom);
					double yMotion = ((double) random.nextFloat() - 0.5D) * 0.125D;
					double zMotion = (double) (random.nextFloat() * (float) ZAxisRandom);
					level.addParticle(ParticleTypes.PORTAL, xPos, yPos, zPos, xMotion, yMotion, zMotion);
				}
			}
		}
	}
}