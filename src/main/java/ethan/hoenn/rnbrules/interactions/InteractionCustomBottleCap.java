package ethan.hoenn.rnbrules.interactions;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.config.PixelmonConfigProxy;
import com.pixelmonmod.pixelmon.api.events.pokemon.BottleCapEvent;
import com.pixelmonmod.pixelmon.api.interactions.IInteraction;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.api.pokemon.stats.IVStore;
import com.pixelmonmod.pixelmon.comm.ChatHandler;
import com.pixelmonmod.pixelmon.comm.EnumUpdateType;
import com.pixelmonmod.pixelmon.comm.packetHandlers.OpenScreenPacket;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.enums.EnumGuiScreen;
import com.pixelmonmod.pixelmon.enums.items.EnumBottleCap;
import com.pixelmonmod.pixelmon.items.BottlecapItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class InteractionCustomBottleCap implements IInteraction {
	public InteractionCustomBottleCap() {

	}

	public boolean processInteract(PixelmonEntity pixelmon, PlayerEntity player, Hand hand, ItemStack itemstack) {
		if (!player.level.isClientSide && hand != Hand.OFF_HAND && itemstack.getItem() instanceof BottlecapItem) {
			Pokemon data = pixelmon.getPokemon();
			if (data.getOwnerPlayer() != player) {
				return false;
			} else {
				IVStore ivs = data.getIVs();
				boolean isMax = ivs.getStat(BattleStatsType.HP) + ivs.getStat(BattleStatsType.ATTACK) + ivs.getStat(BattleStatsType.DEFENSE) + ivs.getStat(BattleStatsType.SPECIAL_ATTACK) + ivs.getStat(BattleStatsType.SPECIAL_DEFENSE) + ivs.getStat(BattleStatsType.SPEED) == 186;
				boolean isHt = ivs.isHyperTrained(BattleStatsType.HP) && ivs.isHyperTrained(BattleStatsType.ATTACK) && ivs.isHyperTrained(BattleStatsType.DEFENSE) && ivs.isHyperTrained(BattleStatsType.SPECIAL_ATTACK) && ivs.isHyperTrained(BattleStatsType.SPECIAL_DEFENSE) && ivs.isHyperTrained(BattleStatsType.SPEED);
				if (!isMax && !isHt) {
					BottlecapItem bottleCap = (BottlecapItem) itemstack.getItem();
					if (Pixelmon.EVENT_BUS.post(new BottleCapEvent(pixelmon, player, bottleCap.type, itemstack))) {
						return false;
					} else {
						if (bottleCap.type == EnumBottleCap.GOLD) {
							ivs.setHyperTrained((BattleStatsType) null, true);
							data.getStats().setLevelStats(data.getNature(), data.getForm(), data.getPokemonLevel());
							data.markDirty(new EnumUpdateType[]{EnumUpdateType.HP, EnumUpdateType.Stats});
							ChatHandler.sendChat(player, "pixelmon.interaction.bottlecap.goldcap", new Object[]{pixelmon.getNickname()});
							itemstack.shrink(1);
						} else {
							BattleStatsType[] types = BattleStatsType.getEVIVStatValues();
							int[] screenData = new int[types.length + 1];

							for (int i = 0; i < types.length; ++i) {
								screenData[i] = !ivs.isHyperTrained(types[i]) && ivs.getStat(types[i]) != 31 ? getHTValue(types[i], data) : 0;
							}

							screenData[6] = pixelmon.getId();
							OpenScreenPacket.open(player, EnumGuiScreen.BottleCap, screenData);
						}

						return true;
					}
				} else {
					ChatHandler.sendChat(player, "pixelmon.interaction.bottlecap.full", new Object[]{pixelmon.getNickname()});
					return true;
				}
			}
		} else {
			return false;
		}
	}

	private static int getHTValue(BattleStatsType type, Pokemon pokemon) {
		IVStore store = pokemon.getIVs();
		boolean isHT = store.isHyperTrained(type);
		store.setHyperTrained(type, true);
		int stat = pokemon.getStats().calculateStat(type, pokemon.getNature(), pokemon.getForm(), pokemon.getPokemonLevel());
		store.setHyperTrained(type, isHT);
		return stat;
	}
}
