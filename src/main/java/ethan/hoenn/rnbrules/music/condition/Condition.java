/**
 * Ported over from PixelTweaks by Strangeone101
 * https://github.com/StrangeOne101/PixelTweaks
 */

package ethan.hoenn.rnbrules.music.condition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import java.lang.reflect.Type;

public abstract class Condition<T> {

	public String type;

	public static class Deserializer implements JsonDeserializer<Condition<?>> {

		@Override
		public Condition<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jsonObject = json.getAsJsonObject();
			String conditionType = jsonObject.get("type").getAsString();

			if ("pokemon".equals(conditionType)) {
				return context.deserialize(json, PokemonCondition.class);
			} else if ("pokemon_list".equals(conditionType)) {
				return context.deserialize(json, PokemonListCondition.class);
			} else if ("trainer".equals(conditionType)) {
				return context.deserialize(json, TrainerCondition.class);
			} else if ("weather".equals(conditionType)) {
				return context.deserialize(json, WeatherCondition.class);
			} else if ("chance".equals(conditionType)) {
				return context.deserialize(json, ChanceCondition.class);
			} else if ("location".equals(conditionType)) {
				return context.deserialize(json, LocationCondition.class);
			}

			throw new JsonParseException("Invalid condition type: " + conditionType);
		}
	}

	protected abstract boolean conditionMet(T item);

	public abstract T itemFromPixelmon(PixelmonEntity entity);

	public boolean conditionMet(PixelmonEntity entity) {
		return conditionMet(itemFromPixelmon(entity));
	}

	@Override
	public String toString() {
		return "Condition{" + "type='" + type + '\'' + '}';
	}
}
