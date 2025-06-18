/**
 * Ported over from PixelTweaks by Strangeone101
 * https://github.com/StrangeOne101/PixelTweaks
 */
package ethan.hoenn.rnbrules.utils.misc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.world.GameRules;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class Gamerules {

	public static GameRules.RuleKey<GameRules.BooleanValue> DO_POKEMON_SPAWNING;
	public static GameRules.RuleKey<GameRules.BooleanValue> DO_TRAINER_SPAWNING;
	public static boolean ENABLED;

	public Gamerules() {
		try {
			Method createBoolean = ObfuscationReflectionHelper.findMethod(GameRules.BooleanValue.class, "func_223568_b", boolean.class); //the create(boolean) method
			createBoolean.setAccessible(true);
			DeferredWorkQueue.runLater(() -> {
				try {
					Object boolTrue = createBoolean.invoke(GameRules.BooleanValue.class, true);
					DO_POKEMON_SPAWNING = GameRules.register("doPokemonSpawning", GameRules.Category.SPAWNING, (GameRules.RuleType<GameRules.BooleanValue>) boolTrue);
					DO_TRAINER_SPAWNING = GameRules.register("doTrainerSpawning", GameRules.Category.SPAWNING, (GameRules.RuleType<GameRules.BooleanValue>) boolTrue);
					ENABLED = true;
				} catch (IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
			});
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw e;
		}
	}
}
