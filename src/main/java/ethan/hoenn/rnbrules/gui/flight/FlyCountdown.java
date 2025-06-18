package ethan.hoenn.rnbrules.gui.flight;

import ethan.hoenn.rnbrules.RNBConfig;
import ethan.hoenn.rnbrules.utils.managers.LocationManager;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class FlyCountdown {

	private static final Map<UUID, FlyCountdown> ACTIVE_COUNTDOWNS = new HashMap<>();

	private final ServerPlayerEntity player;
	private final String destination;
	private final RNBConfig.TeleportLocation location;
	private int ticksRemaining;
	private int lastDisplayedSecond;

	private FlyCountdown(ServerPlayerEntity player, String destination, RNBConfig.TeleportLocation location) {
		this.player = player;
		this.destination = destination;
		this.location = location;
		this.ticksRemaining = 100;
		this.lastDisplayedSecond = 5;

		String title = "Flying to " + destination;
		player.connection.send(new STitlePacket(STitlePacket.Type.TITLE, new StringTextComponent(title).withStyle(TextFormatting.GOLD), 10, 120, 20));
	}

	public static void start(ServerPlayerEntity player, String destination, RNBConfig.TeleportLocation location) {
		UUID playerID = player.getUUID();

		if (ACTIVE_COUNTDOWNS.containsKey(playerID)) {
			ACTIVE_COUNTDOWNS.remove(playerID);
		}

		FlyCountdown countdown = new FlyCountdown(player, destination, location);
		ACTIVE_COUNTDOWNS.put(playerID, countdown);

		countdown.updateSubtitle(5);
		countdown.playStartSound();
	}

	public static void tickAll() {
		Iterator<Map.Entry<UUID, FlyCountdown>> iterator = ACTIVE_COUNTDOWNS.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry<UUID, FlyCountdown> entry = iterator.next();
			FlyCountdown countdown = entry.getValue();

			boolean completed = countdown.tick();

			if (completed) {
				iterator.remove();
			}
		}
	}

	private boolean tick() {
		if (!player.isAlive() || player.hasDisconnected()) {
			return true;
		}

		ticksRemaining--;

		int currentSecond = (ticksRemaining + 19) / 20;

		if (currentSecond < lastDisplayedSecond) {
			lastDisplayedSecond = currentSecond;

			if (currentSecond > 0) {
				updateSubtitle(currentSecond);
				playTickSound(currentSecond);
			} else {
				finishTeleport();
				playTeleportSound();
				return true;
			}
		}

		return false;
	}

	private void updateSubtitle(int count) {
		String subtitle = "Arriving in " + count + "...";

		player.connection.send(new STitlePacket(STitlePacket.Type.SUBTITLE, new StringTextComponent(subtitle).withStyle(TextFormatting.YELLOW), 10, 50, 20));
	}

	private void playStartSound() {
		player.playNotifySound(SoundEvents.PHANTOM_FLAP, SoundCategory.PLAYERS, 1.0F, 1.0F);
	}

	private void playTickSound(int count) {
		float pitch = 0.6F + ((5 - count) * 0.1F);

		player.playNotifySound(SoundEvents.BAT_TAKEOFF, SoundCategory.PLAYERS, 0.3F, pitch);
	}

	private void playTeleportSound() {
		player.playNotifySound(SoundEvents.ENDER_DRAGON_FLAP, SoundCategory.PLAYERS, 1.0F, 1.0F);
	}

	private void finishTeleport() {
		player.connection.send(new STitlePacket(STitlePacket.Type.TITLE, new StringTextComponent("Welcome to " + destination).withStyle(TextFormatting.GREEN), 10, 30, 10));
		player.connection.send(new STitlePacket(STitlePacket.Type.SUBTITLE, new StringTextComponent(""), 10, 0, 10));

		LocationManager lm = LocationManager.get(player.getLevel());
		lm.updatePlayerLocation(player, lm.addLocationFormatting(destination));

		player.teleportTo(location.x, location.y, location.z);
	}
}
