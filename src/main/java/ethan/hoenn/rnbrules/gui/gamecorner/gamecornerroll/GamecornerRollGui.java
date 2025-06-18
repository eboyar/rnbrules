package ethan.hoenn.rnbrules.gui.gamecorner.gamecornerroll;

import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.PokedexEvent;
import com.pixelmonmod.pixelmon.api.events.PokemonReceivedEvent;
import com.pixelmonmod.pixelmon.api.pokedex.PokedexRegistrationStatus;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import ethan.hoenn.rnbrules.RNBConfig;
import ethan.hoenn.rnbrules.gui.gamecorner.GamecornerAssets;
import ethan.hoenn.rnbrules.registries.GuiRegistry;
import ethan.hoenn.rnbrules.utils.managers.GlobalOTManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("NullableProblems")
public class GamecornerRollGui {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final String GAMECORNER_OT = "gamecorner";

	public static void openGui(ServerPlayerEntity player, List<Pokemon> pokemonPool, Pokemon winningPokemon, String levelSpec) {
		NetworkHooks.openGui(
			player,
			new INamedContainerProvider() {
				@Override
				public ITextComponent getDisplayName() {
					return new StringTextComponent("Rolling your Prize...").withStyle(TextFormatting.WHITE);
				}

				@Nullable
				@Override
				public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
					return new GamecornerRollContainer(windowId, inventory, pokemonPool, winningPokemon, levelSpec);
				}
			},
			buf -> {
				buf.writeInt(pokemonPool.size());
				for (Pokemon pokemon : pokemonPool) {
					CompoundNBT nbt = new CompoundNBT();
					pokemon.writeToNBT(nbt);
					buf.writeNbt(nbt);
				}
				CompoundNBT winnerNbt = new CompoundNBT();
				winningPokemon.writeToNBT(winnerNbt);
				buf.writeNbt(winnerNbt);
				buf.writeUtf(levelSpec);
			}
		);
	}

	public static class GamecornerRollContainer extends Container {

		private static final EmptyInventory EMPTY_INVENTORY = new EmptyInventory(27);

		private static final Set<Integer> TOP_ROW_SLOTS = IntStream.rangeClosed(0, 8).boxed().collect(Collectors.toSet());
		private static final Set<Integer> BOTTOM_ROW_SLOTS = IntStream.rangeClosed(18, 26).boxed().collect(Collectors.toSet());

		private final List<Pokemon> pokemonPool;
		private final Pokemon winningPokemon;
		private final List<ItemStack> pokemonPhotos;
		private final int winnerIndex;
		private final String levelSpec;
		private boolean pokemonGiven = false;
		private boolean winMessageSent = false;

		public GamecornerRollContainer(int windowId, PlayerInventory playerInventory, List<Pokemon> pokemonPool, Pokemon winningPokemon, String levelSpec) {
			super(GuiRegistry.GAMECORNER_ROLL_CONTAINER.get(), windowId);
			this.pokemonPool = Collections.unmodifiableList(new ArrayList<>(pokemonPool));
			this.winningPokemon = winningPokemon;
			this.pokemonPhotos = pokemonPool.stream().map(GamecornerAssets::getPokemonPhoto).collect(Collectors.toList());
			this.winnerIndex = findWinnerIndex(pokemonPool, winningPokemon);
			this.levelSpec = levelSpec;

			ItemStack lightPane = new ItemStack(Items.WHITE_STAINED_GLASS_PANE).setHoverName(new StringTextComponent(" "));
			ItemStack darkPane = new ItemStack(Items.LIGHT_GRAY_STAINED_GLASS_PANE).setHoverName(new StringTextComponent(" "));

			for (int row = 0; row < 3; ++row) {
				for (int col = 0; col < 9; ++col) {
					int slotIndex = col + row * 9;
					int x = 8 + col * 18;
					int y = 18 + row * 18;

					if (TOP_ROW_SLOTS.contains(slotIndex) || BOTTOM_ROW_SLOTS.contains(slotIndex)) {
						ItemStack pane = (col % 2 == 0) ? lightPane.copy() : darkPane.copy();
						this.addSlot(new FillerSlot(EMPTY_INVENTORY, slotIndex, x, y, pane));
					}
				}
			}
		}

		private int findWinnerIndex(List<Pokemon> pool, Pokemon winner) {
			for (int i = 0; i < pool.size(); i++) {
				Pokemon current = pool.get(i);
				if (current.getSpecies().equals(winner.getSpecies()) && current.getForm().getName().equalsIgnoreCase(winner.getForm().getName())) {
					return i;
				}
			}
			LOGGER.error(
				"Could not find winning Pokemon index! Winner: {}/{}, Pool: {}",
				winner.getSpecies().getName(),
				winner.getForm().getName(),
				pool.stream().map(p -> p.getSpecies().getName() + "/" + p.getForm().getName()).collect(Collectors.joining(", "))
			);
			return 0;
		}

		public GamecornerRollContainer(int windowId, PlayerInventory playerInventory, PacketBuffer data) {
			this(windowId, playerInventory, readPokemonPool(data), readWinningPokemon(data), data.readUtf());
		}

		private static List<Pokemon> readPokemonPool(PacketBuffer buffer) {
			int size = buffer.readInt();
			List<Pokemon> pool = new ArrayList<>(size);
			for (int i = 0; i < size; i++) {
				CompoundNBT nbt = buffer.readNbt();
				if (nbt != null) {
					Pokemon pokemon = PokemonFactory.create(nbt);
					if (pokemon != null) {
						pool.add(pokemon);
					} else {
						LOGGER.error("Error creating Pokemon from NBT at index {}", i);
					}
				} else {
					LOGGER.error("Error reading Pokemon NBT from buffer at index {}", i);
				}
			}
			return pool;
		}

		private static Pokemon readWinningPokemon(PacketBuffer buffer) {
			CompoundNBT nbt = buffer.readNbt();
			if (nbt != null) {
				Pokemon pokemon = PokemonFactory.create(nbt);
				if (pokemon != null) {
					return pokemon;
				} else {
					LOGGER.error("Error creating winning Pokemon from NBT.");
					return PokemonFactory.create(PixelmonSpecies.MISSINGNO.getValueUnsafe());
				}
			} else {
				LOGGER.error("Error reading winning Pokemon NBT from buffer.");
				return PokemonFactory.create(PixelmonSpecies.MISSINGNO.getValueUnsafe());
			}
		}

		public List<Pokemon> getPokemonPool() {
			return pokemonPool;
		}

		public Pokemon getWinningPokemon() {
			return winningPokemon;
		}

		public List<ItemStack> getPokemonPhotos() {
			return pokemonPhotos;
		}

		public int getWinnerIndex() {
			return winnerIndex;
		}

		public String getLevelSpec() {
			return levelSpec;
		}

		public void setWinMessageSent() {
			this.winMessageSent = true;
		}

		@Override
		public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
			if (index >= 27) {
				return super.quickMoveStack(playerIn, index);
			}
			return ItemStack.EMPTY;
		}

		@Override
		public boolean canTakeItemForPickAll(ItemStack stack, Slot slotIn) {
			return !(slotIn instanceof FillerSlot);
		}

		@Override
		public boolean canDragTo(Slot slotIn) {
			return !(slotIn instanceof FillerSlot);
		}

		@Override
		public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
			if (slotId >= 0 && slotId < this.slots.size()) {
				Slot slot = this.slots.get(slotId);
				if (slot instanceof FillerSlot) {
					return ItemStack.EMPTY;
				}
			}
			if (slotId >= 27) {
				return super.clicked(slotId, dragType, clickTypeIn, player);
			}
			if (player.inventory != null) {
				player.inventory.setCarried(ItemStack.EMPTY);
			}
			return ItemStack.EMPTY;
		}

		@Override
		public boolean stillValid(PlayerEntity playerIn) {
			return true;
		}

		@Override
		public void removed(PlayerEntity playerIn) {
			super.removed(playerIn);

			if (!playerIn.level.isClientSide && !this.pokemonGiven && playerIn instanceof ServerPlayerEntity && this.winningPokemon != null && this.levelSpec != null && !this.levelSpec.isEmpty()) {
				this.pokemonGiven = true;
				ServerPlayerEntity serverPlayer = (ServerPlayerEntity) playerIn;

				try {
					givePokemonReward(serverPlayer);
				} catch (Exception e) {
					LOGGER.error("Error giving Game Corner Pokemon to {}: {}", serverPlayer.getName().getString(), e.getMessage(), e);
					serverPlayer.sendMessage(new StringTextComponent("An error occurred giving your prize.").withStyle(TextFormatting.RED), serverPlayer.getUUID());
				}
			}
		}

		private void givePokemonReward(ServerPlayerEntity serverPlayer) {
			Species species = this.winningPokemon.getSpecies();
			String speciesName = species.getName();
			String formName = this.winningPokemon.getForm().getName();
			String formSpec = "";
			if (formName != null && !formName.isEmpty() && !formName.equalsIgnoreCase("none") && !formName.equalsIgnoreCase("base")) {
				formSpec = " form:" + formName.toLowerCase();
			}

			String haSpec = "";
			if (species.equals(PixelmonSpecies.SLOWBRO.getValueUnsafe()) || species.equals(PixelmonSpecies.SLOWKING.getValueUnsafe())) {
				haSpec = " ha:true";
			}

			String finalSpecString = speciesName + formSpec + " " + this.levelSpec + " numivs:3 cl:gamecorner" + haSpec;

			PokemonSpecification finalSpec = PokemonSpecificationProxy.create(finalSpecString.split(" "));
			Pokemon finalPokemon = finalSpec.create();

			if (finalPokemon == null) {
				LOGGER.error("Error: Failed to create final Pokemon from spec: {}", finalSpecString);
				serverPlayer.sendMessage(new StringTextComponent("An error occurred creating your prize.").withStyle(TextFormatting.RED), serverPlayer.getUUID());
				return;
			}

			PlayerPartyStorage pps = StorageProxy.getParty(serverPlayer.getUUID());

			if (Pixelmon.EVENT_BUS.post(new PokemonReceivedEvent(serverPlayer, finalPokemon, "GameCorner"))) {
				return;
			}

			boolean addedToParty = false;
			if (BattleRegistry.getBattle(serverPlayer) == null) {
				addedToParty = pps.add(finalPokemon);
			}

			if (!addedToParty) {
				StorageProxy.getPCForPlayer(serverPlayer.getUUID()).add(finalPokemon);
			}

			if (!finalPokemon.isEgg()) {
				PokedexEvent.Pre preEvent = new PokedexEvent.Pre(pps.uuid, finalPokemon, PokedexRegistrationStatus.CAUGHT, "gameCornerReward");
				if (!Pixelmon.EVENT_BUS.post(preEvent)) {
					pps.playerPokedex.set(preEvent.getPokemon(), preEvent.getNewStatus());
					pps.playerPokedex.update();
					Pixelmon.EVENT_BUS.post(new PokedexEvent.Post(serverPlayer.getUUID(), preEvent.getOldStatus(), preEvent.getPokemon(), preEvent.getNewStatus(), preEvent.getCause()));
				}
			}

			GlobalOTManager otmanager = GlobalOTManager.get(serverPlayer.getLevel());
			otmanager.addGlobalOT(GAMECORNER_OT);
			otmanager.addPlayerGlobalOT(serverPlayer.getUUID(), GAMECORNER_OT);

			String completionCommand = RNBConfig.getGamecornerCompletionCommand();
			if (completionCommand != null && !completionCommand.isEmpty()) {
				String command = completionCommand.replace("@pl", serverPlayer.getScoreboardName());
				serverPlayer.getServer().getCommands().performCommand(serverPlayer.getServer().createCommandSourceStack().withPermission(4), command);
			}
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
			return size;
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
