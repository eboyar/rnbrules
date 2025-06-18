package ethan.hoenn.rnbrules.items;

import ethan.hoenn.rnbrules.blocks.LocationBlockTileEntity;
import ethan.hoenn.rnbrules.registries.BlockRegistry;
import ethan.hoenn.rnbrules.registries.ItemRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class LocationWand extends Item {

	private static final double MAX_DISTANCE = 64.0D;
	private static final int MAX_DEPTH_CHECK = 3;

	public LocationWand(Properties properties) {
		super(properties);
	}

	private BlockPos findSolidBlockPos(World worldIn, BlockPos hitPos) {
		BlockState hitBlockState = worldIn.getBlockState(hitPos);

		if (hitBlockState.getMaterial().isSolid() && hitBlockState.isCollisionShapeFullBlock(worldIn, hitPos)) {
			return hitPos;
		}

		BlockPos currentPos = hitPos;
		boolean isWater = hitBlockState.getMaterial().isLiquid();

		int maxChecks = isWater ? 10 : MAX_DEPTH_CHECK;

		for (int i = 0; i < maxChecks; i++) {
			currentPos = currentPos.below();
			BlockState state = worldIn.getBlockState(currentPos);

			if (isWater && state.getMaterial().isLiquid()) {
				continue;
			}

			if (state.getMaterial().isSolid() && state.isCollisionShapeFullBlock(worldIn, currentPos)) {
				return currentPos;
			}
		}

		return null;
	}

	@Override
	public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack mainHandStack = playerIn.getItemInHand(handIn);
		ItemStack offHandStack = playerIn.getOffhandItem();

		if (handIn == Hand.MAIN_HAND && offHandStack.getItem() == ItemRegistry.LOCATION_BLOCK.get()) {
			if (!worldIn.isClientSide) {
				Vector3d eyePosition = playerIn.getEyePosition(1.0F);
				Vector3d lookVector = playerIn.getLookAngle();
				Vector3d endPosition = eyePosition.add(lookVector.x * MAX_DISTANCE, lookVector.y * MAX_DISTANCE, lookVector.z * MAX_DISTANCE);

				RayTraceContext context = new RayTraceContext(eyePosition, endPosition, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, playerIn);
				BlockRayTraceResult rayTraceResult = worldIn.clip(context);
				if (rayTraceResult.getType() == RayTraceResult.Type.BLOCK) {
					BlockPos hitPos = rayTraceResult.getBlockPos();

					BlockPos solidBlockPos = findSolidBlockPos(worldIn, hitPos);
					if (solidBlockPos == null) {
						playerIn.displayClientMessage(new StringTextComponent(TextFormatting.YELLOW + "Could not find a suitable solid block."), true);
						return ActionResult.fail(mainHandStack);
					}

					BlockPos placePos = solidBlockPos.below();
					BlockState locationBlockState = BlockRegistry.LOCATION_BLOCK.get().defaultBlockState();

					if (locationBlockState.canSurvive(worldIn, placePos)) {
						boolean placed = worldIn.setBlock(placePos, locationBlockState, 11);

						if (placed) {
							TileEntity tileEntity = worldIn.getBlockEntity(placePos);
							if (tileEntity instanceof LocationBlockTileEntity) {
								LocationBlockTileEntity locationTile = (LocationBlockTileEntity) tileEntity;
								if (offHandStack.hasCustomHoverName()) {
									locationTile.setLocationName(offHandStack.getHoverName().getString());
								} else {
									locationTile.setLocationName("Unnamed Location");
								}
							}

							if (!playerIn.isCreative()) {
								offHandStack.shrink(1);
							}
							worldIn.playSound(null, placePos, SoundEvents.STONE_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
							playerIn.swing(handIn, true);

							return ActionResult.success(mainHandStack);
						} else {
							playerIn.displayClientMessage(new StringTextComponent(TextFormatting.RED + "Failed to place block."), true);
							return ActionResult.fail(mainHandStack);
						}
					} else {
						playerIn.displayClientMessage(new StringTextComponent(TextFormatting.YELLOW + "Cannot place block at target location."), true);
						return ActionResult.fail(mainHandStack);
					}
				} else {
					return ActionResult.pass(mainHandStack);
				}
			}
			return ActionResult.sidedSuccess(mainHandStack, worldIn.isClientSide());
		}
		return ActionResult.pass(mainHandStack);
	}
}
