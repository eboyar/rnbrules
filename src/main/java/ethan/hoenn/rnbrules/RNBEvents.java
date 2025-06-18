package ethan.hoenn.rnbrules;

import com.pixelmonmod.pixelmon.items.AbilityCapsuleItem;
import com.pixelmonmod.pixelmon.items.AbilityPatchItem;
import com.pixelmonmod.pixelmon.items.BadgeItem;
import com.pixelmonmod.pixelmon.items.BikeItem;
import com.pixelmonmod.pixelmon.items.BottlecapItem;
import com.pixelmonmod.pixelmon.items.EvolutionStoneItem;
import com.pixelmonmod.pixelmon.items.ExpCandyItem;
import com.pixelmonmod.pixelmon.items.HMItem;
import com.pixelmonmod.pixelmon.items.HeldItem;
import com.pixelmonmod.pixelmon.items.MintItem;
import com.pixelmonmod.pixelmon.items.PokeBagItem;
import com.pixelmonmod.pixelmon.items.QuestItem;
import com.pixelmonmod.pixelmon.items.TechnicalMoveItem;
import com.pixelmonmod.pixelmon.items.ValuableItem;
import com.pixelmonmod.pixelmon.items.heldItems.MegaStoneItem;
import ethan.hoenn.rnbrules.dialogue.DialogueManager;
import ethan.hoenn.rnbrules.dialogue.DialogueRegistry;
import ethan.hoenn.rnbrules.gui.ferry.FerryCountdown;
import ethan.hoenn.rnbrules.gui.flight.FlyCountdown;
import ethan.hoenn.rnbrules.gui.gamecorner.GamecornerAssets;
import ethan.hoenn.rnbrules.gui.league.LeagueBattleCountdown;
import ethan.hoenn.rnbrules.gui.safari.SafariCountdown;
import ethan.hoenn.rnbrules.items.*;
import ethan.hoenn.rnbrules.listeners.LocationListener;
import ethan.hoenn.rnbrules.registries.CommandRegistry;
import ethan.hoenn.rnbrules.utils.managers.*;
import ethan.hoenn.rnbrules.utils.misc.HungerCounter;
import ethan.hoenn.rnbrules.utils.misc.PlayerFreezeTracker;
import ethan.hoenn.rnbrules.utils.notifications.LocationNotifier;
import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = { Dist.CLIENT, Dist.DEDICATED_SERVER })
public class RNBEvents {

	private static ServerWorld overworld = null;

	@SubscribeEvent
	public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getPlayer() instanceof ServerPlayerEntity) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
			ServerWorld world = player.getLevel();
			LevelCapManager manager = LevelCapManager.get(world);

			player.getFoodData().setFoodLevel(20);
			UUID uuid = player.getUUID();
			if (!manager.hasLevelCap(uuid)) {
				manager.setLevelCap(uuid, 5);
			}
		}
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			if (HungerCounter.incrementAndGetTick() % 200 == 0) {
				MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
				if (server != null) {
					for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
						player.getFoodData().setFoodLevel(20);
					}
				}
			}

			FlyManager.tickAll();
			FerryManager.tickAll();
			FlyCountdown.tickAll();
			FerryCountdown.tickAll();
			SafariCountdown.tickAll();
			PlayerFreezeTracker.tickAll();
			LeagueBattleCountdown.tickAll();
			MultiBattleManager.tickTrainerRemovals();
			LocationNotifier.tickUpdate();

			if (overworld != null) {
				LeagueManager.get(overworld).tickCleanupQueue();
			} else {
				overworld = ServerLifecycleHooks.getCurrentServer().getLevel(World.OVERWORLD);
			}
		}
	}

	@SubscribeEvent
	public static void onDamageEvent(LivingDamageEvent event) {
		if (event.getEntity() instanceof ServerPlayerEntity) {
			event.setCanceled(true);
		}
	}

	//todo move this to a better place
	@SubscribeEvent
	public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
		ItemStack itemStack = event.getItemStack();

		if (itemStack.getItem() instanceof HMItem) {
			if (event.getWorld().isClientSide() || !(event.getPlayer() instanceof ServerPlayerEntity)) {
				return;
			}

			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
			HMItem hmItem = (HMItem) itemStack.getItem();
			String hmName = hmItem.attackName;

			HiddenMachineManager hmManager = HiddenMachineManager.get(player.getLevel());

			if (!hmManager.hasHM(player.getUUID(), hmName.toLowerCase())) {
				hmManager.addHM(player.getUUID(), hmName.toLowerCase());

				player.connection.send(new STitlePacket(STitlePacket.Type.TITLE, new StringTextComponent("You've unlocked the HM: " + "§b" + hmName).withStyle(TextFormatting.YELLOW)));
				player.connection.send(new STitlePacket(STitlePacket.Type.TIMES, null, 10, 45, 20));

				player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, SoundCategory.PLAYERS, 1.0F, 1.0F);

				event.setCanceled(true);
				event.setCancellationResult(ActionResultType.SUCCESS);
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerDropEvent(ItemTossEvent event) {
		PlayerEntity player = event.getPlayer();
		ItemStack itemS = event.getEntityItem().getItem();
		Item item = itemS.getItem();

		if (
			item instanceof TechnicalMoveItem ||
			item instanceof HMItem ||
			item instanceof BadgeItem ||
			item instanceof HeldItem ||
			item instanceof PokeBagItem ||
			item instanceof EvolutionStoneItem ||
			item instanceof EndlessCandy ||
			item instanceof PartyRestore ||
			item instanceof MaxPartyRestore ||
			item instanceof Battery ||
			item instanceof MegaGem ||
			item instanceof ExpCandyItem ||
			item instanceof ValuableItem ||
			item instanceof QuestItem ||
			item instanceof BigBackpack ||
			item instanceof BottlecapItem ||
			item instanceof AbilityCapsuleItem ||
			item instanceof AbilityPatchItem ||
			item instanceof MintItem ||
			item instanceof BikeItem ||
			item instanceof GenericUpgradeItem ||
			item instanceof GenericUpgradeComponentItem ||
			item instanceof MaxHealingSerum ||
			item instanceof MegaStoneItem
		) {
			event.getEntityItem().setNoPickUpDelay();
			event.getEntityItem().setOwner(player.getUUID());
		}
	}

	@SubscribeEvent
	public static void onPlayerPickupItem(PlayerEvent.ItemPickupEvent event) {
		if (event.getPlayer() instanceof ServerPlayerEntity) {
			PlayerEntity player = event.getPlayer();
			ItemStack itemS = event.getStack();
			Item item = itemS.getItem();

			if (
				item instanceof TechnicalMoveItem ||
				item instanceof HMItem ||
				item instanceof BadgeItem ||
				item instanceof HeldItem ||
				item instanceof PokeBagItem ||
				item instanceof EvolutionStoneItem ||
				item instanceof EndlessCandy ||
				item instanceof PartyRestore ||
				item instanceof MaxPartyRestore ||
				item instanceof Battery ||
				item instanceof MegaGem ||
				item instanceof ExpCandyItem ||
				item instanceof ValuableItem ||
				item instanceof QuestItem ||
				item instanceof BigBackpack ||
				item instanceof BottlecapItem ||
				item instanceof AbilityCapsuleItem ||
				item instanceof AbilityPatchItem ||
				item instanceof MintItem ||
				item instanceof BikeItem ||
				item instanceof GenericUpgradeItem ||
				item instanceof GenericUpgradeComponentItem ||
				item instanceof MaxHealingSerum ||
				item instanceof MegaStoneItem
			) {
				if (event.getOriginalEntity().getOwner() != player.getUUID()) {
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onServerWorldLoad(WorldEvent.Load event) {
		if (!(event.getWorld() instanceof ServerWorld)) {
			return;
		}

		ServerWorld serverWorld = (ServerWorld) event.getWorld();

		if (!serverWorld.dimension().equals(World.OVERWORLD)) {
			return;
		}

		MinecraftServer server = serverWorld.getServer();

		Path worldPath = server.getWorldPath(FolderName.ROOT).normalize();
		File dataDir = worldPath.toFile();

		ProgressionManager.init(serverWorld);
		SettingsManager.init(serverWorld);
		DialogueRegistry.INSTANCE.loadAllDialogues(dataDir);
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onClientWorldLoad(WorldEvent.Load event) {
		if (!(event.getWorld() instanceof ClientWorld)) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		if (mc.getSingleplayerServer() == null) {
			return;
		}

		Path worldPath = mc.getSingleplayerServer().getWorldPath(net.minecraft.world.storage.FolderName.ROOT).normalize();
		File dataDir = worldPath.toFile();

		DialogueRegistry.INSTANCE.loadAllDialogues(dataDir);
	}

	@SubscribeEvent
	public static void onServerStarting(FMLServerStartingEvent event) {
		RivalTeamsManager.getInstance().preloadAllTeams();
		GamecornerAssets.initializeAssets();

		ServerWorld overworld = event.getServer().getLevel(World.OVERWORLD);
		if (overworld != null) {
			LocationManager.get(overworld);
			MinecraftForge.EVENT_BUS.register(new LocationListener());
		}
	}

	@SubscribeEvent
	public static void onServerStopping(FMLServerStoppingEvent event) {
		try {
			RivalTeamsManager.getInstance().shutdown();
			DialogueManager.INSTANCE.shutdown();
		} catch (Exception e) {
			System.err.println("Error during shutdown: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggedOutEvent event) {
		try {
			RivalTeamsManager.getInstance().shutdown();
			DialogueManager.INSTANCE.shutdown();
		} catch (Exception e) {
			System.err.println("Error during client disconnect cleanup: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		CommandRegistry.registerAll(event.getDispatcher());
		CommandRegistry.deRegisterAll(event.getDispatcher());
	}
}
