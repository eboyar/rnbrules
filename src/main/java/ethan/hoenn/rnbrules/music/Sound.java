/**
 * Ported over from PixelTweaks by Strangeone101
 * https://github.com/StrangeOne101/PixelTweaks
 */

package ethan.hoenn.rnbrules.music;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import ethan.hoenn.rnbrules.utils.misc.Fade;
import java.lang.reflect.Type;
import java.util.Objects;
import net.minecraft.util.ResourceLocation;

public class Sound {

	public ResourceLocation sound;

	public Fade fade = new Fade(0, 0);

	public float volume = 1.0F;
	public float pitch = 1.0F;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Sound sound1 = (Sound) o;
		return Float.compare(sound1.volume, volume) == 0 && Float.compare(sound1.pitch, pitch) == 0 && Objects.equals(sound, sound1.sound) && Objects.equals(fade, sound1.fade);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sound, fade, volume, pitch);
	}

	@Override
	public String toString() {
		return "Sound[" + sound.toString() + "]";
	}

	public static class Deserializer implements JsonDeserializer<Sound> {

		@Override
		public Sound deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
				Sound sound = new Sound();
				sound.sound = context.deserialize(json, ResourceLocation.class);
				return sound;
			} else if (json.isJsonObject()) {
				Sound sound = new Sound();
				JsonObject o = json.getAsJsonObject();
				if (o.has("fade")) sound.fade = context.deserialize(o.get("fade"), Fade.class);
				if (o.has("volume")) sound.volume = o.get("volume").getAsFloat();
				if (o.has("pitch")) sound.pitch = o.get("pitch").getAsFloat();
				if (o.has("sound")) sound.sound = context.deserialize(o.get("sound"), ResourceLocation.class);

				return sound;
			}
			return null;
		}
	}
}
