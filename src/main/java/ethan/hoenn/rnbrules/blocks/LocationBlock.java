package ethan.hoenn.rnbrules.blocks;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class LocationBlock extends Block {

	public LocationBlock() {
		super(Properties.of(Material.STONE).strength(1.5f, 6.0f).sound(SoundType.STONE).noOcclusion());
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new LocationBlockTileEntity();
	}

	@Override
	public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(worldIn, pos, state, placer, stack);

		TileEntity tileEntity = worldIn.getBlockEntity(pos);
		if (tileEntity instanceof LocationBlockTileEntity && placer instanceof PlayerEntity) {
			LocationBlockTileEntity locationTile = (LocationBlockTileEntity) tileEntity;
			if (stack.hasCustomHoverName()) {
				locationTile.setLocationName(stack.getHoverName().getString());
			} else {
				locationTile.setLocationName("Unnamed Location");
			}
		}
	}

	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if (!worldIn.isClientSide && handIn == Hand.MAIN_HAND) {
			TileEntity tileEntity = worldIn.getBlockEntity(pos);
			if (tileEntity instanceof LocationBlockTileEntity) {
				LocationBlockTileEntity locationTile = (LocationBlockTileEntity) tileEntity;
				String currentName = locationTile.getLocationName();
				player.sendMessage(new StringTextComponent(TextFormatting.YELLOW + "Location: " + TextFormatting.GREEN + currentName), player.getUUID());

				if (player.hasPermissions(2)) {
					player.sendMessage(new StringTextComponent(TextFormatting.GRAY + "Use " + TextFormatting.WHITE + "/setlocation <name>" + TextFormatting.GRAY + " or rename in anvil"), player.getUUID());
				}
			}
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		ItemStack stack = super.getPickBlock(state, target, world, pos, player);
		TileEntity te = world.getBlockEntity(pos);

		if (te instanceof LocationBlockTileEntity) {
			LocationBlockTileEntity locationTile = (LocationBlockTileEntity) te;
			String locationName = locationTile.getLocationName();

			if (locationName != null && !locationName.equals("Unnamed Location")) {
				stack.setHoverName(new StringTextComponent(locationName));
			}
		}

		return stack;
	}

	@Override
	public void playerWillDestroy(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
		TileEntity tileEntity = worldIn.getBlockEntity(pos);
		if (tileEntity instanceof LocationBlockTileEntity) {
			LocationBlockTileEntity locationTile = (LocationBlockTileEntity) tileEntity;
			String locationName = locationTile.getLocationName();

			if (!worldIn.isClientSide && !player.isCreative() && locationName != null && !locationName.equals("Unnamed Location")) {
				ItemStack stack = new ItemStack(this);
				stack.setHoverName(new StringTextComponent(locationName));

				Block.popResource(worldIn, pos, stack);

				player.level.removeBlock(pos, false);
				return;
			}
		}
		super.playerWillDestroy(worldIn, pos, state, player);
	}
}
