package ethan.hoenn.rnbrules.gui.gamecorner;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import ethan.hoenn.rnbrules.gui.gamecorner.confirmation.YNConfirmationGui;
import ethan.hoenn.rnbrules.gui.gamecorner.gamecornerroll.GamecornerRollGui;
import ethan.hoenn.rnbrules.network.BadgesPacket;
import ethan.hoenn.rnbrules.network.GlobalOTsPacket;
import ethan.hoenn.rnbrules.network.PacketHandler;
import ethan.hoenn.rnbrules.registries.GuiRegistry;
import ethan.hoenn.rnbrules.utils.enums.Badge;
import ethan.hoenn.rnbrules.utils.managers.BadgeManager;
import ethan.hoenn.rnbrules.utils.managers.GlobalOTManager;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkHooks;

@SuppressWarnings("NullableProblems")
public class GamecornerGui {

	private static final String GAMECORNER_OT = "gamecorner";

	private static Set<String> clientPlayerBadges = new HashSet<>();

	private static boolean clientHasGamecornerOT = false;

	public static void setClientPlayerBadges(Set<String> badges) {
		clientPlayerBadges = badges;
	}

	public static void setClientGlobalOT(String otName, boolean hasOT) {
		if (otName.equals(GAMECORNER_OT)) {
			clientHasGamecornerOT = hasOT;
		}
	}

	public static boolean hasBadgeOnClient(String badgeId) {
		for (String playerBadge : clientPlayerBadges) {
			if (badgeId.equalsIgnoreCase(playerBadge)) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasGamecornerOTOnClient() {
		return clientHasGamecornerOT;
	}

	public static void openGui(ServerPlayerEntity player) {
		BadgeManager badgeManager = BadgeManager.get((ServerWorld) player.level);
		Set<String> playerBadges = badgeManager.getPlayerBadges(player.getUUID());

		GlobalOTManager otManager = GlobalOTManager.get(player.getLevel());
		boolean hasGamecornerOT = otManager.playerHasGlobalOT(player.getUUID(), GAMECORNER_OT);

		PacketHandler.INSTANCE.sendTo(new BadgesPacket(playerBadges), player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);

		PacketHandler.INSTANCE.sendTo(new GlobalOTsPacket(GAMECORNER_OT, hasGamecornerOT), player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);

		NetworkHooks.openGui(
			player,
			new INamedContainerProvider() {
				@Override
				public ITextComponent getDisplayName() {
					return new StringTextComponent("Game Corner").withStyle(TextFormatting.WHITE);
				}

				@Nullable
				@Override
				public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
					return new GamecornerContainer(windowId, inventory);
				}
			}
		);
	}

	private static final Set<Integer> FUNCTIONAL_SLOTS = IntStream.rangeClosed(11, 15).boxed().collect(Collectors.toSet());

	public static class GamecornerContainer extends Container {

		private static final EmptyInventory EMPTY_INVENTORY = new EmptyInventory(27);

		private static final Set<Integer> ORANGE_PANE_SLOTS = IntStream.concat(IntStream.rangeClosed(0, 8), IntStream.concat(IntStream.of(9, 17), IntStream.rangeClosed(18, 26)))
			.boxed()
			.collect(Collectors.toSet());

		private static final Set<Integer> REDSTONE_TORCH_SLOTS = new HashSet<>(Arrays.asList(10, 16));

		private static final Map<Integer, String> SLOT_TO_LEVEL_SPEC = new HashMap<>();

		private static final Map<Integer, Badge> SLOT_TO_BADGE = new HashMap<>();

		private static final Map<Integer, Integer> SLOT_TO_COST = new HashMap<>();

		static {
			SLOT_TO_LEVEL_SPEC.put(11, "lvl:42");
			SLOT_TO_LEVEL_SPEC.put(12, "lvl:57");
			SLOT_TO_LEVEL_SPEC.put(13, "lvl:69");
			SLOT_TO_LEVEL_SPEC.put(14, "lvl:85");
			SLOT_TO_LEVEL_SPEC.put(15, "lvl:91");

			SLOT_TO_BADGE.put(11, Badge.BALANCE_BADGE);
			SLOT_TO_BADGE.put(12, Badge.HEAT_BADGE);
			SLOT_TO_BADGE.put(13, Badge.FEATHER_BADGE);
			SLOT_TO_BADGE.put(14, Badge.MIND_BADGE);
			SLOT_TO_BADGE.put(15, Badge.RAIN_BADGE);

			SLOT_TO_COST.put(11, 5000);
			SLOT_TO_COST.put(12, 10000);
			SLOT_TO_COST.put(13, 15000);
			SLOT_TO_COST.put(14, 20000);
			SLOT_TO_COST.put(15, 25000);
		}

		private final ServerPlayerEntity serverPlayer;
		private final Set<String> playerBadges;
		private final boolean hasGamecornerOT;

		public GamecornerContainer(int windowId, PlayerInventory playerInventory) {
			super(GuiRegistry.GAMECORNER_CONTAINER.get(), windowId);
			if (playerInventory.player instanceof ServerPlayerEntity) {
				this.serverPlayer = (ServerPlayerEntity) playerInventory.player;
				BadgeManager badgeManager = BadgeManager.get((ServerWorld) serverPlayer.level);
				this.playerBadges = badgeManager.getPlayerBadges(serverPlayer.getUUID());

				GlobalOTManager otManager = GlobalOTManager.get(serverPlayer.getLevel());
				this.hasGamecornerOT = otManager.playerHasGlobalOT(serverPlayer.getUUID(), GAMECORNER_OT);
			} else {
				this.serverPlayer = null;
				this.playerBadges = Collections.emptySet();
				this.hasGamecornerOT = false;
			}

			ItemStack orangePane = new ItemStack(Items.ORANGE_STAINED_GLASS_PANE).setHoverName(new StringTextComponent(" "));
			ItemStack redstoneTorch = new ItemStack(Items.REDSTONE_TORCH).setHoverName(new StringTextComponent(" "));

			List<Map.Entry<ItemStack, List<Pokemon>>> badgePools = new ArrayList<>();

			try {
				if (GamecornerAssets.BADGE_TO_POKEMON_POOL == null) {
					GamecornerAssets.initializeAssets();
				}

				if (GamecornerAssets.BADGE_TO_POKEMON_POOL != null) {
					badgePools.addAll(GamecornerAssets.BADGE_TO_POKEMON_POOL.entrySet());
				} else {
					for (int i = 0; i < 5; i++) {
						badgePools.add(new AbstractMap.SimpleEntry<>(new ItemStack(Items.GRAY_DYE), new ArrayList<>()));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();

				for (int i = 0; i < 5; i++) {
					badgePools.add(new AbstractMap.SimpleEntry<>(new ItemStack(Items.GRAY_DYE), new ArrayList<>()));
				}
			}
			int badgeIndex = 0;

			for (int row = 0; row < 3; ++row) {
				for (int col = 0; col < 9; ++col) {
					int slotIndex = col + row * 9;
					int x = 8 + col * 18;
					int y = 18 + row * 18;

					if (FUNCTIONAL_SLOTS.contains(slotIndex) && badgeIndex < badgePools.size()) {
						Map.Entry<ItemStack, List<Pokemon>> entry = badgePools.get(badgeIndex++);
						ItemStack badgeStack = entry.getKey().copy();
						List<Pokemon> pokemonPool = entry.getValue();

						Badge requiredBadge = SLOT_TO_BADGE.get(slotIndex);

						boolean hasBadge = false;
						boolean hasOT = false;

						if (playerInventory.player.level.isClientSide) {
							hasBadge = requiredBadge != null && GamecornerGui.hasBadgeOnClient(requiredBadge.getBadgeId());
							hasOT = GamecornerGui.hasGamecornerOTOnClient();
						} else {
							hasBadge = requiredBadge != null && playerBadges.stream().anyMatch(playerBadge -> requiredBadge.getBadgeId().equalsIgnoreCase(playerBadge));
							hasOT = this.hasGamecornerOT;
						}

						if (hasBadge && !hasOT) {
							if (!badgeStack.hasCustomHoverName()) {
								String badgeName = formatItemName(badgeStack.getItem().getRegistryName().getPath());
								badgeStack.setHoverName(new StringTextComponent(badgeName).withStyle(TextFormatting.AQUA, TextFormatting.BOLD));
							} else {
								badgeStack.setHoverName(badgeStack.getHoverName().copy().withStyle(TextFormatting.AQUA, TextFormatting.BOLD));
							}

							ListNBT loreList = new ListNBT();
							double chance = pokemonPool.isEmpty() ? 0.0 : 100.0 / pokemonPool.size();
							String chanceStr = String.format("%.1f%%", chance);

							int cost = SLOT_TO_COST.getOrDefault(slotIndex, 0);
							loreList.add(
								StringNBT.valueOf(
									ITextComponent.Serializer.toJson(new StringTextComponent("Cost: ").withStyle(TextFormatting.WHITE).append(new StringTextComponent("₽" + cost).withStyle(TextFormatting.GOLD)))
								)
							);

							for (Pokemon pokemon : pokemonPool) {
								String formName = pokemon.getForm().getLocalizedName();
								String speciesName = pokemon.getSpecies().getLocalizedName();
								String displayName;

								if (formName != null && !formName.isEmpty() && !formName.equalsIgnoreCase("none")) {
									String capitalizedFormName = formName.substring(0, 1).toUpperCase() + formName.substring(1).toLowerCase();
									displayName = capitalizedFormName + " " + speciesName;
								} else {
									displayName = speciesName;
								}

								loreList.add(
									StringNBT.valueOf(
										ITextComponent.Serializer.toJson(
											new StringTextComponent("- ")
												.withStyle(TextFormatting.WHITE)
												.append(new StringTextComponent(displayName).withStyle(TextFormatting.GREEN))
												.append(new StringTextComponent(" (").withStyle(TextFormatting.WHITE))
												.append(new StringTextComponent(chanceStr).withStyle(TextFormatting.AQUA))
												.append(new StringTextComponent(")").withStyle(TextFormatting.WHITE))
										)
									)
								);
							}
							badgeStack.getOrCreateTagElement("display").put("Lore", loreList);

							this.addSlot(new BadgeSelectionSlot(EMPTY_INVENTORY, slotIndex, x, y, badgeStack, pokemonPool, true));
						} else {
							ItemStack grayDye = new ItemStack(Items.GRAY_DYE);
							String badgeName = requiredBadge != null ? formatItemName(requiredBadge.getBadgeId()) : "Unknown Badge";
							grayDye.setHoverName(new StringTextComponent(badgeName).withStyle(TextFormatting.GRAY));

							ListNBT loreList = new ListNBT();
							String loreText;

							if (hasOT) {
								loreText = "You have already received your Game Corner reward.";
							} else {
								loreText = "You must earn the " + badgeName + " first.";
							}

							loreList.add(StringNBT.valueOf(ITextComponent.Serializer.toJson(new StringTextComponent(loreText).withStyle(TextFormatting.RED))));

							grayDye.getOrCreateTagElement("display").put("Lore", loreList);
							this.addSlot(new BadgeSelectionSlot(EMPTY_INVENTORY, slotIndex, x, y, grayDye, Collections.emptyList(), false));
						}
					} else if (ORANGE_PANE_SLOTS.contains(slotIndex)) {
						this.addSlot(new FillerSlot(EMPTY_INVENTORY, slotIndex, x, y, orangePane.copy()));
					} else if (REDSTONE_TORCH_SLOTS.contains(slotIndex)) {
						this.addSlot(new FillerSlot(EMPTY_INVENTORY, slotIndex, x, y, redstoneTorch.copy()));
					} else {
						this.addSlot(new FillerSlot(EMPTY_INVENTORY, slotIndex, x, y, ItemStack.EMPTY));
					}
				}
			}
		}

		private String formatItemName(String registryName) {
			String[] parts = registryName.split("_");
			return Arrays.stream(parts).map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase()).collect(Collectors.joining(" "));
		}

		@Override
		public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
			if (slotId >= 0 && slotId < this.slots.size()) {
				Slot slot = this.slots.get(slotId);
				if (slot instanceof FillerSlot) {
					return ItemStack.EMPTY;
				} else if (slot instanceof BadgeSelectionSlot && clickTypeIn == ClickType.PICKUP) {
					BadgeSelectionSlot badgeSlot = (BadgeSelectionSlot) slot;

					if (!badgeSlot.isEnabled()) {
						return ItemStack.EMPTY;
					}

					if (!player.level.isClientSide) {
						ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
						ItemStack displayStack = badgeSlot.getItem().copy();
						List<Pokemon> pokemonPool = badgeSlot.getPokemonPool();
						String levelSpec = SLOT_TO_LEVEL_SPEC.getOrDefault(slotId, "");

						if (pokemonPool == null || pokemonPool.isEmpty()) {
							serverPlayer.sendMessage(new StringTextComponent("Error: Selected pool is empty.").withStyle(TextFormatting.RED), serverPlayer.getUUID());
							return ItemStack.EMPTY;
						}
						if (levelSpec.isEmpty()) {
							serverPlayer.sendMessage(new StringTextComponent("Error: Could not determine level for this badge.").withStyle(TextFormatting.RED), serverPlayer.getUUID());
							return ItemStack.EMPTY;
						}

						Random random = new Random();
						Pokemon winningPokemon = pokemonPool.get(random.nextInt(pokemonPool.size()));

						int cost = SLOT_TO_COST.getOrDefault(slotId, 0);

						YNConfirmationGui.openGui(
							serverPlayer,
							"§fConfirm Selection?",
							displayStack,
							pokemonPool,
							winningPokemon,
							levelSpec,
							cost,
							(confirmed, poolFromCallback, winnerFromCallback, levelSpecFromCallback) -> {
								if (confirmed) {
									GamecornerRollGui.openGui(serverPlayer, poolFromCallback, winnerFromCallback, levelSpecFromCallback);
								}
							}
						);
					}
					return ItemStack.EMPTY;
				}
			}
			if (player.inventory != null) {
				player.inventory.setCarried(ItemStack.EMPTY);
			}
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
			return ItemStack.EMPTY;
		}

		@Override
		public boolean canTakeItemForPickAll(ItemStack stack, Slot slotIn) {
			return false;
		}

		@Override
		public boolean canDragTo(Slot slotIn) {
			return false;
		}

		@Override
		public boolean stillValid(PlayerEntity playerIn) {
			return true;
		}
	}

	public static class BadgeSelectionSlot extends Slot {

		private final ItemStack displayStack;
		private final List<Pokemon> pokemonPool;
		private final boolean enabled;

		public BadgeSelectionSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, ItemStack displayStack, List<Pokemon> pokemonPool, boolean enabled) {
			super(inventoryIn, index, xPosition, yPosition);
			this.displayStack = displayStack;
			this.pokemonPool = Collections.unmodifiableList(new ArrayList<>(pokemonPool));
			this.enabled = enabled;
		}

		public List<Pokemon> getPokemonPool() {
			return pokemonPool;
		}

		public boolean isEnabled() {
			return enabled;
		}

		@Override
		public ItemStack getItem() {
			return displayStack;
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return false;
		}

		@Override
		public boolean mayPickup(PlayerEntity playerIn) {
			return enabled;
		}

		@Override
		public boolean isActive() {
			return true;
		}

		@Override
		public int getMaxStackSize() {
			return 1;
		}

		@Override
		public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {
			return ItemStack.EMPTY;
		}
	}

	public static class FillerSlot extends Slot {

		private final ItemStack displayStack;

		public FillerSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, ItemStack displayStack) {
			super(inventoryIn, index, xPosition, yPosition);
			this.displayStack = displayStack;
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return false;
		}

		@Override
		public boolean mayPickup(PlayerEntity playerIn) {
			return false;
		}

		@Override
		public boolean isActive() {
			return true;
		}

		@Override
		public ItemStack getItem() {
			return displayStack;
		}

		@Override
		public int getMaxStackSize() {
			return 1;
		}
	}

	private static class EmptyInventory implements IInventory {

		private final int size;

		public EmptyInventory(int size) {
			this.size = size;
		}

		@Override
		public int getContainerSize() {
			return this.size;
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public ItemStack getItem(int index) {
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack removeItem(int index, int count) {
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack removeItemNoUpdate(int index) {
			return ItemStack.EMPTY;
		}

		@Override
		public void setItem(int index, ItemStack stack) {}

		@Override
		public void setChanged() {}

		@Override
		public boolean stillValid(PlayerEntity player) {
			return true;
		}

		@Override
		public void clearContent() {}
	}
}
