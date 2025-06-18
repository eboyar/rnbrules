package ethan.hoenn.rnbrules.blocks;

import ethan.hoenn.rnbrules.registries.TileEntityRegistry;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;

public class LocationBlockTileEntity extends TileEntity {

	private String locationName = "Unnamed Location";

	public LocationBlockTileEntity() {
		super(TileEntityRegistry.LOCATION_BLOCK.get());
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String name) {
		this.locationName = name;
		this.setChanged();

		if (this.level != null && !this.level.isClientSide) {
			BlockState state = this.level.getBlockState(this.worldPosition);
			this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
		}
	}

	@Override
	public void load(BlockState state, CompoundNBT nbt) {
		super.load(state, nbt);
		this.locationName = nbt.getString("LocationName");
	}

	@Override
	public CompoundNBT save(CompoundNBT compound) {
		super.save(compound);
		compound.putString("LocationName", this.locationName);
		return compound;
	}

	@Override
	public CompoundNBT getUpdateTag() {
		CompoundNBT tag = super.getUpdateTag();
		tag.putString("LocationName", this.locationName);
		return tag;
	}

	@Override
	public void handleUpdateTag(BlockState state, CompoundNBT tag) {
		super.handleUpdateTag(state, tag);
		this.locationName = tag.getString("LocationName");
	}

	@Nullable
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(this.worldPosition, 1, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		this.handleUpdateTag(this.getBlockState(), pkt.getTag());
	}
}
