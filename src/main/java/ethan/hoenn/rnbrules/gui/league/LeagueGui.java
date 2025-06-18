package ethan.hoenn.rnbrules.gui.league;

import ethan.hoenn.rnbrules.gui.league.formatselection.FormatSelectionGui;
import ethan.hoenn.rnbrules.registries.GuiRegistry;
import ethan.hoenn.rnbrules.registries.ItemRegistry;
import ethan.hoenn.rnbrules.utils.managers.LeagueManager;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;

@SuppressWarnings("NullableProblems")
public class LeagueGui {

	private static final int NUM_SLOTS = 27;

	private static final Set<Integer> FUNCTIONAL_SLOTS = IntStream.rangeClosed(11, 15).boxed().collect(Collectors.toSet());

	private static final Set<Integer> FILLER_SLOTS = IntStream.range(0, NUM_SLOTS).filter(i -> !FUNCTIONAL_SLOTS.contains(i)).boxed().collect(Collectors.toSet());

	private static final Map<Integer, LeagueManager.LeagueMember> SLOT_TO_MEMBER = new HashMap<>();

	static {
		SLOT_TO_MEMBER.put(11, LeagueManager.LeagueMember.SIDNEY);
		SLOT_TO_MEMBER.put(12, LeagueManager.LeagueMember.PHOEBE);
		SLOT_TO_MEMBER.put(13, LeagueManager.LeagueMember.GLACIA);
		SLOT_TO_MEMBER.put(14, LeagueManager.LeagueMember.DRAKE);
		SLOT_TO_MEMBER.put(15, LeagueManager.LeagueMember.WALLACE);
	}

	public static void openGui(ServerPlayerEntity player) {
		LeagueManager leagueManager = LeagueManager.get((ServerWorld) player.level);
		List<LeagueManager.Progress> playerProgress = leagueManager.getPlayerProgress(player.getUUID());

		Inventory leagueInventory = new Inventory(NUM_SLOTS);

		Random random = new Random();

		for (int slot : FILLER_SLOTS) {
			ItemStack fillerPane;
			if (random.nextBoolean()) {
				fillerPane = new ItemStack(Items.ORANGE_STAINED_GLASS_PANE);
			} else {
				fillerPane = new ItemStack(Items.YELLOW_STAINED_GLASS_PANE);
			}
			fillerPane.setHoverName(new StringTextComponent(" "));
			leagueInventory.setItem(slot, fillerPane);
		}

		setupLeagueMemberSlot(leagueInventory, 11, ItemRegistry.SIDNEY.get().getDefaultInstance(), "Sidney", TextFormatting.DARK_GRAY, playerProgress.get(0));
		setupLeagueMemberSlot(leagueInventory, 12, ItemRegistry.PHOEBE.get().getDefaultInstance(), "Phoebe", TextFormatting.LIGHT_PURPLE, playerProgress.get(1));
		setupLeagueMemberSlot(leagueInventory, 13, ItemRegistry.GLACIA.get().getDefaultInstance(), "Glacia", TextFormatting.AQUA, playerProgress.get(2));
		setupLeagueMemberSlot(leagueInventory, 14, ItemRegistry.DRAKE.get().getDefaultInstance(), "Drake", TextFormatting.GOLD, playerProgress.get(3));
		setupLeagueMemberSlot(leagueInventory, 15, ItemRegistry.WALLACE.get().getDefaultInstance(), "Wallace", TextFormatting.BLUE, playerProgress.get(4));

		NetworkHooks.openGui(
			player,
			new INamedContainerProvider() {
				@Override
				public ITextComponent getDisplayName() {
					return new StringTextComponent("The Hoenn League").withStyle(style -> style.withBold(true).withItalic(false));
				}

				@Nullable
				@Override
				public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
					return new LeagueContainer(windowId, inventory, leagueInventory, playerProgress);
				}
			}
		);
	}

	private static void setupLeagueMemberSlot(Inventory inventory, int slot, ItemStack stack, String name, TextFormatting color, LeagueManager.Progress progress) {
		stack.setHoverName(new StringTextComponent(name).withStyle(style -> style.withColor(color).withBold(true).withItalic(false)));

		ListNBT loreList = new ListNBT();

		switch (progress) {
			case NONE:
				loreList.add(
					StringNBT.valueOf(
						ITextComponent.Serializer.toJson(
							new StringTextComponent("Status: ")
								.withStyle(style -> style.withColor(TextFormatting.WHITE).withItalic(false))
								.append(new StringTextComponent("Not Challenged").withStyle(style -> style.withColor(TextFormatting.RED).withItalic(false)))
						)
					)
				);
				break;
			case SINGLES:
				loreList.add(
					StringNBT.valueOf(
						ITextComponent.Serializer.toJson(
							new StringTextComponent("Status: ")
								.withStyle(style -> style.withColor(TextFormatting.WHITE).withItalic(false))
								.append(new StringTextComponent("Singles Completed!").withStyle(style -> style.withColor(TextFormatting.GREEN).withItalic(false)))
						)
					)
				);
				break;
			case DOUBLES:
				loreList.add(
					StringNBT.valueOf(
						ITextComponent.Serializer.toJson(
							new StringTextComponent("Status: ")
								.withStyle(style -> style.withColor(TextFormatting.WHITE).withItalic(false))
								.append(new StringTextComponent("Doubles Completed!").withStyle(style -> style.withColor(TextFormatting.GREEN).withItalic(false)))
						)
					)
				);
				break;
			case CHAMPION:
				loreList.add(
					StringNBT.valueOf(
						ITextComponent.Serializer.toJson(
							new StringTextComponent("Status: ")
								.withStyle(style -> style.withColor(TextFormatting.WHITE).withItalic(false))
								.append(new StringTextComponent("Champion Defeated!").withStyle(style -> style.withColor(TextFormatting.GOLD).withItalic(false)))
						)
					)
				);
				break;
		}

		CompoundNBT display = stack.getOrCreateTagElement("display");
		display.put("Lore", loreList);

		inventory.setItem(slot, stack);
	}

	public static class LeagueContainer extends Container {

		private final Inventory leagueInventory;
		private final List<LeagueManager.Progress> playerProgress;

		public LeagueContainer(int windowId, PlayerInventory playerInventory, Inventory leagueInventory, List<LeagueManager.Progress> playerProgress) {
			super(GuiRegistry.LEAGUE_CONTAINER.get(), windowId);
			if (leagueInventory == null) {
				this.leagueInventory = new Inventory(NUM_SLOTS);
				this.playerProgress = Arrays.asList(LeagueManager.Progress.NONE, LeagueManager.Progress.NONE, LeagueManager.Progress.NONE, LeagueManager.Progress.NONE, LeagueManager.Progress.NONE);

				Random random = new Random();
				for (int slot : FILLER_SLOTS) {
					ItemStack fillerPane = random.nextBoolean() ? new ItemStack(Items.ORANGE_STAINED_GLASS_PANE) : new ItemStack(Items.YELLOW_STAINED_GLASS_PANE);
					fillerPane.setHoverName(new StringTextComponent(" "));
					this.leagueInventory.setItem(slot, fillerPane);
				}

				this.leagueInventory.setItem(11, new ItemStack(Items.GRAY_STAINED_GLASS_PANE));
				this.leagueInventory.setItem(12, new ItemStack(Items.PURPLE_STAINED_GLASS_PANE));
				this.leagueInventory.setItem(13, new ItemStack(Items.LIGHT_BLUE_STAINED_GLASS_PANE));
				this.leagueInventory.setItem(14, new ItemStack(Items.YELLOW_STAINED_GLASS_PANE));
				this.leagueInventory.setItem(15, new ItemStack(Items.BLUE_STAINED_GLASS_PANE));

			} else {
				this.leagueInventory = leagueInventory;
				this.playerProgress = playerProgress;

				updateLeagueMemberSlots();
			}

			for (int row = 0; row < 3; row++) {
				for (int col = 0; col < 9; col++) {
					int index = col + row * 9;
					int x = 8 + col * 18;
					int y = 18 + row * 18;

					if (FUNCTIONAL_SLOTS.contains(index)) {
						this.addSlot(new LeagueMemberSlot(this.leagueInventory, index, x, y, SLOT_TO_MEMBER.get(index)));
					} else {
						this.addSlot(new FillerSlot(this.leagueInventory, index, x, y));
					}
				}
			}
		}

		private void updateLeagueMemberSlots() {
			for (Map.Entry<Integer, LeagueManager.LeagueMember> entry : SLOT_TO_MEMBER.entrySet()) {
				int slotIndex = entry.getKey();
				LeagueManager.LeagueMember member = entry.getValue();

				ItemStack stack = this.leagueInventory.getItem(slotIndex);

				if (!stack.isEmpty()) {
					boolean isAccessible = canAccessMember(member, this.playerProgress);

					if (member == LeagueManager.LeagueMember.WALLACE) {
						boolean allEliteFourCompleted = true;
						for (int i = 0; i < 4; i++) {
							if (playerProgress.get(i) == LeagueManager.Progress.NONE) {
								allEliteFourCompleted = false;
								break;
							}
						}
						isAccessible = isAccessible && allEliteFourCompleted;
					}

					CompoundNBT display = stack.getOrCreateTagElement("display");
					ListNBT loreList = display.getList("Lore", 8);

					if (!isAccessible) {
						loreList.add(StringNBT.valueOf(ITextComponent.Serializer.toJson(new StringTextComponent("").withStyle(style -> style.withItalic(false)))));

						if (member == LeagueManager.LeagueMember.WALLACE) {
							loreList.add(
								StringNBT.valueOf(
									ITextComponent.Serializer.toJson(new StringTextComponent("You must defeat all Elite Four").withStyle(style -> style.withColor(TextFormatting.GRAY).withItalic(false)))
								)
							);
							loreList.add(
								StringNBT.valueOf(
									ITextComponent.Serializer.toJson(new StringTextComponent("members to challenge the Champion!").withStyle(style -> style.withColor(TextFormatting.GRAY).withItalic(false)))
								)
							);
						} else {
							loreList.add(
								StringNBT.valueOf(ITextComponent.Serializer.toJson(new StringTextComponent("You must defeat the previous").withStyle(style -> style.withColor(TextFormatting.GRAY).withItalic(false))))
							);
							loreList.add(
								StringNBT.valueOf(ITextComponent.Serializer.toJson(new StringTextComponent("Elite Four member first!").withStyle(style -> style.withColor(TextFormatting.GRAY).withItalic(false))))
							);
						}

						stack.getOrCreateTagElement("HideFlags").putInt("HideEnchantments", 0);
						stack.enchant(null, 0);
					}

					display.put("Lore", loreList);
				}
			}
		}

		@Override
		public boolean stillValid(PlayerEntity player) {
			return true;
		}

		@Override
		public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
			if (slotId >= 0 && slotId < this.slots.size()) {
				Slot slot = this.slots.get(slotId);

				if (slot instanceof LeagueMemberSlot && slot.hasItem()) {
					LeagueMemberSlot leagueMemberSlot = (LeagueMemberSlot) slot;
					LeagueManager.LeagueMember member = leagueMemberSlot.getLeagueMember();
					int memberIndex = member.ordinal();

					LeagueManager.Progress clientSideCurrentProgress = LeagueManager.Progress.NONE;
					if (this.playerProgress != null && memberIndex < this.playerProgress.size()) {
						clientSideCurrentProgress = this.playerProgress.get(memberIndex);
					}

					if (
						clientSideCurrentProgress == LeagueManager.Progress.SINGLES ||
						clientSideCurrentProgress == LeagueManager.Progress.DOUBLES ||
						(member == LeagueManager.LeagueMember.WALLACE && clientSideCurrentProgress == LeagueManager.Progress.CHAMPION)
					) {
						return ItemStack.EMPTY;
					}

					if (!canAccessMember(member, this.playerProgress)) {
						return ItemStack.EMPTY;
					}

					if (player instanceof ServerPlayerEntity) {
						ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
						LeagueManager leagueManager = LeagueManager.get((ServerWorld) player.level);
						List<LeagueManager.Progress> serverSidePlayerProgress = leagueManager.getPlayerProgress(player.getUUID());

						if (memberIndex >= serverSidePlayerProgress.size()) {
							return ItemStack.EMPTY;
						}
						LeagueManager.Progress serverSideCurrentMemberProgress = serverSidePlayerProgress.get(memberIndex);

						if (
							serverSideCurrentMemberProgress == LeagueManager.Progress.SINGLES ||
							serverSideCurrentMemberProgress == LeagueManager.Progress.DOUBLES ||
							(member == LeagueManager.LeagueMember.WALLACE && serverSideCurrentMemberProgress == LeagueManager.Progress.CHAMPION)
						) {
							return ItemStack.EMPTY;
						}

						if (!canAccessMember(member, serverSidePlayerProgress)) {
							return ItemStack.EMPTY;
						}

						if (member == LeagueManager.LeagueMember.WALLACE) {
							if (!leagueManager.isEligibleForChampion(player.getUUID())) {
								return ItemStack.EMPTY;
							}
						}
						boolean shouldCloseGui = this.handleLeagueMemberInteraction(serverPlayer, member, serverSideCurrentMemberProgress);

						if (shouldCloseGui) {
							serverPlayer.closeContainer();
						}
					}
					return ItemStack.EMPTY;
				}
			}

			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
			return ItemStack.EMPTY;
		}

		private boolean handleLeagueMemberInteraction(ServerPlayerEntity player, LeagueManager.LeagueMember member, LeagueManager.Progress progress) {
			String memberName = member.name().charAt(0) + member.name().substring(1).toLowerCase();
			LeagueManager leagueManager = LeagueManager.get((ServerWorld) player.level);

			List<LeagueManager.Progress> currentTotalPlayerProgress = leagueManager.getPlayerProgress(player.getUUID());
			int singlesCount = 0;
			int doublesCount = 0;
			for (LeagueManager.Progress p : currentTotalPlayerProgress) {
				if (p == LeagueManager.Progress.SINGLES) singlesCount++;
				if (p == LeagueManager.Progress.DOUBLES) doublesCount++;
			}

			if (member == LeagueManager.LeagueMember.WALLACE) {
				LeagueManager.initLeagueBattle(player, LeagueManager.LeagueMember.WALLACE, false);
				return true;
			} else {
				FormatSelectionGui.openGui(player, member, singlesCount, doublesCount, (isSingles, selectedMember) -> {
					LeagueManager.initLeagueBattle(player, selectedMember, !isSingles);
				});
				return false;
			}
		}

		private boolean canAccessMember(LeagueManager.LeagueMember member, List<LeagueManager.Progress> playerProgress) {
			int memberIndex = member.ordinal();

			if (memberIndex == 0) return true;

			for (int i = 0; i < memberIndex; i++) {
				LeagueManager.Progress progress = playerProgress.get(i);
				if (progress == LeagueManager.Progress.NONE) {
					return false;
				}
			}

			return true;
		}
	}

	public static class FillerSlot extends Slot {

		public FillerSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
			super(inventoryIn, index, xPosition, yPosition);
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return false;
		}

		@Override
		public boolean mayPickup(PlayerEntity playerIn) {
			return false;
		}
	}

	public static class LeagueMemberSlot extends Slot {

		private final LeagueManager.LeagueMember leagueMember;

		public LeagueMemberSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, LeagueManager.LeagueMember leagueMember) {
			super(inventoryIn, index, xPosition, yPosition);
			this.leagueMember = leagueMember;
		}

		public LeagueManager.LeagueMember getLeagueMember() {
			return leagueMember;
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return false;
		}
	}
}
