package ethan.hoenn.rnbrules.registries;

import ethan.hoenn.rnbrules.environment.particles.AuroraVeilParticle;
import ethan.hoenn.rnbrules.environment.particles.MagmaStormParticle;
import ethan.hoenn.rnbrules.environment.particles.SandstormParticle;
import ethan.hoenn.rnbrules.environment.particles.WindParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "rnbrules", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ParticleRegistry {

	private static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, "rnbrules");

	public static final RegistryObject<BasicParticleType> SANDSTORM_LIGHT = PARTICLES.register("sandstorm_light", () -> new BasicParticleType(false));
	public static final RegistryObject<BasicParticleType> SANDSTORM_MEDIUM = PARTICLES.register("sandstorm_medium", () -> new BasicParticleType(false));
	public static final RegistryObject<BasicParticleType> SANDSTORM_DARK = PARTICLES.register("sandstorm_dark", () -> new BasicParticleType(false));
	public static final RegistryObject<BasicParticleType> MAGMA_HARDENED = PARTICLES.register("magma_hardened", () -> new BasicParticleType(false));
	public static final RegistryObject<BasicParticleType> MAGMA_SUPERCOOL = PARTICLES.register("magma_supercool", () -> new BasicParticleType(false));
	public static final RegistryObject<BasicParticleType> MAGMA_COOL = PARTICLES.register("magma_cool", () -> new BasicParticleType(false));
	public static final RegistryObject<BasicParticleType> MAGMA_HOT = PARTICLES.register("magma_hot", () -> new BasicParticleType(false));
	public static final RegistryObject<BasicParticleType> MAGMA_SUPERHOT = PARTICLES.register("magma_superhot", () -> new BasicParticleType(false));
	public static final RegistryObject<BasicParticleType> WIND_NORMAL = PARTICLES.register("wind_normal", () -> new BasicParticleType(false));
	public static final RegistryObject<BasicParticleType> WIND_NORMAL_ANIM = PARTICLES.register("wind_normal_anim", () -> new BasicParticleType(false));
	public static final RegistryObject<BasicParticleType> WIND_MYSTIC = PARTICLES.register("wind_mystic", () -> new BasicParticleType(false));
	public static final RegistryObject<BasicParticleType> WIND_MYSTIC_ANIM = PARTICLES.register("wind_mystic_anim", () -> new BasicParticleType(false));
	public static final RegistryObject<BasicParticleType> FROST_ROCK = PARTICLES.register("frost_rock", () -> new BasicParticleType(false));
	public static final RegistryObject<BasicParticleType> FROST_ROCK_LIGHT = PARTICLES.register("frost_rock_light", () -> new BasicParticleType(false));
	public static final RegistryObject<BasicParticleType> SNOW_STAR = PARTICLES.register("snow_star", () -> new BasicParticleType(false));
	public static final RegistryObject<BasicParticleType> GLOWSTONE_LIGHT = PARTICLES.register("glowstone_light", () -> new BasicParticleType(false));
	public static final RegistryObject<BasicParticleType> FROST_LIGHT = PARTICLES.register("frost_light", () -> new BasicParticleType(false));
	public static final RegistryObject<BasicParticleType> FROST_STAR = PARTICLES.register("frost_star", () -> new BasicParticleType(false));

	public static void init() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		PARTICLES.register(modEventBus);
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void registerFactories(ParticleFactoryRegisterEvent event) {
		Minecraft.getInstance().particleEngine.register(SANDSTORM_LIGHT.get(), sprite -> new SandstormParticle.Factory(sprite, false, false));
		Minecraft.getInstance().particleEngine.register(SANDSTORM_MEDIUM.get(), sprite -> new SandstormParticle.Factory(sprite, true, false));
		Minecraft.getInstance().particleEngine.register(SANDSTORM_DARK.get(), sprite -> new SandstormParticle.Factory(sprite, true, true));
		Minecraft.getInstance().particleEngine.register(MAGMA_HARDENED.get(), sprite -> new MagmaStormParticle.Factory(sprite, 0));
		Minecraft.getInstance().particleEngine.register(MAGMA_SUPERCOOL.get(), sprite -> new MagmaStormParticle.Factory(sprite, 1));
		Minecraft.getInstance().particleEngine.register(MAGMA_COOL.get(), sprite -> new MagmaStormParticle.Factory(sprite, 2));
		Minecraft.getInstance().particleEngine.register(MAGMA_HOT.get(), sprite -> new MagmaStormParticle.Factory(sprite, 3));
		Minecraft.getInstance().particleEngine.register(MAGMA_SUPERHOT.get(), sprite -> new MagmaStormParticle.Factory(sprite, 4));
		Minecraft.getInstance().particleEngine.register(WIND_NORMAL.get(), sprite -> new WindParticle.Factory(sprite, false, false));
		Minecraft.getInstance().particleEngine.register(WIND_NORMAL_ANIM.get(), sprite -> new WindParticle.Factory(sprite, true, false));
		Minecraft.getInstance().particleEngine.register(WIND_MYSTIC.get(), sprite -> new WindParticle.Factory(sprite, false, true));
		Minecraft.getInstance().particleEngine.register(WIND_MYSTIC_ANIM.get(), sprite -> new WindParticle.Factory(sprite, true, true));
		Minecraft.getInstance().particleEngine.register(FROST_ROCK.get(), sprite -> new AuroraVeilParticle.Factory(sprite, 0, false, false));
		Minecraft.getInstance().particleEngine.register(FROST_ROCK_LIGHT.get(), sprite -> new AuroraVeilParticle.Factory(sprite, 1, true, false));
		Minecraft.getInstance().particleEngine.register(SNOW_STAR.get(), sprite -> new AuroraVeilParticle.Factory(sprite, 2, false, false));
		Minecraft.getInstance().particleEngine.register(GLOWSTONE_LIGHT.get(), sprite -> new AuroraVeilParticle.Factory(sprite, 3, true, false));
		Minecraft.getInstance().particleEngine.register(FROST_LIGHT.get(), sprite -> new AuroraVeilParticle.Factory(sprite, 4, true, false));
		Minecraft.getInstance().particleEngine.register(FROST_STAR.get(), sprite -> new AuroraVeilParticle.Factory(sprite, 5, false, false));
	}
}
