/**
 * Ported over from PixelTweaks by Strangeone101
 * https://github.com/StrangeOne101/PixelTweaks
 */

package ethan.hoenn.rnbrules.music;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.pixelmonmod.pixelmon.client.music.PixelmonMusic;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ChannelManager;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.SoundCategory;

public class SoundManager {

	public static final ExecutorService EXECUTOR;

	public static Set<ChainedMusic> ALL_MUSIC = new HashSet<>();
	public static Set<ChainedMusic> BATTLE_MUSIC = new HashSet<>();

	static {
		EXECUTOR = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setPriority(5).setDaemon(true).setNameFormat("rnbrules_sound_%d").build());

		EXECUTOR.submit(() -> {
			try {
				Thread.sleep(20);

				while (Minecraft.getInstance().isRunning()) {
					Iterator<ChainedMusic> allMusic = ALL_MUSIC.iterator();

					while (allMusic.hasNext()) {
						ChainedMusic music = allMusic.next();
						if (music.shouldTick()) {
							music.tick();
						} else if (!music.isPlaying()) {
							allMusic.remove();

							BATTLE_MUSIC.remove(music);
						}
					}

					Thread.sleep(20);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public static void fadeSoundToStart(ISound sound, long millis) {
		try {
			if (PixelmonMusic.getSoundManager().isActive(sound)) return;

			EXECUTOR.submit(() -> {
				PixelmonMusic.resetFade(sound, false);
				PixelmonMusic.getSoundHandler().play(sound);
				AtomicReference<ChannelManager.Entry> channel = new AtomicReference<ChannelManager.Entry>();
				PixelmonMusic.getSoundManager()
					.instanceToChannel.forEach((s, c) -> {
						if (Objects.equals(sound, s)) {
							channel.set(c);
						}
					});
				float initialVolume;
				if (channel.get() != null) {
					initialVolume = PixelmonMusic.getSoundManager().calculateVolume(sound);
				} else {
					initialVolume = 1.0F;
				}

				try {
					for (int i = 0; (long) i < millis; ++i) {
						float volume = PixelmonMusic.fadeSound(sound, initialVolume, millis, false);
						if (channel.get() != null) {
							(channel.get()).execute(source -> {
									source.setVolume(volume);
								});
						}

						Thread.sleep(1L);
					}
				} catch (InterruptedException var7) {
					var7.printStackTrace();
				}

				PixelmonMusic.resetFade(sound, false);
				if (channel.get() != null) {
					(channel.get()).execute(source -> {
							source.setVolume(initialVolume);
						});
				}
			});
		} catch (Exception var4) {
			PixelmonMusic.getSoundManager().play(sound);
		}
	}

	public static void fadeSoundToStop(ISound sound, long millis, Runnable runnable) {
		try {
			if (!PixelmonMusic.getSoundManager().isActive(sound)) return;
			EXECUTOR.submit(() -> {
				PixelmonMusic.resetFade(sound, true);
				AtomicReference<ChannelManager.Entry> channel = new AtomicReference<>(null);
				PixelmonMusic.getSoundManager()
					.instanceToChannel.forEach((s, c) -> {
						if (Objects.equals(sound, s)) {
							channel.set(c);
						}
					});
				float initialVolume;
				if (channel.get() != null) {
					initialVolume = PixelmonMusic.getSoundManager().calculateVolume(sound);
				} else {
					initialVolume = 1.0F;
				}

				try {
					for (int i = 0; (long) i < millis; ++i) {
						float volume = PixelmonMusic.fadeSound(sound, initialVolume, millis, true);
						if (channel.get() != null) {
							(channel.get()).execute(source -> {
									source.setVolume(volume);
								});
						}

						Thread.sleep(1L);
					}
				} catch (InterruptedException var8) {
					var8.printStackTrace();
				}

				PixelmonMusic.getSoundManager().stop(sound);
				PixelmonMusic.resetFade(sound, true);
				if (channel.get() != null) {
					((ChannelManager.Entry) channel.get()).execute(source -> {
							source.setVolume(initialVolume);
						});
				}

				if (runnable != null) {
					runnable.run();
				}
			});
		} catch (Exception var5) {
			PixelmonMusic.getSoundManager().stop(sound);
			if (runnable != null) {
				runnable.run();
			}
		}
	}

	public static void playBattleAction(PixelmonEntity entity, MusicEvent.BattleAction.Action... actions) {
		for (MusicEvent.BattleAction.Action action : actions) {
			Collection<MusicEvent.BattleAction> events = MusicEvent.BattleAction.REGISTRY.getOrDefault(action, new HashSet<>());

			Optional<MusicEvent.BattleAction> optional = events
				.stream()
				.filter(event -> {
					return event.conditions
						.stream()
						.allMatch(condition -> {
							if (condition.conditionMet(entity)) {
								return true;
							} else {}

							return false;
						});
				})
				.findFirst();

			if (optional.isPresent()) {
				MusicEvent.BattleAction event = optional.get();

				playEvent(event);
				return;
			}
		}
		// BattleParticipant player = controller.getPlayer(Minecraft.getInstance().player.getDisplayName().getString());
		//List<PixelmonWrapper> opponents = controller.getOpponentPokemon(player);

	}

	public static void playEvent(MusicEvent event) {
		if (event.music != null) {
			ChainedMusic chainedMusic = new ChainedMusic(event.music);

			Set<ChainedMusic> allMusic = ALL_MUSIC;

			if (event instanceof MusicEvent.Battle) {
				allMusic = BATTLE_MUSIC;
			}

			if (event.music.cutOtherMusic) {
				for (ChainedMusic music : allMusic) {
					music.finish(() -> {});
				}
				allMusic.clear();
			}
			ALL_MUSIC.add(chainedMusic);
			if (event instanceof MusicEvent.Battle) {
				BATTLE_MUSIC.add(chainedMusic);
			}
		} else if (event.sound != null) {
			SimpleSound sound = new SimpleSound(event.sound.sound, SoundCategory.HOSTILE, event.sound.volume, event.sound.pitch, false, 0, SimpleSound.AttenuationType.NONE, 0.0D, 0.0D, 0.0D, true);

			fadeSoundToStart(sound, event.sound.fade.start);
		}
	}

	public static void pauseAllMusic() {
		List<ISound> pixelTweaksMusic = ALL_MUSIC.stream().map(ChainedMusic::getPlaying).collect(Collectors.toList());
		PixelmonMusic.getSoundManager()
			.instanceToChannel.forEach((s, e) -> {
				//Make sure it is music, but make sure it isn't battle music played by PixelTweaks
				if (s.getSource() == SoundCategory.MUSIC && e.channel != null && !pixelTweaksMusic.contains(s)) {
					e.channel.pause();
				}
			});
	}

	public static void resumeAllMusic() {
		List<ISound> pixelTweaksMusic = ALL_MUSIC.stream().map(ChainedMusic::getPlaying).collect(Collectors.toList());
		PixelmonMusic.getSoundManager()
			.instanceToChannel.forEach((s, e) -> {
				if (s.getSource() == SoundCategory.MUSIC && e.channel != null && !pixelTweaksMusic.contains(s)) {
					//e.source.resume();
					fadeSoundToStart(s, 2000L);
				}
			});
	}
}
