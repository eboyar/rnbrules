package ethan.hoenn.rnbrules.listeners;

import com.pixelmonmod.pixelmon.api.events.PokeBallImpactEvent;
import com.pixelmonmod.pixelmon.api.events.battles.BattleStartedEvent;
import com.pixelmonmod.pixelmon.api.events.npc.NPCEvent;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import com.pixelmonmod.pixelmon.entities.npcs.NPCChatting;
import com.pixelmonmod.pixelmon.entities.npcs.NPCEntity;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import ethan.hoenn.rnbrules.network.CancelTeamSelectionPacket;
import ethan.hoenn.rnbrules.network.PacketHandler;
import ethan.hoenn.rnbrules.utils.managers.BattleDependencyManager;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BattleDependencyListener {

	//order of checks -> Dependencies, Gauntlets, Dialogue, Clauses, Multi Battle
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onInitiateTrainerBattle(PokeBallImpactEvent event) {
		ServerPlayerEntity player = (ServerPlayerEntity) event.getPokeBall().getOwner();
		if (event.getEntityHit().isPresent() && event.getEntityHit().get() instanceof NPCTrainer && player != null) {
			NPCTrainer target = (NPCTrainer) event.getEntityHit().get();
			BattleDependencyManager bdm = BattleDependencyManager.get(player.getLevel());
			if (!bdm.trainerHasDependencies(target)) {
				return;
			}

			Set<String> trainerDeps = bdm.getTrainerDependencies(target);
			Set<String> playerDeps = bdm.getPlayerDependencies(player.getUUID());

			for (String dependency : trainerDeps) {
				if (!playerDeps.contains(dependency)) {
					String trainerName = target.getName().getString();
					String missingDescription = bdm.getDependency(dependency).getDescription();
					event.setCanceled(true);

					PacketHandler.INSTANCE.sendTo(
						new CancelTeamSelectionPacket("§7To battle §9" + trainerName + "§7, you must §6" + missingDescription + "§7 first."),
						player.connection.getConnection(),
						net.minecraftforge.fml.network.NetworkDirection.PLAY_TO_CLIENT
					);
					return;
				}
			}
		}
	}

	/*
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onBattleStarted(BattleStartedEvent.Pre event) {
		List<BattleParticipant> bps = event.getBattleController().participants;

		PlayerParticipant pp = null;
		TrainerParticipant tp = null;

		for (BattleParticipant bp : bps) {
			if (bp instanceof TrainerParticipant) {
				tp = (TrainerParticipant) bp;
			} else if (bp instanceof PlayerParticipant) {
				pp = (PlayerParticipant) bp;
			}
		}

		if (tp == null || pp == null) {
			return;
		}

		BattleDependencyManager bdm = BattleDependencyManager.get(pp.player.getLevel());
		if (!bdm.trainerHasDependencies(tp.trainer)) {
			return;
		}

		Set<String> trainerDeps = bdm.getTrainerDependencies(tp.trainer);
		Set<String> playerDeps = bdm.getPlayerDependencies(pp.player.getUUID());

		for (String dependency : trainerDeps) {
			if (!playerDeps.contains(dependency)) {
				String trainerName = tp.trainer.getName().getString();
				String missingDescription = bdm.getDependency(dependency).getDescription();
				event.setCanceled(true);

				PacketHandler.INSTANCE.sendTo(
					new CancelTeamSelectionPacket("§7To battle §9" + trainerName + "§7, you must §6" + missingDescription + "§7 first."),
					pp.player.connection.getConnection(),
					net.minecraftforge.fml.network.NetworkDirection.PLAY_TO_CLIENT
				);
				return;
			}
		}
	}
	*/

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onInteractWithNPC(NPCEvent.Interact event) {
		if (!(event.npc instanceof NPCChatting) || (event.player.getMainHandItem().getItem().equals(PixelmonItems.trainer_editor))) {
			return;
		}

		ServerPlayerEntity player = (ServerPlayerEntity) event.player;
		NPCChatting npc = (NPCChatting) event.npc;

		BattleDependencyManager depManager = BattleDependencyManager.get((ServerWorld) player.level);
		if (depManager.npcHasDependencies(npc)) {
			CompoundNBT data = event.npc.getPersistentData();
			Set<String> npcDeps = depManager.getNPCDependencies(npc);
			boolean missingDependency = false;

			for (String depId : npcDeps) {
				if (!depManager.playerHasDependency(player.getUUID(), depId)) {
					missingDependency = true;
					String depDescription = depManager.getDependency(depId) != null ? depManager.getDependency(depId).getDescription() : "Unknown requirement";

					if (data.contains("OneTimeReward")) {
						player.sendMessage(new StringTextComponent("§9" + npc.getName("en_us") + "§7 wants to give you something, but you must §6" + depDescription + "§7 first."), player.getUUID());
					} else {
						player.sendMessage(new StringTextComponent("§9" + npc.getName("en_us") + "§7 wants to talk to you, but you must §6" + depDescription + "§7 first."), player.getUUID());
					}

					event.setCanceled(true);
					break;
				}
			}

			if (missingDependency) {
				event.setCanceled(true);
			}
		}
	}
}
