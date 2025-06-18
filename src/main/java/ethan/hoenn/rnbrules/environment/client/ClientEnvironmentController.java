package ethan.hoenn.rnbrules.environment.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import ethan.hoenn.rnbrules.environment.AuroraCaveEnvironment;
import ethan.hoenn.rnbrules.environment.AuroraVeilEnvironment;
import ethan.hoenn.rnbrules.environment.BaseEnvironment;
import ethan.hoenn.rnbrules.environment.HeatCaveEnvironment;
import ethan.hoenn.rnbrules.environment.MagmaStormEnvironment;
import ethan.hoenn.rnbrules.environment.RainEnvironment;
import ethan.hoenn.rnbrules.environment.SandstormEnvironment;
import ethan.hoenn.rnbrules.environment.TailwindEnvironment;
import ethan.hoenn.rnbrules.utils.enums.Environment;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEnvironmentController {

	private static final ClientEnvironmentController INSTANCE = new ClientEnvironmentController();
	private final Map<Environment, BaseEnvironment> environmentInstances;
	
	private static Environment currentClientEnvironment = Environment.NONE;
	private Environment previousClientEnvironment = Environment.NONE;
	private float transitionProgress = 0.0f;
	private boolean transitioning = false;
	private float currentIntensity = 1.0f;

	private ClientEnvironmentController() {
		environmentInstances = new HashMap<>();
		initEnvironments();
	}

	public static ClientEnvironmentController getInstance() {
		return INSTANCE;
	}

	private void initEnvironments() {
		environmentInstances.put(Environment.SANDSTORM, new SandstormEnvironment());
		environmentInstances.put(Environment.MAGMA_STORM, new MagmaStormEnvironment());
		environmentInstances.put(Environment.HEAT_CAVE, new HeatCaveEnvironment());
		environmentInstances.put(Environment.TAILWIND, new TailwindEnvironment());
		environmentInstances.put(Environment.AURORA_VEIL, new AuroraVeilEnvironment());
		environmentInstances.put(Environment.AURORA_CAVE, new AuroraCaveEnvironment());
		environmentInstances.put(Environment.RAIN, new RainEnvironment());
	}

	public BaseEnvironment getEnvironmentInstance(Environment type) {
		return environmentInstances.get(type);
	}

	public static Environment getCurrentClientEnvironment() {
		return currentClientEnvironment;
	}

	public void setClientEnvironment(Environment environment, float intensity) {
		if (environment != currentClientEnvironment) {
			previousClientEnvironment = currentClientEnvironment;
			currentClientEnvironment = environment;
			transitionProgress = 0.0f;
			transitioning = true;
			currentIntensity = intensity;
		} else {
			currentIntensity = intensity;
		}
	}

	public void update(float delta) {
		if (transitioning) {
			float transitionSpeed = 0.05f;

			if (currentClientEnvironment == Environment.SANDSTORM || previousClientEnvironment == Environment.SANDSTORM) {
				transitionSpeed = 0.02f;
			}

			transitionProgress += transitionSpeed;

			if (transitionProgress >= 1.0f) {
				transitionProgress = 1.0f;
				transitioning = false;
			}

			if (previousClientEnvironment != Environment.NONE) {
				BaseEnvironment prevEnv = environmentInstances.get(previousClientEnvironment);
				if (prevEnv != null) {
					prevEnv.onExit(1.0f - transitionProgress);
				}
			}

			if (currentClientEnvironment != Environment.NONE) {
				BaseEnvironment currEnv = environmentInstances.get(currentClientEnvironment);
				if (currEnv != null) {
					currEnv.onEnter(transitionProgress * currentIntensity);
				}
			}
		}

		if (currentClientEnvironment != Environment.NONE) {
			BaseEnvironment currEnv = environmentInstances.get(currentClientEnvironment);
			if (currEnv != null) {
				currEnv.update(delta);
			}
		}
	}

	public void render(float tickDelta) {
		if (transitioning) {
			if (previousClientEnvironment != Environment.NONE) {
				BaseEnvironment prevEnv = environmentInstances.get(previousClientEnvironment);
				if (prevEnv != null) {
					float exitIntensity = 1.0f - transitionProgress;

					if (previousClientEnvironment == Environment.SANDSTORM) {
						exitIntensity *= currentIntensity;
					}
					prevEnv.render(tickDelta, exitIntensity);
				}
			}

			if (currentClientEnvironment != Environment.NONE) {
				BaseEnvironment currEnv = environmentInstances.get(currentClientEnvironment);
				if (currEnv != null) {
					float entryIntensity = transitionProgress * currentIntensity;
					currEnv.render(tickDelta, entryIntensity);
				}
			}
		} else if (currentClientEnvironment != Environment.NONE) {
			BaseEnvironment currEnv = environmentInstances.get(currentClientEnvironment);
			if (currEnv != null) {
				currEnv.render(tickDelta, currentIntensity);
			}
		}
	}

	public static SandstormEnvironment getSandstormEnvironment() {
		return (SandstormEnvironment) INSTANCE.getEnvironmentInstance(Environment.SANDSTORM);
	}

	public static MagmaStormEnvironment getMagmaStormEnvironment() {
		return (MagmaStormEnvironment) INSTANCE.getEnvironmentInstance(Environment.MAGMA_STORM);
	}

	public static HeatCaveEnvironment getHeatCaveEnvironment() {
		return (HeatCaveEnvironment) INSTANCE.getEnvironmentInstance(Environment.HEAT_CAVE);
	}

	public static TailwindEnvironment getTailwindEnvironment() {
		return (TailwindEnvironment) INSTANCE.getEnvironmentInstance(Environment.TAILWIND);
	}

	public static AuroraVeilEnvironment getAuroraVeilEnvironment() {
		return (AuroraVeilEnvironment) INSTANCE.getEnvironmentInstance(Environment.AURORA_VEIL);
	}

	public static AuroraCaveEnvironment getAuroraCaveEnvironment() {
		return (AuroraCaveEnvironment) INSTANCE.getEnvironmentInstance(Environment.AURORA_CAVE);
	}

	public static RainEnvironment getRainEnvironment() {
		return (RainEnvironment) INSTANCE.getEnvironmentInstance(Environment.RAIN);
	}

	@SubscribeEvent
	public static void onFogDensity(EntityViewRenderEvent.FogDensity event) {
		float intensity = getIntensity();

		if (intensity > 0.01f) {
			float normalDensity = 0.01f;
			float sandstormDensity = 0.02f;

			float resultDensity = normalDensity + (sandstormDensity - normalDensity) * intensity;

			event.setDensity(resultDensity);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onFogColors(EntityViewRenderEvent.FogColors event) {
		float intensity = getIntensity();

		if (intensity > 0.01f) {
			float normalRed = event.getRed();
			float normalGreen = event.getGreen();
			float normalBlue = event.getBlue();

			float sandstormRed = 0.93f;
			float sandstormGreen = 0.85f;
			float sandstormBlue = 0.55f;

			event.setRed(normalRed + (sandstormRed - normalRed) * intensity);
			event.setGreen(normalGreen + (sandstormGreen - normalGreen) * intensity);
			event.setBlue(normalBlue + (sandstormBlue - normalBlue) * intensity);
		}
	}

	private static float getIntensity() {
		ClientEnvironmentController controller = INSTANCE;
		float intensity = 0f;

		if (currentClientEnvironment == Environment.SANDSTORM) {
			intensity = controller.transitioning ? controller.transitionProgress * controller.currentIntensity : controller.currentIntensity;
		} else if (controller.transitioning && controller.previousClientEnvironment == Environment.SANDSTORM) {
			intensity = (1.0f - controller.transitionProgress) * controller.currentIntensity;
		}
		return intensity;
	}

	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public static void onFogRender(EntityViewRenderEvent.RenderFogEvent event) {
		float intensity = getIntensity();

		if (intensity > 0.01f) {
			RenderSystem.fogMode(GlStateManager.FogMode.EXP2);

			float renderDistance = event.getRenderer().getRenderDistance();

			float normalFogStart = renderDistance * 0.25f;
			float normalFogEnd = renderDistance;

			float sandstormFogStart = 32f;
			float sandstormFogEnd = 48f;

			float fogStart = normalFogStart + (sandstormFogStart - normalFogStart) * intensity;
			float fogEnd = normalFogEnd + (sandstormFogEnd - normalFogEnd) * intensity;

			RenderSystem.fogStart(fogStart);
			RenderSystem.fogEnd(fogEnd);
			RenderSystem.setupNvFogDistance();
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			PlayerEntity player = Minecraft.getInstance().player;
			if (player != null && Minecraft.getInstance().level != null) {
				INSTANCE.update(1.0f);
			}
		}
	}

	@SubscribeEvent
	public static void onRenderTick(TickEvent.RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			PlayerEntity player = Minecraft.getInstance().player;
			if (player != null && Minecraft.getInstance().level != null) {
				INSTANCE.render(event.renderTickTime);
			}
		}
	}
}
