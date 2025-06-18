/**
 * Ported over from PixelTweaks by Strangeone101
 * https://github.com/StrangeOne101/PixelTweaks
 */

package ethan.hoenn.rnbrules.music.condition;

import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.ParticipantType;
import com.pixelmonmod.pixelmon.client.ClientProxy;
import com.pixelmonmod.pixelmon.client.gui.battles.ClientBattleManager;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import ethan.hoenn.rnbrules.utils.managers.ClientLocationManager;
import ethan.hoenn.rnbrules.utils.managers.LocationManager;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class TrainerCondition extends Condition<NPCTrainer> {

	public String trainerType;
	public Integer textureIndex;
	public String customSteveTexture;
	public String name;
	public Boolean gymLeader;
	public int minPartyLevel = 0;
	public int maxPartyLevel = 100;
	public String location;

	@Override
	public boolean conditionMet(NPCTrainer trainer) {
		if (trainer == null) return false;

		if (customSteveTexture != null && !customSteveTexture.isEmpty() && !trainer.getCustomSteveTexture().equals(customSteveTexture)) return false;
		if (name != null && !name.isEmpty() && trainer.getName("en_us") != null && (!trainer.getName("en_us").equals(name) && !trainer.getName("en_us").contains(name))) return false;
		if (gymLeader != null && isGymLeaderAlt(trainer) != gymLeader) return false;
		if (trainerType != null && !trainerType.isEmpty() && !trainer.getBaseTrainer().name.equalsIgnoreCase(trainerType)) return false;
		if (textureIndex != null && trainer.getTextureIndex() != textureIndex) return false;
		if (trainer.getTrainerLevel() < minPartyLevel) return false;
		if (trainer.getTrainerLevel() > maxPartyLevel) return false;

		if (location != null && !location.isEmpty()) {
			ClientLocationManager locationManager = ClientLocationManager.getInstance();
			String currentLocation = locationManager.getCurrentPlayerLocation();

			if (currentLocation == null) return false;

			String normalizedPlayerLocation = LocationManager.normalizeLocationName(currentLocation);
			String normalizedConditionLocation = LocationManager.normalizeLocationName(location);

			return normalizedPlayerLocation.equals(normalizedConditionLocation);
		}

		return true;
	}

	public boolean isGymLeaderAlt(NPCTrainer trainer) {
		ItemStack[] winnings = trainer.getWinnings();

		return false;
	}

	@Override
	public NPCTrainer itemFromPixelmon(PixelmonEntity entity) {
		BattleController bt = entity.battleController;
		if (bt != null) {
			for (BattleParticipant p : bt.participants) {
				Entity participantEntity = p.getEntity();
				if (participantEntity instanceof NPCTrainer) {
					return (NPCTrainer) participantEntity;
				}
			}
		} else {
			ClientBattleManager bm = ClientProxy.battleManager;
			for (ParticipantType[] type : bm.battleSetup) {
				if (type[0] == ParticipantType.Trainer) {
					Iterable<Entity> entities = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.entitiesForRendering() : null;
					Entity closest = findClosestEntityToBlockPos(Objects.requireNonNull(entities), entity.blockPosition(), entity);

					if (closest instanceof NPCTrainer) {
						return (NPCTrainer) closest;
					}
					break;
				}
			}
		}
		return null;
	}

	public Entity findClosestEntityToBlockPos(Iterable<Entity> entities, BlockPos position, Entity exclude) {
		double minDistanceSquared = Double.MAX_VALUE;
		Entity closest = null;

		for (Entity e : entities) {
			if (e.equals(exclude)) {
				continue;
			}

			double distanceSquared = e.distanceToSqr(position.getX(), position.getY(), position.getZ());
			if (distanceSquared < minDistanceSquared) {
				minDistanceSquared = distanceSquared;
				closest = e;
			}
		}

		return closest;
	}

	@Override
	public String toString() {
		return (
			"TrainerCondition{" +
			"trainerType='" +
			trainerType +
			'\'' +
			", textureIndex=" +
			textureIndex +
			", customSteveTexture='" +
			customSteveTexture +
			'\'' +
			", name='" +
			name +
			'\'' +
			", gymLeader=" +
			gymLeader +
			", minPartyLevel=" +
			minPartyLevel +
			", maxPartyLevel=" +
			maxPartyLevel +
			", location='" +
			location +
			'\'' +
			'}'
		);
	}
}
