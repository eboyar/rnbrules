package ethan.hoenn.rnbrules.listeners;

import com.pixelmonmod.pixelmon.api.events.PokeBallImpactEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.entities.pokeballs.EmptyPokeBallEntity;
import ethan.hoenn.rnbrules.RNBConfig;
import ethan.hoenn.rnbrules.network.CancelTeamSelectionPacket;
import ethan.hoenn.rnbrules.network.PacketHandler;
import ethan.hoenn.rnbrules.utils.managers.GauntletManager;
import ethan.hoenn.rnbrules.utils.managers.LevelCapManager;
import ethan.hoenn.rnbrules.utils.managers.ProgressionManager;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkDirection;

public class PokeBallImpactListener {

	@SubscribeEvent
	public void onInitiateTrainerBattle(PokeBallImpactEvent event) {
		ServerPlayerEntity player = (ServerPlayerEntity) event.getPokeBall().getOwner();

		if (event.getPokeBall() instanceof EmptyPokeBallEntity) {
			return;
		}

		Optional<Entity> hit = event.getEntityHit();
		if (hit.isPresent() && (hit.get() instanceof PlayerEntity || hit.get() instanceof ServerPlayerEntity)) {
			return;
		}

		if (hit.isPresent() && hit.get() instanceof NPCTrainer) {
			handleTrainerBattle(event, player, (NPCTrainer) hit.get());
		}

		if (hit.isPresent() && hit.get() instanceof PixelmonEntity) {
			handleWildPokemonBattle(event, player, (PixelmonEntity) hit.get());
		}
	}

	private boolean verifySpeciesClause(ServerPlayerEntity spe) {
		PlayerPartyStorage pps = StorageProxy.getParty(spe);
		Set<String> seenSpecies = new HashSet<>();

		for (Pokemon pkm : pps.getAll()) {
			if (pkm == null || pkm.isEgg()) continue;

			String speciesName = pkm.getSpecies().getName();

			if (seenSpecies.contains(speciesName)) {
				return false;
			}
			seenSpecies.add(speciesName);
		}
		return true;
	}

	private String getTrainerNameFromUUID(ServerWorld world, String uuidString) {
		try {
			UUID uuid = UUID.fromString(uuidString);
			Entity entity = world.getEntity(uuid);

			if (entity instanceof NPCTrainer) {
				NPCTrainer trainer = (NPCTrainer) entity;
				return trainer.getName().getString();
			}
			return "Unknown Trainer";
		} catch (IllegalArgumentException e) {
			return "Unknown Trainer";
		}
	}

	private void handleTrainerBattle(PokeBallImpactEvent event, ServerPlayerEntity player, NPCTrainer target) {
		if (!verifySpeciesClause(player)) {
			sendCancelMessage(event, player, "§7Your party §4cannot§7 contain more than one Pokemon of the §lsame§r§7 species.");
			return;
		}

		if (target.getPersistentData().hasUUID("LeagueTrainer")) {
			event.setCanceled(true);
			return;
		}

		if (target.getPersistentData().contains("IsPlayerPartner") && target.getPersistentData().getBoolean("IsPlayerPartner")) {
			sendCancelMessage(event, player, "§9" + target.getName("en_us") + "§7 is your §2ally§7 and cannot be challenged to a battle.");
			return;
		}

		if (target.getPersistentData().contains("CopyTrainer") && target.getPersistentData().getBoolean("CopyTrainer")) {
			event.setCanceled(true);
			return;
		}

		if (checkTrainerDefeats(event, player, target)) {
			return;
		}

		handleGauntletValidation(event, player, target);
	}

	private boolean checkTrainerDefeats(PokeBallImpactEvent event, ServerPlayerEntity player, NPCTrainer target) {
		ProgressionManager pm = ProgressionManager.get();

		if (pm.hasDefeatedTrainer(player.getUUID(), target.getUUID())) {
			sendCancelMessage(event, player, "§7You have already defeated this trainer.");
			return true;
		}

		if (pm.isInGauntlet(player.getUUID()) && pm.hasGauntletTrainerBeenDefeated(player.getUUID(), target.getUUID())) {
			sendCancelMessage(event, player, "§7You have already defeated this trainer.");
			return true;
		}

		return false;
	}

	private void handleGauntletValidation(PokeBallImpactEvent event, ServerPlayerEntity player, NPCTrainer target) {
		GauntletManager gm = GauntletManager.get(player.getLevel());
		String gauntletId = gm.findGauntletForTrainerWithPartners(target.getUUID().toString(), player.getLevel());

		if (gauntletId == null) {
			return;
		}

		int trainerPosition = gm.getEffectiveTrainerPosition(target.getUUID().toString(), gauntletId, player.getLevel());
		if (trainerPosition == -1) {
			event.setCanceled(true);
			return;
		}

		Map<String, List<String>> gauntlets = RNBConfig.getGauntlets();

		if (!gm.isPartOfAnyGauntlet(player.getUUID())) {
			handleGauntletStart(event, player, gauntletId, trainerPosition, gauntlets, gm);
		} else {
			handleActiveGauntletValidation(event, player, gauntletId, trainerPosition, gauntlets, gm);
		}
	}

	private void handleGauntletStart(PokeBallImpactEvent event, ServerPlayerEntity player, String gauntletId, int trainerPosition, Map<String, List<String>> gauntlets, GauntletManager gm) {
		if (trainerPosition == 0) {
			String parentGauntletId = RNBConfig.findParentGauntlet(gauntletId);

			if (parentGauntletId != null) {
				String firstTrainerUUID = gauntlets.get(parentGauntletId).get(0);
				String firstTrainerName = getTrainerNameFromUUID(player.getLevel(), firstTrainerUUID);
				sendCancelMessage(event, player, "§7This §dGauntlet§7 is part of a sequence. You must first complete defeat §9" + firstTrainerName + "§7.");
				return;
			}
		}

		if (trainerPosition != 0) {
			String firstTrainerUUID = gauntlets.get(gauntletId).get(0);
			String firstTrainerName = getTrainerNameFromUUID(player.getLevel(), firstTrainerUUID);
			sendCancelMessage(event, player, "§7To begin this §dGauntlet§7, defeat, §9" + firstTrainerName + "§7 first.");
			return;
		}

		ProgressionManager.get().startGauntlet(player.getUUID(), gauntletId);
	}

	private void handleActiveGauntletValidation(PokeBallImpactEvent event, ServerPlayerEntity player, String gauntletId, int trainerPosition, Map<String, List<String>> gauntlets, GauntletManager gm) {
		String playerGauntletId = gm.getCurrentGauntletName(player.getUUID());

		if (!gauntletId.equals(playerGauntletId)) {
			sendCancelMessage(event, player, "§7You must complete or fail your current §dGauntlet§7 first.");
			return;
		}

		int nextAllowedPosition = gm.getNextTrainerPosition(player.getUUID(), gauntletId);
		if (trainerPosition != nextAllowedPosition) {
			String nextTrainerUUID = gauntlets.get(gauntletId).get(nextAllowedPosition);
			String nextTrainerName = getTrainerNameFromUUID(player.getLevel(), nextTrainerUUID);
			sendCancelMessage(event, player, "§7You must follow the sequence of trainers in this §dGauntlet§7.\n" + "§7Your next battle is against §9" + nextTrainerName + "§7.");
		}
	}

	private void handleWildPokemonBattle(PokeBallImpactEvent event, ServerPlayerEntity player, PixelmonEntity entity) {
		Pokemon pokemon = entity.getPokemon();
		LevelCapManager lcm = LevelCapManager.get(player.getLevel());

		if (pokemon.getPokemonLevel() > lcm.getLevelCap(player.getUUID())) {
			event.setCanceled(true);
			return;
		}

		if (pokemon.getPersistentData().getString("CatchLocation").equalsIgnoreCase("safarizone")) {
			player.sendMessage(new StringTextComponent("§7You §4cannot§7 battle Pokémon above your level cap. §6(" + lcm.getLevelCap(player.getUUID()) + ")"), player.getUUID());
			event.setCanceled(true);
		}
	}

	private void sendCancelMessage(PokeBallImpactEvent event, ServerPlayerEntity player, String message) {
		PacketHandler.INSTANCE.sendTo(new CancelTeamSelectionPacket(message), player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
		event.getPokeBall().remove();
		event.setCanceled(true);
	}
}
