package ethan.hoenn.rnbrules.listeners;

import com.pixelmonmod.pixelmon.api.events.npc.TeachMoveEvent;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTutor;
import ethan.hoenn.rnbrules.utils.managers.BattleDependencyManager;
import java.util.Set;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MoveTutorListener {

	@SubscribeEvent
	public void onInteractMoveTutor(TeachMoveEvent.MoveLearnt.Pre event) {
		if (event.getNpc() instanceof NPCTutor && event.getPlayer() instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
			NPCTutor tutor = (NPCTutor) event.getNpc();

			BattleDependencyManager depManager = BattleDependencyManager.get((ServerWorld) player.level);
			if (depManager.tutorHasDependencies(tutor)) {
				Set<String> tutorDeps = depManager.getTutorDependencies(tutor);

				for (String depId : tutorDeps) {
					if (!depManager.playerHasDependency(player.getUUID(), depId)) {
						String depDescription = depManager.getDependency(depId) != null ? depManager.getDependency(depId).getDescription() : "Unknown requirement";

						player.sendMessage(new StringTextComponent("§9" + tutor.getName().getString() + "§7 won't teach you any moves until you §6" + depDescription + "§7."), player.getUUID());
						event.setCanceled(true);
						break;
					}
				}
			}
		}
	}
}
