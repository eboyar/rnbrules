/**
 * Ported over from PixelTweaks by Strangeone101
 * https://github.com/StrangeOne101/PixelTweaks
 */

package ethan.hoenn.rnbrules.music.event;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import ethan.hoenn.rnbrules.music.Sound;
import ethan.hoenn.rnbrules.music.condition.Condition;
import ethan.hoenn.rnbrules.utils.misc.SpecificTime;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;

public class EventRegistry implements ISelectiveResourceReloadListener {

	private static Map<Class<? extends Event>, Set<Event>> EVENTS = Maps.newHashMap();
	private static final Random RANDOM = new Random();

	protected static void registerEvent(Event event) {
		EVENTS.computeIfAbsent(event.getClass(), k -> newTreeset()).add(event);

		if (event instanceof EventListener) {
			((EventListener) event).onRegister();
		}
	}

	public static TreeSet<Event> newTreeset() {
		return Sets.newTreeSet((e1, e2) -> {
			if (e1.getPriority() == e2.getPriority()) {
				return Integer.compare(System.identityHashCode(e1), System.identityHashCode(e2));
			}
			return e2.getPriority() - e1.getPriority();
		});
	}

	public static <T extends Event> Collection<T> getEvents(Class<T> eventClass) {
		return (Collection<T>) EVENTS.getOrDefault(eventClass, Sets.newHashSet());
	}

	public static <T extends Event> T getRandomEvent(Class<T> eventClass, Collection<T> events) {
		if (events.isEmpty()) return null;

		int highestPriority = events.stream().mapToInt(Event::getPriority).max().orElse(0);

		List<T> highestPriorityEvents = new ArrayList<>();
		for (T event : events) {
			if (event.getPriority() == highestPriority) {
				highestPriorityEvents.add(event);
			}
		}

		return highestPriorityEvents.get(RANDOM.nextInt(highestPriorityEvents.size()));
	}

	public EventRegistry() {
		System.out.println("EventRegistry constructor called!");
		IResourceManager manager = Minecraft.getInstance().getResourceManager();
		if (manager instanceof IReloadableResourceManager) {
			System.out.println("Registering reload listener...");
			((IReloadableResourceManager) manager).registerReloadListener(this);
			try {
				onResourceManagerReload(manager);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("ERROR: Resource manager is not reloadable!");
		}
	}

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		String directoryPath = "rnbevents";
		System.out.println("Trying to load rnbevents...");
		EVENTS.clear();

		Collection<ResourceLocation> resourceLocations = resourceManager.listResources(directoryPath, fileName -> fileName.endsWith(".json"));

		Gson gson = new GsonBuilder()
			.registerTypeAdapter(Event.class, new Event.Deserializer())
			.registerTypeAdapter(Condition.class, new Condition.Deserializer())
			.registerTypeAdapter(
				ResourceLocation.class,
				(JsonDeserializer<ResourceLocation>) (json, type, context) -> json.isJsonNull() || json.getAsString().isEmpty() ? null : new ResourceLocation(json.getAsString())
			)
			.registerTypeAdapter(PokemonSpecification.class, (JsonDeserializer<PokemonSpecification>) (json, type, context) -> PokemonSpecificationProxy.create(json.getAsString()))
			.registerTypeAdapter(SpecificTime.class, new SpecificTime.Deserializer())
			.registerTypeAdapter(Sound.class, new Sound.Deserializer())
			.create();

		int loadedSuccessfully = 0;
		for (ResourceLocation location : resourceLocations) {
			try {
				IResource resource = resourceManager.getResource(location);
				try (InputStream stream = resource.getInputStream(); Reader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
					Event event = gson.fromJson(reader, Event.class);
					if (event != null) {
						event.pack = resource.getSourceName() != null ? resource.getSourceName() : "unknown";
						event.file = location.getPath();
						registerEvent(event);
						loadedSuccessfully++;
					} else {
						System.out.println("Warning: Failed to parse event from " + location.getPath() + " - event is null");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("Loaded " + loadedSuccessfully + " events successfully");
	}

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
		onResourceManagerReload(resourceManager);
	}
}
