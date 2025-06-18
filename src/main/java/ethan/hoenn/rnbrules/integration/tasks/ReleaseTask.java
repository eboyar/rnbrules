/**
 * Ported over from PixelTweaks by Strangeone101
 * https://github.com/StrangeOne101/PixelTweaks
 */
package ethan.hoenn.rnbrules.integration.tasks;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.Tristate;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import ethan.hoenn.rnbrules.integration.ftbquests.PokemonTask;
import ethan.hoenn.rnbrules.integration.ftbquests.PokemonTaskTypes;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ReleaseTask extends PokemonTask {

	public ReleaseTask(Quest q) {
		super(q);
	}

	public Tristate hasOT = Tristate.DEFAULT;

	@Override
	public TaskType getType() {
		return PokemonTaskTypes.RELEASE_POKEMON;
	}

	@Override
	public void writeData(CompoundNBT nbt) {
		super.writeData(nbt);
		hasOT.write(nbt, "hasOT");
	}

	@Override
	public void readData(CompoundNBT nbt) {
		super.readData(nbt);
		this.hasOT = Tristate.read(nbt, "hasOT");
	}

	@Override
	public void writeNetData(PacketBuffer buffer) {
		super.writeNetData(buffer);
		hasOT.write(buffer);
	}

	@Override
	public void readNetData(PacketBuffer buffer) {
		super.readNetData(buffer);
		this.hasOT = Tristate.read(buffer);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.addTristate("hasOT", this.hasOT, v -> this.hasOT = v, Tristate.DEFAULT);
	}

	public void releasePokemon(TeamData team, ServerPlayerEntity player, Pokemon pokemon) {
		if (
			!team.isCompleted(this) &&
			(this.cachedSpec == null || this.cachedSpec.matches(pokemon) != this.invert) &&
			(this.hasOT.isDefault() || pokemon.getOriginalTrainerUUID().equals(player.getUUID()) == this.hasOT.isTrue())
		) {
			team.addProgress(this, 1L);
		}
	}
}
