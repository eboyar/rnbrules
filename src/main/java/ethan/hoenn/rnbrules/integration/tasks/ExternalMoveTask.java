/**
 * Ported over from PixelTweaks by Strangeone101
 * https://github.com/StrangeOne101/PixelTweaks
 */
package ethan.hoenn.rnbrules.integration.tasks;

import com.pixelmonmod.pixelmon.api.moveskills.MoveSkill;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import ethan.hoenn.rnbrules.integration.ftbquests.PokemonTask;
import ethan.hoenn.rnbrules.integration.ftbquests.PokemonTaskTypes;
import java.util.stream.Collectors;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ExternalMoveTask extends PokemonTask {

	public String move = "forage";

	public ExternalMoveTask(Quest q) {
		super(q);
	}

	@Override
	public void writeData(CompoundNBT nbt) {
		super.writeData(nbt);
		nbt.putString("move", move);
	}

	@Override
	public void readData(CompoundNBT nbt) {
		super.readData(nbt);
		move = nbt.getString("move");
	}

	@Override
	public void writeNetData(PacketBuffer buffer) {
		super.writeNetData(buffer);
		buffer.writeUtf(move);
	}

	@Override
	public void readNetData(PacketBuffer buffer) {
		super.readNetData(buffer);
		move = buffer.readUtf();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);

		config.addEnum(
			"move",
			move,
			v -> move = v,
			NameMap.of("forage", MoveSkill.moveSkills.stream().map(m -> m.id).collect(Collectors.toList()))
				.nameKey(m -> "pixelmon.moveskill." + m)
				.icon(m -> Icon.getIcon(MoveSkill.getMoveSkillByID(m).sprite))
				.create()
		);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Icon getAltIcon() {
		return Icon.getIcon(MoveSkill.getMoveSkillByID(move).sprite);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ITextComponent getAltTitle() {
		TranslationTextComponent title = new TranslationTextComponent("ftbquests.task.pixelmon.external_move.title", new TranslationTextComponent(MoveSkill.getMoveSkillByID(move).name));
		if (count > 1) {
			title.append(" ");
			title.append(count + "x");
		}
		if (this.cachedSpec != null) {
			title.append(" ");
			title.append(getPokemon());
		}
		return title;
	}

	@Override
	public TaskType getType() {
		return PokemonTaskTypes.EXTERNAL_MOVE;
	}

	public void onMove(TeamData teamData, String move, Pokemon pokemon) {
		if (teamData.isCompleted(this)) return;

		if (teamData.file.isServerSide() && (this.cachedSpec == null || this.cachedSpec.matches(pokemon) != this.invert) && this.move.equals(move)) {
			teamData.addProgress(this, 1);
		}
	}
}
