package ethan.hoenn.rnbrules.gui.safari;

import com.pixelmonmod.pixelmon.api.pokemon.item.pokeball.PokeBall;
import com.pixelmonmod.pixelmon.api.pokemon.item.pokeball.PokeBallRegistry;
import ethan.hoenn.rnbrules.RNBConfig;
import ethan.hoenn.rnbrules.utils.managers.SafariManager;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

public class SafariCountdown {

	private static final Map<UUID, SafariCountdown> ACTIVE_COUNTDOWNS = new HashMap<>();

	private final ServerPlayerEntity player;
	private final boolean isEntering;
	private final RNBConfig.TeleportLocation location;
	private final Consumer<ServerPlayerEntity> onComplete;
	private int ticksRemaining;
	private int lastDisplayedSecond;
	private int initialSilentTicks;
	private boolean countdownStarted;

	private SafariCountdown(ServerPlayerEntity player, boolean isEntering, RNBConfig.TeleportLocation location, Consumer<ServerPlayerEntity> onComplete, int additionalSilentSeconds) {
		this.player = player;
		this.isEntering = isEntering;
		this.location = location;
		this.onComplete = onComplete;
		this.initialSilentTicks = additionalSilentSeconds * 20;
		this.ticksRemaining = 100 + this.initialSilentTicks;
		this.lastDisplayedSecond = 5;
		this.countdownStarted = false;
	}

	public static void startEntering(ServerPlayerEntity player, RNBConfig.TeleportLocation location, int catches) {
		UUID playerID = player.getUUID();

		ACTIVE_COUNTDOWNS.remove(playerID);

		Consumer<ServerPlayerEntity> onComplete = p -> {
			ServerWorld world = p.getLevel();
			SafariManager safariManager = SafariManager.get(world);
			safariManager.startSafari(p.getUUID(), catches);
			safariManager.enableScoreboard(p);
		};

		SafariCountdown countdown = new SafariCountdown(player, true, location, onComplete, 0);
		ACTIVE_COUNTDOWNS.put(playerID, countdown);

		countdown.startCountdownVisuals();
	}

	public static void startExiting(ServerPlayerEntity player, RNBConfig.TeleportLocation location, int additionalSilentSeconds) {
		UUID playerID = player.getUUID();

		ACTIVE_COUNTDOWNS.remove(playerID);
		Consumer<ServerPlayerEntity> onComplete = p -> {
			ServerWorld world = p.getLevel();
			SafariManager safariManager = SafariManager.get(world);
			safariManager.endSafari(p.getUUID());
			safariManager.disableScoreboard(p);

			String completionCommand = RNBConfig.getSafariCompletionCommand();
			if (completionCommand != null && !completionCommand.isEmpty()) {
				String command = completionCommand.replace("@pl", p.getScoreboardName());
				p.getServer().getCommands().performCommand(p.getServer().createCommandSourceStack().withPermission(4), command);
			}
		};

		SafariCountdown countdown = new SafariCountdown(player, false, location, onComplete, additionalSilentSeconds);
		ACTIVE_COUNTDOWNS.put(playerID, countdown);

		if (additionalSilentSeconds <= 0) {
			countdown.startCountdownVisuals();
		}
	}

	public static void startExiting(ServerPlayerEntity player, RNBConfig.TeleportLocation location) {
		startExiting(player, location, 0);
	}

	private void startCountdownVisuals() {
		countdownStarted = true;

		String title = isEntering ? "Entering Safari Zone" : "Exiting Safari Zone";
		TextFormatting titleColor = isEntering ? TextFormatting.GREEN : TextFormatting.GOLD;
		player.connection.send(new STitlePacket(STitlePacket.Type.TITLE, new StringTextComponent(title).withStyle(titleColor), 10, 120, 20));

		updateSubtitle(5);
		playStartSound();
	}

	public static void tickAll() {
		Iterator<Map.Entry<UUID, SafariCountdown>> iterator = ACTIVE_COUNTDOWNS.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry<UUID, SafariCountdown> entry = iterator.next();
			SafariCountdown countdown = entry.getValue();

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

		if (!countdownStarted && initialSilentTicks > 0 && ticksRemaining <= 100) {
			initialSilentTicks = 0;
			startCountdownVisuals();
			return false;
		}

		if (countdownStarted) {
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
		}

		return false;
	}

	private void updateSubtitle(int count) {
		String action = isEntering ? "Beginning" : "Ending";
		String subtitle = action + " in " + count + "...";

		player.connection.send(new STitlePacket(STitlePacket.Type.SUBTITLE, new StringTextComponent(subtitle).withStyle(TextFormatting.YELLOW), 10, 40, 20));
	}

	private void playStartSound() {
		player.playNotifySound(SoundEvents.UI_BUTTON_CLICK, SoundCategory.PLAYERS, 1.0F, 1.0F);
	}

	private void playTickSound(int count) {
		float pitch = 0.8F + ((5 - count) * 0.1F);
		player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.5F, pitch);
	}

	private void playTeleportSound() {
		if (isEntering) {
			player.playNotifySound(SoundEvents.CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 0.8F, 1.0F);
		} else {
			player.playNotifySound(SoundEvents.CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 0.8F, 0.8F);
		}
	}

	private void finishTeleport() {
		String message = isEntering ? "Welcome to the Safari Zone!" : "Thank you for visiting!";
		TextFormatting messageColor = isEntering ? TextFormatting.GREEN : TextFormatting.GOLD;

		player.connection.send(new STitlePacket(STitlePacket.Type.TITLE, new StringTextComponent(message).withStyle(messageColor), 10, 30, 10));
		player.connection.send(new STitlePacket(STitlePacket.Type.SUBTITLE, new StringTextComponent(""), 10, 0, 10));

		player.teleportTo(location.x, location.y, location.z);

		if (isEntering) {
			ItemStack safariBalls = getSafariBallStack(20);
			if (!player.inventory.add(safariBalls)) {
				player.drop(safariBalls, false);
				player.sendMessage(new StringTextComponent(TextFormatting.YELLOW + "Your inventory was full. Safari Balls dropped on the ground!"), player.getUUID());
			} else {
				player.sendMessage(new StringTextComponent(TextFormatting.GREEN + "You received 20 Safari Balls!"), player.getUUID());
			}
		} else {
			int removed = 0;
			for (int i = 0; i < player.inventory.getContainerSize(); i++) {
				ItemStack stack = player.inventory.getItem(i);
				if (!stack.isEmpty() && stack.getItem().equals(PokeBallRegistry.SAFARI_BALL.getValueUnsafe().getBallItem().getItem())) {
					removed += stack.getCount();
					player.inventory.setItem(i, ItemStack.EMPTY);
				}
			}

			if (removed > 0) {
				player.sendMessage(new StringTextComponent(TextFormatting.GOLD + "Your Safari Balls have been returned."), player.getUUID());
			}
		}

		onComplete.accept(player);
	}

	public static ItemStack getSafariBallStack(int count) {
		PokeBall safariBall = PokeBallRegistry.SAFARI_BALL.getValueUnsafe();
		if (safariBall != null) {
			ItemStack stack = safariBall.getBallItem().copy();
			stack.setCount(count);
			return stack;
		}
		return ItemStack.EMPTY;
	}
}
