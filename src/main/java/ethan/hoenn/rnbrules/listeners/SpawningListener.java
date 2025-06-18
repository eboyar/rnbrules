package ethan.hoenn.rnbrules.listeners;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.spawning.SpawnEvent;
import com.pixelmonmod.pixelmon.entities.npcs.NPCFisherman;
import com.pixelmonmod.pixelmon.entities.npcs.NPCRelearner;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTutor;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import ethan.hoenn.rnbrules.utils.misc.Gamerules;
import net.minecraft.entity.Entity;
import net.minecraft.world.GameRules;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SpawningListener {

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onPokemonSpawn(SpawnEvent event) {
		if (!event.isCanceled() && !Pixelmon.isClient() && Gamerules.ENABLED) {
			Entity entity = event.action.getOrCreateEntity();
			GameRules rules = event.action.spawnLocation.location.world.getGameRules();
			if (!event.spawner.name.equals("fishing") && entity instanceof PixelmonEntity) {
				if (!rules.getBoolean(Gamerules.DO_POKEMON_SPAWNING)) {
					event.setCanceled(true);
				}
			} else if (entity instanceof NPCTrainer || entity instanceof NPCTutor || entity instanceof NPCRelearner || entity instanceof NPCFisherman) {
				if (!rules.getBoolean(Gamerules.DO_TRAINER_SPAWNING)) {
					event.setCanceled(true);
				}
			}
		}
	}
}
