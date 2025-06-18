/**
 * Ported over from PixelTweaks by Strangeone101
 * https://github.com/StrangeOne101/PixelTweaks
 */
package ethan.hoenn.rnbrules.integration.ftbquests;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import ethan.hoenn.rnbrules.integration.rewards.PokeDollarReward;
import ethan.hoenn.rnbrules.integration.rewards.PokelootReward;
import ethan.hoenn.rnbrules.integration.rewards.PokemonReward;
import net.minecraft.util.ResourceLocation;

public class PokemonRewardTypes {

	public static RewardType POKELOOT;
	public static RewardType POKEDOLLARS;
	public static RewardType POKEMON;

	public static void register() {
		POKELOOT = RewardTypes.register(new ResourceLocation("pixelmon", "pokeloot"), PokelootReward::new, () -> Icon.getIcon("rnbrules:textures/gui/pokeloot/1.png"));

		POKEDOLLARS = RewardTypes.register(new ResourceLocation("pixelmon", "pokedollars"), PokeDollarReward::new, () -> Icon.getIcon("pixelmon:textures/gui/pokedollar.png"));

		POKEMON = RewardTypes.register(new ResourceLocation("pixelmon", "pokemon"), PokemonReward::new, () -> Icon.getIcon("pixelmon:items/pokeballs/poke_ball"));
	}
}
