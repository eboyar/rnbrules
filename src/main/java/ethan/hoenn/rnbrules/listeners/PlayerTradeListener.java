package ethan.hoenn.rnbrules.listeners;

import com.pixelmonmod.pixelmon.api.events.PixelmonTradeEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import ethan.hoenn.rnbrules.utils.managers.LevelCapManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerTradeListener {

	@SubscribeEvent
	public void onTryPlayerTrade(PixelmonTradeEvent.Pre event) {
		ServerPlayerEntity p1 = (ServerPlayerEntity) event.getPlayer1();
		ServerPlayerEntity p2 = (ServerPlayerEntity) event.getPlayer2();
		Pokemon pkm1 = event.getPokemon1();
		Pokemon pkm2 = event.getPokemon2();

		LevelCapManager lcm = LevelCapManager.get(p1.getLevel());
		
		int p1LevelCap = lcm.getLevelCap(p1.getUUID());
		int p2LevelCap = lcm.getLevelCap(p2.getUUID());

		int pkm1Level = pkm1.getPokemonLevel();
		int pkm2Level = pkm2.getPokemonLevel();
		
		if (pkm2Level > p1LevelCap) {
			event.setCanceled(true);
			String message = TextFormatting.RED + "Trade cancelled! The Pokemon you would receive (Lvl " + pkm2Level + ") exceeds your level cap of " + p1LevelCap + ".";
			p1.sendMessage(new StringTextComponent(message), p1.getUUID());
			p2.sendMessage(new StringTextComponent(TextFormatting.RED + "Trade cancelled! Your Pokemon (Lvl " + pkm2Level + ") exceeds " + p1.getDisplayName().getString() + "'s level cap of " + p1LevelCap + "."), p2.getUUID());
			return;
		}

		if (pkm1Level > p2LevelCap) {
			event.setCanceled(true);
			String message = TextFormatting.RED + "Trade cancelled! The Pokemon you would receive (Lvl " + pkm1Level + ") exceeds your level cap of " + p2LevelCap + ".";
			p2.sendMessage(new StringTextComponent(message), p2.getUUID());
			p1.sendMessage(new StringTextComponent(TextFormatting.RED + "Trade cancelled! Your Pokemon (Lvl " + pkm1Level + ") exceeds " + p2.getDisplayName().getString() + "'s level cap of " + p2LevelCap + "."), p1.getUUID());
			return;
		}

		
		String cl1 = pkm1.getPersistentData().getString("CatchLocation");
		String cl2 = pkm2.getPersistentData().getString("CatchLocation");

		
		String[] forbiddenLocations = {"fossil", "gamecorner", "starter", "hoennstarter", "roamer", "chosen"};

		
		for (String forbidden : forbiddenLocations) {
			if (cl1.equalsIgnoreCase(forbidden)) {
				event.setCanceled(true);
				String message = TextFormatting.RED + "Trade cancelled! Your Pokemon is not wild caught or is exceedingly rare and cannot be traded.";
				p1.sendMessage(new StringTextComponent(message), p1.getUUID());
				p2.sendMessage(new StringTextComponent(TextFormatting.RED + "Trade cancelled! " + p1.getDisplayName().getString() + "'s Pokemon is not wild caught or is exceedingly rare and cannot be traded."), p2.getUUID());
				return;
			}
		}

		
		for (String forbidden : forbiddenLocations) {
			if (cl2.equalsIgnoreCase(forbidden)) {
				event.setCanceled(true);
				String message = TextFormatting.RED + "Trade cancelled! Your Pokemon is not wild caught or is exceedingly rare and cannot be traded.";
				p2.sendMessage(new StringTextComponent(message), p2.getUUID());
				p1.sendMessage(new StringTextComponent(TextFormatting.RED + "Trade cancelled! " + p2.getDisplayName().getString() + "'s Pokemon is not wild caught or is exceedingly rare and cannot be traded."), p1.getUUID());
				return;
			}
		}

	}
}
