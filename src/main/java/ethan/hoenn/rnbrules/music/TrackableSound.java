/**
 * Ported over from PixelTweaks by Strangeone101
 * https://github.com/StrangeOne101/PixelTweaks
 */

package ethan.hoenn.rnbrules.music;

import com.pixelmonmod.pixelmon.client.music.PixelmonMusic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ISoundEventListener;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

public class TrackableSound extends SimpleSound implements AutoCloseable, ISoundEventListener {

	private boolean called;
	private boolean started;

	public TrackableSound(
		ResourceLocation sound,
		SoundCategory category,
		float volume,
		float pitch,
		boolean repeat,
		int repeatDelay,
		AttenuationType attenuationType,
		double x,
		double y,
		double z,
		boolean global
	) {
		super(sound, category, volume, pitch, repeat, repeatDelay, attenuationType, x, y, z, global);
		Minecraft.getInstance().getSoundManager().addListener(this);
	}

	public void fadeIn(long time) {
		this.called = true;
		PixelmonMusic.fadeSoundToStart(this, time);
	}

	public void fadeOut(long time) {
		SoundManager.fadeSoundToStop(this, time, null);
	}

	public void play() {
		Minecraft.getInstance().getSoundManager().play(this);
		this.started = true;
	}

	public void stop() {
		Minecraft.getInstance().getSoundManager().stop(this);
	}

	public boolean isPlaying() {
		return called || Minecraft.getInstance().getSoundManager().isActive(this);
	}

	public boolean isFinished() {
		return started && !isPlaying();
	}

	public boolean isStarted() {
		return called || started;
	}

	@Override
	public void close() throws Exception {
		Minecraft.getInstance().getSoundManager().removeListener(this);
	}

	@Override
	public void onPlaySound(ISound soundIn, SoundEventAccessor accessor) {
		if (soundIn == this) {
			this.called = false;
			this.started = true;
		}
	}
}
