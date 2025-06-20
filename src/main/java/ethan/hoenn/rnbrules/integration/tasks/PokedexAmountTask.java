/**
 * Ported over from PixelTweaks by Strangeone101
 * https://github.com/StrangeOne101/PixelTweaks
 */
package ethan.hoenn.rnbrules.integration.tasks;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import ethan.hoenn.rnbrules.integration.ftbquests.PokemonTaskTypes;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PokedexAmountTask extends PokedexTask {

	public int count = 1;

	public PokedexAmountTask(Quest q) {
		super(q);
	}

	@Override
	public TaskType getType() {
		return PokemonTaskTypes.POKEDEX_AMOUNT;
	}

	@Override
	public long getMaxProgress() {
		return count;
	}

	@Override
	public void writeData(CompoundNBT nbt) {
		super.writeData(nbt);
		nbt.putInt("count", this.count);
	}

	@Override
	public void readData(CompoundNBT nbt) {
		super.readData(nbt);
		this.count = nbt.getInt("count");
		if (this.filter == PokedexFilter.SINGLE_MON) {
			this.count = 1;
		}
	}

	@Override
	public void writeNetData(PacketBuffer buffer) {
		super.writeNetData(buffer);
		buffer.writeVarInt(this.count);
	}

	@Override
	public void readNetData(PacketBuffer buffer) {
		super.readNetData(buffer);
		this.count = buffer.readVarInt();
		if (this.filter == PokedexFilter.SINGLE_MON) {
			this.count = 1;
		}
	}

	@Override
	public void calculateAmount() {
		super.calculateAmount();
		this.count = Math.min(this.count, this.maxPokedexSize);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);

		config.addInt("count", this.count, v -> this.count = v, 10, 1, this.maxPokedexSize);
	}
}
