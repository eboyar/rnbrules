package ethan.hoenn.rnbrules.listeners;

import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.item.pokeball.PokeBall;
import com.pixelmonmod.pixelmon.api.pokemon.item.pokeball.PokeBallRegistry;
import ethan.hoenn.rnbrules.RNBConfig;
import ethan.hoenn.rnbrules.gui.safari.SafariCountdown;
import ethan.hoenn.rnbrules.utils.enums.HiddenMachine;
import ethan.hoenn.rnbrules.utils.managers.CatchLocationManager;
import ethan.hoenn.rnbrules.utils.managers.HiddenMachineManager;
import ethan.hoenn.rnbrules.utils.managers.LevelCapManager;
import ethan.hoenn.rnbrules.utils.managers.SafariManager;
import ethan.hoenn.rnbrules.utils.misc.Flags;

import java.util.UUID;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CatchListener {

	private static final String SAFARI_LOCATION = "safarizone";

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onCatchAttempt(CaptureEvent.StartCapture event) {
		Pokemon pokemon = event.getPokemon().getPokemon();
		ServerPlayerEntity player = event.getPlayer();
		CompoundNBT pd = pokemon.getPersistentData();
		UUID playerUUID = player.getUUID();
		ServerWorld world = player.getLevel();
		LevelCapManager levelCapManager = LevelCapManager.get(world);
		HiddenMachineManager hmManager = HiddenMachineManager.get(world);
		PokeBall pball = event.getPokeBall().getBallType();
		SafariManager safariManager = SafariManager.get(world);

		String catchLocation = pd.getString("CatchLocation");

		boolean isSafariBall = pball.is(PokeBallRegistry.SAFARI_BALL);
		boolean isMasterBall = pball.is(PokeBallRegistry.MASTER_BALL);

		if (isSafariBall && !catchLocation.equalsIgnoreCase(SAFARI_LOCATION)) {
			event.setCanceled(true);
			player.sendMessage(new StringTextComponent(TextFormatting.RED + "Safari Balls can only be used on Safari Zone Pokémon!"), playerUUID);
			refundPokeBall(player, pball);
			return;
		}

		if (!catchLocation.isEmpty()) {
			CatchLocationManager catchManager = CatchLocationManager.get(world);

			boolean isSafariLocation = catchLocation.equalsIgnoreCase(SAFARI_LOCATION);
			boolean playerInSafari = safariManager.isPlayerInSafari(playerUUID);

			if (isSafariLocation && !isSafariBall) {
				event.setCanceled(true);
				player.sendMessage(new StringTextComponent(TextFormatting.RED + "Safari Zone Pokémon can only be caught with Safari Balls!"), playerUUID);
				refundPokeBall(player, pball);
				return;
			}

			if (isSafariLocation && !playerInSafari) {
				event.setCanceled(true);
				player.sendMessage(new StringTextComponent(TextFormatting.RED + "You need to be in a Safari game to catch Pokémon here!"), playerUUID);
				refundPokeBall(player, pball);
				return;
			}

			if (isSafariLocation && playerInSafari) {
				SafariManager.SafariPlayerData playerData = safariManager.getPlayerData(playerUUID);
				if (playerData.getRemainingCatches() <= 0) {
					event.setCanceled(true);
					player.sendMessage(new StringTextComponent(TextFormatting.RED + "You have no Safari catches remaining!"), playerUUID);
					refundPokeBall(player, pball);
					return;
				}

				playerData.useCatch();
				safariManager.updateScoreboard(player);

				if (playerData.getRemainingCatches() <= 0) {
					player.sendMessage(new StringTextComponent(TextFormatting.YELLOW + "You've used all your Safari catches!"), playerUUID);

					RNBConfig.TeleportLocation exitPoint = RNBConfig.getSafariExitPoint();
					if (exitPoint != null) {
						SafariCountdown.startExiting(player, exitPoint, 8);
					} else {
						safariManager.endSafari(playerUUID);
						safariManager.disableScoreboard(player);
					}
				}
			}

			if (catchManager.hasPlayerCaughtAtLocation(playerUUID, catchLocation)) {
				//right now we do nothing here, but this is the foundation for nuzlocke rules in the future
			}
		}

		if (pokemon.getPokemonLevel() > levelCapManager.getLevelCap(player.getUUID()) && !isMasterBall) {
			event.setCanceled(true);
			player.sendMessage(new StringTextComponent("§7You §4cannot§7 catch Pokémon above your level cap. §6(" + levelCapManager.getLevelCap(player.getUUID()) + ")"), player.getUUID());
			refundPokeBall(player, pball);
			return;
		}

		if (pokemon.hasFlag(Flags.NEEDS_SURF)) {
			if (!hmManager.hasHM(player.getUUID(), HiddenMachine.SURF.getHmId())) {
				event.setCanceled(true);
				player.sendMessage(new StringTextComponent("§7You need the §9Surf§7 HM to capture this Pokémon!"), player.getUUID());
				refundPokeBall(player, pball);
			}
		} else if (pokemon.hasFlag(Flags.NEEDS_ROCKSMASH)) {
			if (!hmManager.hasHM(player.getUUID(), HiddenMachine.ROCK_SMASH.getHmId())) {
				event.setCanceled(true);
				player.sendMessage(new StringTextComponent("§7You need the §8Rock Smash§7 HM to capture this Pokémon!"), player.getUUID());
				refundPokeBall(player, pball);
			}
		}
	}

	private void refundPokeBall(ServerPlayerEntity player, PokeBall pokeBall) {
		ItemStack pballItem = pokeBall.getBallItem();
		if (!player.inventory.add(pballItem)) {
			player.drop(pballItem, false);
			player.sendMessage(new StringTextComponent("§7Your Poké Ball was refunded but your inventory is full!"), player.getUUID());
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onCatchSuccessful(CaptureEvent.SuccessfulCapture event) {
		Pokemon pokemon = event.getPokemon().getPokemon();
		ServerPlayerEntity player = event.getPlayer();
		ServerWorld world = player.getLevel();
		UUID playerUUID = player.getUUID();

		String catchLocation = pokemon.getPersistentData().getString("CatchLocation");

		if (!catchLocation.isEmpty()) {
			CatchLocationManager catchManager = CatchLocationManager.get(world);

			if (!catchManager.hasPlayerCaughtAtLocation(playerUUID, catchLocation)) {
				catchManager.addCatchLocation(playerUUID, catchLocation);
			}
		}

		pokemon.removeFlag(Flags.NEEDS_SURF);
		pokemon.removeFlag(Flags.NEEDS_ROCKSMASH);
		pokemon.removeFlag(Flags.NEEDS_WATERFALL);

		pokemon.getPersistentData().remove("NeedsSpawnPoint");

	}
}
