package ethan.hoenn.rnbrules.mixins.items;

import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStats;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.enums.heldItems.EnumHeldItems;
import com.pixelmonmod.pixelmon.items.HeldItem;
import com.pixelmonmod.pixelmon.items.heldItems.SoulDewItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SoulDewItem.class)
public abstract class CustomSoulDew extends HeldItem {

	public CustomSoulDew(EnumHeldItems heldItemType, Properties properties) {
		super(heldItemType, properties);
	}

	/**
	 * @author ethan
	 * @reason Remove original functionality
	 */
	@Overwrite(remap = false)
	public double preProcessDamagingAttackUser(PixelmonWrapper attacker, PixelmonWrapper target, Attack attack, double damage) {
		return damage;
	}

	@Unique
	@Override
	public void applySwitchInEffect(PixelmonWrapper newPokemon) {
		if (newPokemon.getSpecies().is(PixelmonSpecies.LATIOS) || newPokemon.getSpecies().is(PixelmonSpecies.LATIAS)) {
			BattleStats stats = newPokemon.getBattleStats();

			newPokemon.bc.sendToAll(newPokemon.getPokemonName() + " has activated its Soul Dew!");
			stats.modifyStat(1, BattleStatsType.SPECIAL_ATTACK);
			stats.modifyStat(1, BattleStatsType.SPECIAL_DEFENSE);
		}
	}
}
