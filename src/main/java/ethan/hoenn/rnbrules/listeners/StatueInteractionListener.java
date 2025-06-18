package ethan.hoenn.rnbrules.listeners;

import com.pixelmonmod.pixelmon.entities.pixelmon.StatueEntity;
import ethan.hoenn.rnbrules.utils.managers.EncounterManager;
import ethan.hoenn.rnbrules.utils.managers.GlobalOTManager;
import ethan.hoenn.rnbrules.utils.managers.StatueVisibilityManager;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StatueInteractionListener {

	private static final Map<UUID, Long> lastInteractionTime = new ConcurrentHashMap<>();

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		if (event.getWorld().isClientSide() || event.getHand() != Hand.MAIN_HAND) return;

		Entity target = event.getTarget();

		if (target instanceof StatueEntity && event.getPlayer() instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
			StatueEntity statue = (StatueEntity) target;

			UUID playerUUID = player.getUUID();
			long currentTime = System.currentTimeMillis();

			if (lastInteractionTime.containsKey(playerUUID)) {
				long lastTime = lastInteractionTime.get(playerUUID);
				if ((currentTime - lastTime) < 1000) {
					return;
				}
			}

			lastInteractionTime.put(playerUUID, currentTime);

			CompoundNBT persistentData = statue.getPersistentData();
			if (!persistentData.contains("ForcedEncounter")) {
				return;
			}

			String encounterID = persistentData.getString("ForcedEncounter");

			GlobalOTManager globalot = GlobalOTManager.get(player.getLevel());
			EncounterManager em = EncounterManager.get(player.getLevel());

			if (globalot.playerHasGlobalOT(player.getUUID(), encounterID)) {
				return;
			}

			boolean success = EncounterManager.startForcedEncounterBattle(player, em.createForcedEncounter(encounterID));

			if (success) {
				StatueVisibilityManager svm = StatueVisibilityManager.get(player.getLevel());
				svm.hideStatueForPlayer(statue.getUUID(), player);
				globalot.addGlobalOT(encounterID);
				globalot.addPlayerGlobalOT(player.getUUID(), encounterID);
			}
		}
	}
}
