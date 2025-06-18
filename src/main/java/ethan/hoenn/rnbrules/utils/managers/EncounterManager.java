package ethan.hoenn.rnbrules.utils.managers;

import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.pixelmon.api.battles.BattleResults;
import com.pixelmonmod.pixelmon.api.battles.BattleType;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.api.BattleBuilder;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleRuleRegistry;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleRules;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.WildPixelmonParticipant;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.enums.EnumOldGenMode;
import ethan.hoenn.rnbrules.network.PacketHandler;
import ethan.hoenn.rnbrules.network.RoamerOverlayPacket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.network.NetworkDirection;

public class EncounterManager extends WorldSavedData implements ResetableManager {

	private static final String DATA_TAG = "encounters";

	private static EncounterManager instance;

	private final Map<UUID, Set<String>> encounteredRoamers = new HashMap<>();
	private final Map<UUID, Map<String, String>> roamerEncounterRoutes = new HashMap<>();
	private final Map<UUID, Boolean> playerRoamerToggle = new HashMap<>();

	private static final List<String> VALID_ROAMER_ROUTES = Arrays.asList(
		"route110",
		"route111",
		"route117",
		"route118",
		"route119",
		"route120",
		"route121",
		"route122",
		"route123",
		"route124",
		"route125",
		"route126",
		"route127",
		"route128",
		"route129",
		"route130",
		"route131",
		"route132",
		"route133",
		"route134"
	);

	private final Map<String, PokemonSpecification> forcedEncounters = new HashMap<>();
	private final Map<String, PokemonSpecification> roamerSpecs = new HashMap<>();

	public EncounterManager() {
		super(DATA_TAG);
		initializeEncounters();
	}

	private void initializeEncounters() {
		forcedEncounters.put("kecleon", PokemonSpecificationProxy.create("species:kecleon cl:route120 numivs:3 gr:ordinary hi:shed_shell minlvl:50 maxlvl:60"));
		forcedEncounters.put("voltorb", PokemonSpecificationProxy.create("species:voltorb form:hisuian gr:runt cl:newmauville numivs:3 lvl:40"));
		forcedEncounters.put("electrode", PokemonSpecificationProxy.create("species:electrode form:hisuian gr:runt cl:newmauville numivs:3 lvl:50"));

		roamerSpecs.put("latias", PokemonSpecificationProxy.create("species:latias cl:roamer numivs:3 lvl:70"));
		roamerSpecs.put("latios", PokemonSpecificationProxy.create("species:latios cl:roamer numivs:3 lvl:70"));
		roamerSpecs.put("cresselia", PokemonSpecificationProxy.create("species:cresselia cl:roamer numivs:3 lvl:70"));
		roamerSpecs.put("tornadus", PokemonSpecificationProxy.create("species:tornadus cl:roamer numivs:3 lvl:70"));
		roamerSpecs.put("thundurus", PokemonSpecificationProxy.create("species:thundurus cl:roamer numivs:3 lvl:70"));
		roamerSpecs.put("landorus", PokemonSpecificationProxy.create("species:landorus cl:roamer numivs:3 lvl:70"));
		roamerSpecs.put("enamorus", PokemonSpecificationProxy.create("species:enamorus cl:roamer numivs:3 lvl:70"));
	}

	public static EncounterManager get(ServerWorld world) {
		if (instance == null) {
			instance = world.getDataStorage().computeIfAbsent(EncounterManager::new, DATA_TAG);
		}
		return instance;
	}

	@Override
	public boolean resetPlayerData(UUID playerUUID) {
		boolean hadData = false;

		if (encounteredRoamers.remove(playerUUID) != null) {
			hadData = true;
		}

		if (roamerEncounterRoutes.remove(playerUUID) != null) {
			hadData = true;
		}

		if (playerRoamerToggle.remove(playerUUID) != null) {
			hadData = true;
		}

		if (hadData) {
			setDirty();
		}

		return hadData;
	}

	public boolean isValidRoamerRoute(String location) {
		return VALID_ROAMER_ROUTES.contains(location);
	}

	public Pokemon createRoamerEncounter(String roamerName, String route) {
		if (!roamerSpecs.containsKey(roamerName) || !isValidRoamerRoute(route)) {
			return null;
		}

		PokemonSpecification baseSpec = roamerSpecs.get(roamerName);
		PokemonSpecification routedSpec = PokemonSpecificationProxy.create(baseSpec.toString() + " cl:" + route);
		return routedSpec.create();
	}

	public Pokemon createForcedEncounter(String encounterName) {
		PokemonSpecification baseSpec = forcedEncounters.get(encounterName);
		if (baseSpec != null) {
			return baseSpec.create();
		}
		return null;
	}

	public boolean hasPlayerEncounteredRoamer(UUID playerUUID, String roamerName) {
		Set<String> encountered = encounteredRoamers.getOrDefault(playerUUID, new HashSet<>());
		return encountered.contains(roamerName);
	}

	public void recordRoamerEncounter(UUID playerUUID, String roamerName, String route) {
		encounteredRoamers.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(roamerName);

		Map<String, String> routeMap = roamerEncounterRoutes.computeIfAbsent(playerUUID, k -> new HashMap<>());
		routeMap.put(roamerName, route);

		this.setDirty();
	}

	public boolean hasPlayerEncounteredRoamerOnRoute(UUID playerUUID, String route) {
		Map<String, String> routeMap = roamerEncounterRoutes.get(playerUUID);
		if (routeMap == null) {
			return false;
		}

		return routeMap.values().contains(route);
	}

	public String getRandomAvailableRoamer(UUID playerUUID, String route) {
		if (!isValidRoamerRoute(route) || hasPlayerEncounteredRoamerOnRoute(playerUUID, route)) {
			return null;
		}

		Set<String> playerEncountered = encounteredRoamers.getOrDefault(playerUUID, new HashSet<>());
		List<String> availableRoamers = new ArrayList<>();

		for (String roamer : roamerSpecs.keySet()) {
			if (!playerEncountered.contains(roamer)) {
				availableRoamers.add(roamer);
			}
		}

		if (availableRoamers.isEmpty()) {
			return null;
		}

		Random random = new Random();
		return availableRoamers.get(random.nextInt(availableRoamers.size()));
	}

	public boolean tryTriggerRoamerEncounter(ServerPlayerEntity player) {
		UUID playerUUID = player.getUUID();

		if (!areRoamerEncountersEnabled(playerUUID)) {
			return false;
		}

		String currentLocation = LocationManager.get(player.getLevel()).getPlayerCurrentLocation(playerUUID);

		if (currentLocation == null) {
			return false;
		}

		String normalizedLocation = LocationManager.normalizeLocationName(currentLocation);

		if (!isValidRoamerRoute(normalizedLocation) || hasPlayerEncounteredRoamerOnRoute(playerUUID, normalizedLocation)) {
			return false;
		}

		String roamerName = getRandomAvailableRoamer(playerUUID, normalizedLocation);
		if (roamerName == null) {
			return false;
		}

		Pokemon roamer = createRoamerEncounter(roamerName, normalizedLocation);
		if (roamer == null) {
			return false;
		}

		boolean battleStarted = startForcedEncounterBattle(player, roamer);

		if (battleStarted) {
			recordRoamerEncounter(playerUUID, roamerName, normalizedLocation);

			PacketHandler.INSTANCE.sendTo(new RoamerOverlayPacket(roamerName, "You've encountered a Roamer!", 120), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
		}

		return battleStarted;
	}

	public static boolean startForcedEncounterBattle(ServerPlayerEntity player, Pokemon pokemon) {
		ServerWorld world = player.getLevel();
		PlayerPartyStorage storage = StorageProxy.getParty(player);
		List<Pokemon> partyList = storage.getTeam();

		if (BattleRegistry.getBattle(player) != null) {
			return false;
		}

		Pokemon[] starting = partyList.stream().filter(Objects::nonNull).toArray(Pokemon[]::new);

		PlayerParticipant pp = new PlayerParticipant(player, starting);
		pp.setNumControlledPokemon(1);

		Vector3d lookVec = player.getLookAngle();
		Vector3d eyePos = player.getEyePosition(1.0F);

		Pokemon firstPokemon = storage.getFirstAblePokemon();
		if (firstPokemon == null) {
			return false;
		}

		double playerY = player.getY();
		int seaLevel = player.level.getSeaLevel();

		boolean isInWater = world.getBlockState(player.blockPosition()).getMaterial().isLiquid() || world.getBlockState(player.blockPosition().below()).getMaterial().isLiquid();

		boolean isSurfHeight = Math.abs(playerY - seaLevel) <= 1;

		boolean isPlayerSurfing = player.isPassenger() && isInWater && isSurfHeight;

		if (!isPlayerSurfing && world.getBlockState(player.blockPosition().below()).getMaterial().isLiquid()) {
			return false;
		}

		Vector3d playerPokemonSpawnVec = eyePos.add(lookVec.scale(1.0));
		BlockPos playerPokemonSpawnPos = new BlockPos(playerPokemonSpawnVec.x, playerPokemonSpawnVec.y, playerPokemonSpawnVec.z);
		BlockState playerPokemonBlockState = world.getBlockState(playerPokemonSpawnPos);

		if (!playerPokemonBlockState.getMaterial().isReplaceable()) {
			return false;
		}

		firstPokemon.getOrSpawnPixelmon(world, playerPokemonSpawnPos.getX() + 1, playerPokemonSpawnPos.getY(), playerPokemonSpawnPos.getZ() + 1);

		Vector3d wildSpawnVec = null;
		BlockPos wildSpawnPos = null;
		BlockPos playerBlockPos = player.blockPosition();

		for (int distance : new int[] { 9, 8, 7, 6, 5 }) {
			Vector3d initialVec = eyePos.add(lookVec.scale(distance));
			BlockPos initialPos = new BlockPos(initialVec.x, initialVec.y, initialVec.z);

			BlockPos.Mutable mutablePos = new BlockPos.Mutable(initialPos.getX(), initialPos.getY(), initialPos.getZ());
			BlockState blockState = world.getBlockState(mutablePos);

			BlockPos potentialWildSpawnPos = null;
			Vector3d potentialWildSpawnVec = null;

			if (blockState.getMaterial().equals(Material.AIR)) {
				for (int yOffset = 0; yOffset >= -5; yOffset--) {
					mutablePos.setY(initialPos.getY() + yOffset);
					BlockState currentBlockState = world.getBlockState(mutablePos);

					if (currentBlockState.getMaterial().isSolid()) {
						potentialWildSpawnVec = new Vector3d(mutablePos.getX() + 0.5, mutablePos.getY() + 1.0, mutablePos.getZ() + 0.5);
						potentialWildSpawnPos = new BlockPos(potentialWildSpawnVec);
						break;
					} else if (currentBlockState.getMaterial().isLiquid()) {
						BlockPos abovePos = mutablePos.above();
						if (world.getBlockState(abovePos).getMaterial().equals(Material.AIR)) {
							potentialWildSpawnVec = new Vector3d(mutablePos.getX() + 0.5, mutablePos.getY() + 0.1, mutablePos.getZ() + 0.5);
							potentialWildSpawnPos = new BlockPos(potentialWildSpawnVec);
							break;
						}
					}
				}
			} else if (blockState.getMaterial().isLiquid()) {
				for (int yOffset = 0; yOffset <= 5; yOffset++) {
					mutablePos.setY(initialPos.getY() + yOffset);
					BlockState currentBlockState = world.getBlockState(mutablePos);
					BlockPos abovePos = mutablePos.above();

					if (currentBlockState.getMaterial().isLiquid() && world.getBlockState(abovePos).getMaterial().equals(Material.AIR)) {
						potentialWildSpawnVec = new Vector3d(mutablePos.getX() + 0.5, mutablePos.getY() + 0.1, mutablePos.getZ() + 0.5);
						potentialWildSpawnPos = new BlockPos(potentialWildSpawnVec);
						break;
					}
				}
			} else if (blockState.getMaterial().isSolid()) {
				for (int yOffset = 1; yOffset <= 5; yOffset++) {
					mutablePos.setY(initialPos.getY() + yOffset);
					BlockState currentBlockState = world.getBlockState(mutablePos);

					if (currentBlockState.getMaterial().equals(Material.AIR)) {
						potentialWildSpawnVec = new Vector3d(mutablePos.getX() + 0.5, mutablePos.getY(), mutablePos.getZ() + 0.5);
						potentialWildSpawnPos = new BlockPos(potentialWildSpawnVec);
						break;
					} else if (currentBlockState.getMaterial().isLiquid()) {
						potentialWildSpawnVec = new Vector3d(mutablePos.getX() + 0.5, mutablePos.getY() + 0.1, mutablePos.getZ() + 0.5);
						potentialWildSpawnPos = new BlockPos(potentialWildSpawnVec);
						break;
					}
				}
			}

			if (potentialWildSpawnPos != null) {
				if (potentialWildSpawnPos.distSqr(playerBlockPos) >= 25.0) {
					wildSpawnVec = potentialWildSpawnVec;
					wildSpawnPos = potentialWildSpawnPos;
					break;
				}
			}
		}

		if (wildSpawnVec == null) {
			return false;
		}

		PixelmonEntity entity = pokemon.getOrSpawnPixelmon(world, wildSpawnPos.getX() + 0.5, wildSpawnPos.getY(), wildSpawnPos.getZ() + 0.5);
		WildPixelmonParticipant wpp = new WildPixelmonParticipant(entity);

		BattleBuilder battleBuilder = BattleBuilder.builder()
			.startSync()
			.rules(new BattleRules(BattleType.SINGLE).set(BattleRuleRegistry.GEN_MODE, EnumOldGenMode.Both))
			.ignoreTempParties()
			.disableExp()
			.allowSpectators()
			.teamOne(pp)
			.teamTwo(wpp);

		battleBuilder
			.endHandler((battleEndEvent, battleController) -> {
				if (entity.getOwnerUUID() != player.getUUID()) {
					entity.remove();
				}
			})
			.start();

		return true;
	}

	public boolean areRoamerEncountersEnabled(UUID playerUUID) {
		return playerRoamerToggle.getOrDefault(playerUUID, false);
	}

	public void setRoamerEncountersEnabled(UUID playerUUID, boolean enabled) {
		playerRoamerToggle.put(playerUUID, enabled);
		this.setDirty();
	}

	public boolean toggleRoamerEncounters(UUID playerUUID) {
		boolean newState = !areRoamerEncountersEnabled(playerUUID);
		setRoamerEncountersEnabled(playerUUID, newState);
		return newState;
	}

	public Map<UUID, Set<String>> getEncounteredRoamers() {
		return encounteredRoamers;
	}

	public Map<UUID, Map<String, String>> getRoamerEncounterRoutes() {
		return roamerEncounterRoutes;
	}

	public Map<UUID, Boolean> getPlayerRoamerToggle() {
		return playerRoamerToggle;
	}

	@Override
	public void load(CompoundNBT nbt) {
		encounteredRoamers.clear();
		roamerEncounterRoutes.clear();
		playerRoamerToggle.clear();

		if (nbt.contains("EncounteredRoamers")) {
			CompoundNBT encounteredNBT = nbt.getCompound("EncounteredRoamers");
			for (String uuidStr : encounteredNBT.getAllKeys()) {
				UUID uuid = UUID.fromString(uuidStr);
				Set<String> roamers = new HashSet<>();

				CompoundNBT playerRoamersNBT = encounteredNBT.getCompound(uuidStr);
				for (String roamerKey : playerRoamersNBT.getAllKeys()) {
					if (playerRoamersNBT.getBoolean(roamerKey)) {
						roamers.add(roamerKey);
					}
				}

				encounteredRoamers.put(uuid, roamers);
			}
		}

		if (nbt.contains("RoamerRoutes")) {
			CompoundNBT routesNBT = nbt.getCompound("RoamerRoutes");
			for (String uuidStr : routesNBT.getAllKeys()) {
				UUID uuid = UUID.fromString(uuidStr);
				Map<String, String> routeMap = new HashMap<>();

				CompoundNBT playerRoutesNBT = routesNBT.getCompound(uuidStr);
				for (String roamerKey : playerRoutesNBT.getAllKeys()) {
					routeMap.put(roamerKey, playerRoutesNBT.getString(roamerKey));
				}

				roamerEncounterRoutes.put(uuid, routeMap);
			}
		}

		if (nbt.contains("PlayerRoamerToggle")) {
			CompoundNBT toggleNBT = nbt.getCompound("PlayerRoamerToggle");
			for (String uuidStr : toggleNBT.getAllKeys()) {
				UUID uuid = UUID.fromString(uuidStr);
				playerRoamerToggle.put(uuid, toggleNBT.getBoolean(uuidStr));
			}
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		CompoundNBT encounteredNBT = new CompoundNBT();
		for (Map.Entry<UUID, Set<String>> entry : encounteredRoamers.entrySet()) {
			CompoundNBT playerRoamersNBT = new CompoundNBT();
			for (String roamer : entry.getValue()) {
				playerRoamersNBT.putBoolean(roamer, true);
			}
			encounteredNBT.put(entry.getKey().toString(), playerRoamersNBT);
		}
		nbt.put("EncounteredRoamers", encounteredNBT);

		CompoundNBT routesNBT = new CompoundNBT();
		for (Map.Entry<UUID, Map<String, String>> entry : roamerEncounterRoutes.entrySet()) {
			CompoundNBT playerRoutesNBT = new CompoundNBT();
			for (Map.Entry<String, String> routeEntry : entry.getValue().entrySet()) {
				playerRoutesNBT.putString(routeEntry.getKey(), routeEntry.getValue());
			}
			routesNBT.put(entry.getKey().toString(), playerRoutesNBT);
		}
		nbt.put("RoamerRoutes", routesNBT);

		CompoundNBT toggleNBT = new CompoundNBT();
		for (Map.Entry<UUID, Boolean> entry : playerRoamerToggle.entrySet()) {
			toggleNBT.putBoolean(entry.getKey().toString(), entry.getValue());
		}
		nbt.put("PlayerRoamerToggle", toggleNBT);

		return nbt;
	}
}
