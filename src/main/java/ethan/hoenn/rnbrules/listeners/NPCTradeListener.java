package ethan.hoenn.rnbrules.listeners;

import com.pixelmonmod.pixelmon.api.events.npc.NPCTraderEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrader;
import ethan.hoenn.rnbrules.utils.managers.BattleDependencyManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class NPCTradeListener {

	@SubscribeEvent
	public void onOpenNPCTradeScreen(NPCTraderEvent.ShowTrade.Pre event) {
		//check battle dependencies before allowing trade
		if (event.getPlayer() instanceof ServerPlayerEntity && event.getTrader() instanceof NPCTrader) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
			NPCTrader npc = (NPCTrader) event.getTrader();
			ServerWorld world = (ServerWorld) player.level;

			BattleDependencyManager depManager = BattleDependencyManager.get(world);
			if (depManager.npcTraderHasDependencies(npc)) {
				boolean missingDependency = false;

				for (String depId : depManager.getNPCTraderDependencies(npc)) {
					if (!depManager.playerHasDependency(player.getUUID(), depId)) {
						missingDependency = true;
						String depDescription = depManager.getDependency(depId) != null ? depManager.getDependency(depId).getDescription() : "Unknown requirement";

						player.sendMessage(new StringTextComponent("§9" + npc.getDisplayName().getString() + "§7 won't trade with you until you §6" + depDescription + "§7."), player.getUUID());
						event.setCanceled(true);
						break;
					}
				}

				if (missingDependency) {
					return;
				}
			}
		}
	}

	@SubscribeEvent
	public void onNPCTrade(NPCTraderEvent.AcceptTrade.Pre event) {
		event.setRemoveNPC(false);
		Pokemon toGive = event.getPlayerPokemon();
		Pokemon toGet = event.getTradedPokemon();

		//handles cases like growlithe -> hisuian growlithe
		if (toGive.getSpecies().is(toGet.getSpecies())) {
			toGet.getIVs().fillFromArray(event.getPlayerPokemon().getIVs().getArray());
			toGet.setLevel(toGive.getPokemonLevel());
			toGet.setMoveset(toGive.getMoveset());
		}
	}

	@SubscribeEvent
	public void onNPCTradePost(NPCTraderEvent.AcceptTrade.Post event) {
		if (event.getPlayer() instanceof ServerPlayerEntity && event.getTrader() instanceof NPCTrader) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
			NPCTrader npc = (NPCTrader) event.getTrader();
			CompoundNBT data = npc.getPersistentData();

			if (data.contains("TradeCommand")) {
				String command = data.getString("TradeCommand");
				if (!command.isEmpty()) {
					command = command.replace("@p", player.getScoreboardName());

					MinecraftServer server = player.getServer();
					if (server != null) {
						server.getCommands().performCommand(server.createCommandSourceStack(), command);
					}
				}
			}
		}
	}
}
