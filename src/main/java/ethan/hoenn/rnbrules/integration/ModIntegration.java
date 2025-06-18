package ethan.hoenn.rnbrules.integration;
import ethan.hoenn.rnbrules.integration.ftbquests.PokemonRewardTypes;
import ethan.hoenn.rnbrules.integration.ftbquests.PokemonTaskTypes;
import ethan.hoenn.rnbrules.integration.ftbquests.TaskListener;
import net.minecraftforge.fml.ModList;

public class ModIntegration {

	public static boolean ftbQuests() {
		return ModList.get().isLoaded("ftbquests");
	}

	public static void registerFTBQuestsIntegration() {
		if (!ftbQuests()) return;

		PokemonTaskTypes.register();
		PokemonRewardTypes.register();
		new TaskListener();
	}
}
