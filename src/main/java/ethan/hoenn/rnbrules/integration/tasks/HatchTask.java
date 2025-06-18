/**
 * Ported over from PixelTweaks by Strangeone101
 * https://github.com/StrangeOne101/PixelTweaks
 */
package ethan.hoenn.rnbrules.integration.tasks;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import ethan.hoenn.rnbrules.integration.ftbquests.PokemonTask;
import ethan.hoenn.rnbrules.integration.ftbquests.PokemonTaskTypes;

public class HatchTask extends PokemonTask {

    public HatchTask(Quest q) {
        super(q);
    }

    @Override
    public TaskType getType() {
        return PokemonTaskTypes.HATCH_EGG;
    }

    public void onHatch(TeamData team, Pokemon pokemon) {
        if (!team.isCompleted(this) && team.file.isServerSide() && (this.cachedSpec == null || this.cachedSpec.matches(pokemon) != this.invert)) {
            team.addProgress(this, 1L);
        }
    }
}
