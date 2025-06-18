package ethan.hoenn.rnbrules.utils.misc;


import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.util.math.BlockPos;


public class SpawnPointHelper {


	public static boolean hasSpawnPoint(PixelmonEntity pokemon) {
		return pokemon.getPersistentData().contains("spX") &&
				pokemon.getPersistentData().contains("spZ");
	}

	public static boolean hasSpawnPoint(Pokemon pokemon) {
		return pokemon.getPersistentData().contains("spX") &&
				pokemon.getPersistentData().contains("spZ");
	}

	public static void setSpawnPoint(PixelmonEntity pokemon, double x, double z) {
		pokemon.getPersistentData().putDouble("spX", x);
		pokemon.getPersistentData().putDouble("spZ", z);
	}


	public static void setSpawnPoint(PixelmonEntity pokemon) {
		setSpawnPoint(pokemon, pokemon.getX(), pokemon.getZ());
	}


	public static void setSpawnPoint(PixelmonEntity pokemon, BlockPos pos) {
		setSpawnPoint(pokemon, pos.getX() + 0.5, pos.getZ() + 0.5);
	}


	public static double getSpawnX(PixelmonEntity pokemon) {
		return pokemon.getPersistentData().getDouble("spX");
	}


	public static double getSpawnZ(PixelmonEntity pokemon) {
		return pokemon.getPersistentData().getDouble("spZ");
	}


	public static BlockPos getSpawnPoint(PixelmonEntity pokemon) {
		if (!hasSpawnPoint(pokemon)) {

			setSpawnPoint(pokemon);
		}
		return new BlockPos(getSpawnX(pokemon), pokemon.getY(), getSpawnZ(pokemon));
	}


	public static double distanceToSpawnSq(PixelmonEntity pokemon) {
		if (!hasSpawnPoint(pokemon)) {
			return 0;
		}

		double x = getSpawnX(pokemon);
		double z = getSpawnZ(pokemon);

		double dx = pokemon.getX() - x;
		double dz = pokemon.getZ() - z;

		return dx * dx + dz * dz;
	}
}
