package ethan.hoenn.rnbrules;

import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import ethan.hoenn.rnbrules.integration.ModIntegration;
import ethan.hoenn.rnbrules.interactions.*;
import ethan.hoenn.rnbrules.listeners.*;
import ethan.hoenn.rnbrules.network.PacketHandler;
import ethan.hoenn.rnbrules.registries.*;
import ethan.hoenn.rnbrules.spec.*;
import ethan.hoenn.rnbrules.utils.misc.Gamerules;
import ethan.hoenn.rnbrules.utils.misc.LogFilter;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("rnbrules")
public class RNBRules {

	public RNBRules() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

		BlockRegistry.register(eventBus);
		ItemRegistry.register(eventBus);
		TileEntityRegistry.register(eventBus);
		GuiRegistry.register(eventBus);
		ParticleRegistry.init();

		LogFilter.init();

		eventBus.addListener(this::setup);
		eventBus.addListener(this::enqueueIMC);
		eventBus.addListener(this::processIMC);
		eventBus.addListener(this::doClientStuff);
	}

	private void setup(final FMLCommonSetupEvent event) {
		RNBConfig.loadConfig();
		PacketHandler.register();

		PixelmonEntity.interactionList.removeIf(i -> i instanceof com.pixelmonmod.pixelmon.entities.pixelmon.interactions.InteractionAbilityPatch);
		PixelmonEntity.interactionList.removeIf(i -> i instanceof com.pixelmonmod.pixelmon.entities.pixelmon.interactions.InteractionRareCandy);
		PixelmonEntity.interactionList.removeIf(i -> i instanceof com.pixelmonmod.pixelmon.entities.pixelmon.interactions.InteractionPotion);
		PixelmonEntity.interactionList.removeIf(i -> i instanceof com.pixelmonmod.pixelmon.entities.pixelmon.interactions.InteractionElixir);
		PixelmonEntity.interactionList.removeIf(i -> i instanceof com.pixelmonmod.pixelmon.entities.pixelmon.interactions.InteractionEther);
		PixelmonEntity.interactionList.removeIf(i -> i instanceof com.pixelmonmod.pixelmon.entities.pixelmon.interactions.InteractionTM);
		PixelmonEntity.interactionList.removeIf(i -> i instanceof com.pixelmonmod.pixelmon.entities.pixelmon.interactions.InteractionTechnicalMove);
		PixelmonEntity.interactionList.removeIf(i -> i instanceof com.pixelmonmod.pixelmon.entities.pixelmon.interactions.InteractionBottleCap);

		PixelmonEntity.interactionList.add(new InteractionCustomAbilityPatch());
		PixelmonEntity.interactionList.add(new InteractionCustomTM());
		PixelmonEntity.interactionList.add(new InteractionCustomTechnicalMove());
		PixelmonEntity.interactionList.add(new InteractionCustomElixir());
		PixelmonEntity.interactionList.add(new InteractionCustomEther());
		PixelmonEntity.interactionList.add(new InteractionCustomPotion());
		PixelmonEntity.interactionList.add(new InteractionCappedRareCandy());
		PixelmonEntity.interactionList.add(new InteractionEndlessCandy());
		PixelmonEntity.interactionList.add(new InteractionCustomBottleCap());

		Pixelmon.EVENT_BUS.register(new BattleDependencyListener());
		Pixelmon.EVENT_BUS.register(new BattleInfoListener());
		Pixelmon.EVENT_BUS.register(new MultiBattleListener());
		Pixelmon.EVENT_BUS.register(new PokeBallImpactListener());
		Pixelmon.EVENT_BUS.register(new EnvironmentListener());
		Pixelmon.EVENT_BUS.register(new CatchListener());
		Pixelmon.EVENT_BUS.register(new ShopListener());
		Pixelmon.EVENT_BUS.register(new GauntletListeners());
		Pixelmon.EVENT_BUS.register(new DeathlessListener());
		Pixelmon.EVENT_BUS.register(new FerryListener());
		Pixelmon.EVENT_BUS.register(new OneTimeRewardListener());
		Pixelmon.EVENT_BUS.register(new NPCInteractionListener());
		Pixelmon.EVENT_BUS.register(new MoveTutorListener());
		Pixelmon.EVENT_BUS.register(new DialogueNPCListener());
		Pixelmon.EVENT_BUS.register(new NPCTradeListener());
		Pixelmon.EVENT_BUS.register(new PlayerTradeListener());
		Pixelmon.EVENT_BUS.register(new PokeLootListener());
		Pixelmon.EVENT_BUS.register(new RoamerFleeListener());
		Pixelmon.EVENT_BUS.register(new LeagueListener());
		Pixelmon.EVENT_BUS.register(new SpawningListener());

		PokemonSpecificationProxy.register(new SpawnPointRequirement());
		PokemonSpecificationProxy.register(new NeedsSurfRequirement());
		PokemonSpecificationProxy.register(new NeedsRockSmashRequirement());
		PokemonSpecificationProxy.register(new CatchLocationRequirement());
		PokemonSpecificationProxy.register(new NumIVsRequirement());

		new Gamerules();
		ModIntegration.registerFTBQuestsIntegration();
	}

	private void doClientStuff(final FMLClientSetupEvent event) {
		ClientSetup.init(event);
	}

	private void enqueueIMC(final InterModEnqueueEvent event) {}

	private void processIMC(final InterModProcessEvent event) {}
}
