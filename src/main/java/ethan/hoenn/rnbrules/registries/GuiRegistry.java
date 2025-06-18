package ethan.hoenn.rnbrules.registries;

import ethan.hoenn.rnbrules.gui.backpack.BigBackpackContainer;
import ethan.hoenn.rnbrules.gui.backpack.BigBackpackScreen;
import ethan.hoenn.rnbrules.gui.ferry.FerryGui;
import ethan.hoenn.rnbrules.gui.ferry.FerryScreen;
import ethan.hoenn.rnbrules.gui.flight.FlyGui;
import ethan.hoenn.rnbrules.gui.flight.FlyScreen;
import ethan.hoenn.rnbrules.gui.fossils.FossilGui;
import ethan.hoenn.rnbrules.gui.fossils.FossilScreen;
import ethan.hoenn.rnbrules.gui.fossils.underpass.UnderpassGui;
import ethan.hoenn.rnbrules.gui.fossils.underpass.UnderpassScreen;
import ethan.hoenn.rnbrules.gui.gamecorner.GamecornerGui;
import ethan.hoenn.rnbrules.gui.gamecorner.GamecornerScreen;
import ethan.hoenn.rnbrules.gui.gamecorner.confirmation.YNConfirmationGui;
import ethan.hoenn.rnbrules.gui.gamecorner.confirmation.YNConfirmationScreen;
import ethan.hoenn.rnbrules.gui.gamecorner.gamecornerroll.GamecornerRollGui;
import ethan.hoenn.rnbrules.gui.gamecorner.gamecornerroll.GamecornerRollScreen;
import ethan.hoenn.rnbrules.gui.heartscale.HeartscaleExchangeGui;
import ethan.hoenn.rnbrules.gui.heartscale.HeartscaleExchangeScreen;
import ethan.hoenn.rnbrules.gui.heartscale.natures.NaturesGui;
import ethan.hoenn.rnbrules.gui.heartscale.natures.NaturesScreen;
import ethan.hoenn.rnbrules.gui.intriguingstone.IntriguingStoneExchangeGui;
import ethan.hoenn.rnbrules.gui.intriguingstone.IntriguingStoneExchangeScreen;
import ethan.hoenn.rnbrules.gui.itemupgrade.ItemUpgradeGui;
import ethan.hoenn.rnbrules.gui.itemupgrade.ItemUpgradeScreen;
import ethan.hoenn.rnbrules.gui.league.LeagueGui;
import ethan.hoenn.rnbrules.gui.league.LeagueScreen;
import ethan.hoenn.rnbrules.gui.league.formatselection.FormatSelectionGui;
import ethan.hoenn.rnbrules.gui.league.formatselection.FormatSelectionScreen;
import ethan.hoenn.rnbrules.utils.enums.FerryDestination;
import ethan.hoenn.rnbrules.utils.enums.FerryRoute;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class GuiRegistry {

	private static final String MOD_ID = "rnbrules";

	public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MOD_ID);

	public static final RegistryObject<ContainerType<FlyGui.FlyContainer>> FLY_CONTAINER = CONTAINERS.register("fly_container", () ->
		IForgeContainerType.create((windowId, inv, data) -> new FlyGui.FlyContainer(windowId, inv))
	);

	public static final RegistryObject<ContainerType<FerryGui.FerryContainer>> FERRY_CONTAINER = CONTAINERS.register("ferry_container", () ->
		IForgeContainerType.create((windowId, inv, data) -> {
			String routeStr = data.readUtf();
			FerryRoute route;

			try {
				route = FerryRoute.valueOf(routeStr);
			} catch (IllegalArgumentException e) {
				route = FerryRoute.WESTERN_HOENN;
			}

			FerryDestination currentLocation = null;
			if (data.readBoolean()) {
				String locationStr = data.readUtf();
				try {
					currentLocation = FerryDestination.valueOf(locationStr);
				} catch (IllegalArgumentException e) {}
			}

			return new FerryGui.FerryContainer(windowId, inv, route, currentLocation);
		})
	);

	public static final RegistryObject<ContainerType<BigBackpackContainer>> BACKPACK_CONTAINER = CONTAINERS.register("backpack_container", () -> IForgeContainerType.create(BigBackpackContainer::new));

	public static final RegistryObject<ContainerType<NaturesGui.NaturesContainer>> NATURES_CONTAINER = CONTAINERS.register("natures_container", () ->
		IForgeContainerType.create((windowId, inv, data) -> new NaturesGui.NaturesContainer(windowId, inv))
	);

	public static final RegistryObject<ContainerType<HeartscaleExchangeGui.HeartscaleExchangeContainer>> HEARTSCALE_EXCHANGE_CONTAINER = CONTAINERS.register("heartscale_exchange_container", () ->
		IForgeContainerType.create((windowId, inv, data) -> new HeartscaleExchangeGui.HeartscaleExchangeContainer(windowId, inv))
	);

	public static final RegistryObject<ContainerType<ItemUpgradeGui.ItemUpgradeContainer>> ITEM_UPGRADE_CONTAINER = CONTAINERS.register("item_upgrade_container", () ->
		IForgeContainerType.create((windowId, inv, data) -> new ItemUpgradeGui.ItemUpgradeContainer(windowId, inv))
	);

	public static final RegistryObject<ContainerType<FossilGui.FossilContainer>> FOSSIL_CONTAINER = CONTAINERS.register("fossil_container", () ->
		IForgeContainerType.create((windowId, inv, data) -> new FossilGui.FossilContainer(windowId, inv))
	);

	public static final RegistryObject<ContainerType<GamecornerGui.GamecornerContainer>> GAMECORNER_CONTAINER = CONTAINERS.register("gamecorner_container", () ->
		IForgeContainerType.create((windowId, inv, data) -> new GamecornerGui.GamecornerContainer(windowId, inv))
	);

	public static final RegistryObject<ContainerType<YNConfirmationGui.YNConfirmationContainer>> YN_CONFIRMATION_CONTAINER = CONTAINERS.register("yn_confirmation_container", () ->
		IForgeContainerType.create(YNConfirmationGui.YNConfirmationContainer::new)
	);

	public static final RegistryObject<ContainerType<GamecornerRollGui.GamecornerRollContainer>> GAMECORNER_ROLL_CONTAINER = CONTAINERS.register("gamecorner_roll_container", () ->
		IForgeContainerType.create(GamecornerRollGui.GamecornerRollContainer::new)
	);

	public static final RegistryObject<ContainerType<LeagueGui.LeagueContainer>> LEAGUE_CONTAINER = CONTAINERS.register("league_container", () ->
		IForgeContainerType.create((windowId, inv, data) -> {
			return new LeagueGui.LeagueContainer(windowId, inv, null, null);
		})
	);

	public static final RegistryObject<ContainerType<FormatSelectionGui.FormatSelectionContainer>> FORMAT_SELECTION_CONTAINER = CONTAINERS.register("format_selection_container", () ->
		IForgeContainerType.create(FormatSelectionGui.FormatSelectionContainer::new)
	);

	public static final RegistryObject<ContainerType<IntriguingStoneExchangeGui.IntriguingStoneExchangeContainer>> INTRIGUING_STONE_EXCHANGE_CONTAINER = CONTAINERS.register(
		"intriguing_stone_exchange_container",
		() -> IForgeContainerType.create((windowId, inv, data) -> new IntriguingStoneExchangeGui.IntriguingStoneExchangeContainer(windowId, inv))
	);

	public static final RegistryObject<ContainerType<UnderpassGui.UnderpassContainer>> UNDERPASS_CONTAINER = CONTAINERS.register(
		"underpass_container",
		() -> IForgeContainerType.create((windowId, inv, data) -> new UnderpassGui.UnderpassContainer(windowId, inv))
	);

	public static final ResourceLocation SELECT_27 = new ResourceLocation(MOD_ID, "textures/gui/select_27.png");
	public static final ResourceLocation SELECT_27_INVENTORY = new ResourceLocation(MOD_ID, "textures/gui/select_27_inventory.png");
	public static final ResourceLocation GRAY_SELECT_27 = new ResourceLocation(MOD_ID, "textures/gui/gray_select_27.png");
	public static final ResourceLocation GRAY_SELECT_11 = new ResourceLocation(MOD_ID, "textures/gui/gray_select_11.png");
	public static final ResourceLocation BIG_BACKPACK = new ResourceLocation(MOD_ID, "textures/gui/184_backpack.png");
	public static final ResourceLocation SELECT_36 = new ResourceLocation(MOD_ID, "textures/gui/select_36.png");
	public static final ResourceLocation GRAY_SELECT_36 = new ResourceLocation(MOD_ID, "textures/gui/gray_select_36.png");

	public static void register(IEventBus eventBus) {
		CONTAINERS.register(eventBus);
	}

	public static void registerScreens(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			ScreenManager.register(FLY_CONTAINER.get(), FlyScreen::new);

			ScreenManager.register(FERRY_CONTAINER.get(), FerryScreen::new);

			ScreenManager.register(BACKPACK_CONTAINER.get(), BigBackpackScreen::new);

			ScreenManager.register(NATURES_CONTAINER.get(), NaturesScreen::new);

			ScreenManager.register(HEARTSCALE_EXCHANGE_CONTAINER.get(), HeartscaleExchangeScreen::new);

			ScreenManager.register(ITEM_UPGRADE_CONTAINER.get(), ItemUpgradeScreen::new);

			ScreenManager.register(FOSSIL_CONTAINER.get(), FossilScreen::new);

			ScreenManager.register(GAMECORNER_CONTAINER.get(), GamecornerScreen::new);

			ScreenManager.register(YN_CONFIRMATION_CONTAINER.get(), YNConfirmationScreen::new);

			ScreenManager.register(GAMECORNER_ROLL_CONTAINER.get(), GamecornerRollScreen::new);

			ScreenManager.register(LEAGUE_CONTAINER.get(), LeagueScreen::new);

			ScreenManager.register(FORMAT_SELECTION_CONTAINER.get(), FormatSelectionScreen::new);

			ScreenManager.register(INTRIGUING_STONE_EXCHANGE_CONTAINER.get(), IntriguingStoneExchangeScreen::new);

			ScreenManager.register(UNDERPASS_CONTAINER.get(), UnderpassScreen::new);
		});
	}
}
