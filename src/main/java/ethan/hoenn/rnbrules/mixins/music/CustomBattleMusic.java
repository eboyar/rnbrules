/**
 * Ported over from PixelTweaks by Strangeone101
 * https://github.com/StrangeOne101/PixelTweaks
 */

package ethan.hoenn.rnbrules.mixins.music;

import com.pixelmonmod.pixelmon.api.config.PixelmonConfigProxy;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.client.ClientProxy;
import com.pixelmonmod.pixelmon.client.gui.battles.PixelmonClientData;
import com.pixelmonmod.pixelmon.client.music.BattleMusic;
import com.pixelmonmod.pixelmon.client.music.PixelmonMusic;
import com.pixelmonmod.pixelmon.client.music.VoidMusicTicker;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.init.registry.SoundRegistration;
import com.pixelmonmod.pixelmon.sounds.BattleMusicType;
import ethan.hoenn.rnbrules.music.ChainedMusic;
import ethan.hoenn.rnbrules.music.MusicEvent;
import ethan.hoenn.rnbrules.music.SoundManager;
import ethan.hoenn.rnbrules.music.event.EventRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.LocatableSound;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BattleMusic.class)
public abstract class CustomBattleMusic {

	@Shadow(remap = false)
	private static LocatableSound song;

	/**
	 * @author StrangeOne101
	 * @reason Rewrites the battle music logic, so we can use music for specific battles
	 */
	@Overwrite(remap = false)
	public static void startBattleMusic(BattleMusicType type, int index, long playtime, boolean repeat) {
		if (playtime == -1) {
			endBattleMusic();
			return;
		}

		List<PixelmonEntity> opponentEntities = new ArrayList<>(1);

		if (!Minecraft.getInstance().isLocalServer()) { //On a server
			//Define list of displayed opponents
			List<PixelmonClientData> opponents = new ArrayList<>(Arrays.asList(ClientProxy.battleManager.displayedEnemyPokemon));
			//Loop through all entities in the world
			for (Entity entity : Minecraft.getInstance().level.entitiesForRendering()) {
				if (entity instanceof PixelmonEntity) {
					PixelmonEntity pokemon = (PixelmonEntity) entity;

					//Loop through all opponents and check if the UUID matches
					for (Iterator<PixelmonClientData> it = opponents.iterator(); it.hasNext();) {
						PixelmonClientData data = it.next();
						if (data.pokemonUUID.equals(pokemon.getUUID())) {
							opponentEntities.add(pokemon);
							//System.out.println("Added: " + pokemon.toString());
							it.remove();
							break;
						}
					}
					if (opponents.isEmpty()) break;
				}
			}
		} else { //Single player. This method is faster
			BattleController controller = BattleRegistry.getBattle(Minecraft.getInstance().player);
			if (controller == null) {} else {
				BattleParticipant player = controller.getPlayer(Minecraft.getInstance().player.getDisplayName().getString());
				controller.getOpponentPokemon(player).stream().map(wrapper -> wrapper.entity).forEach(opponentEntities::add);
			}
		}

		//System.out.println(opponentEntities.toString());

		if (!opponentEntities.isEmpty()) {
			// BattleParticipant player = controller.getPlayer(Minecraft.getInstance().player.getDisplayName().getString());
			//List<PixelmonWrapper> opponents = controller.getOpponentPokemon(player);
			Collection<MusicEvent.Battle> events = EventRegistry.getEvents(MusicEvent.Battle.class);

			List<MusicEvent.Battle> filteredEvents = events
				.stream()
				.filter(event ->
					event.conditions
						.stream()
						.allMatch(condition -> {
							for (PixelmonEntity opponent : opponentEntities) {
								if (condition.conditionMet(opponent)) {
									return true;
								}
							}
							return false;
						})
				)
				.collect(java.util.stream.Collectors.toList());

			MusicEvent.Battle event = EventRegistry.getRandomEvent(MusicEvent.Battle.class, filteredEvents);
			if (event != null && !isPlaying()) {
				pixelTweaks$pause();

				SoundManager.playEvent(event);
				return;
			}
		}

		Minecraft mc = Minecraft.getInstance();
		pixelTweaks$pause();
		if (isPlaying()) {
			mc.getSoundManager().stop(song);
			song = null;
		}

		SoundEvent soundEvent = SoundRegistration.BATTLE_MUSIC.get(type).get();
		SimpleSound record = new BattleMusic.FixedTrackSound(
			soundEvent,
			index,
			SoundCategory.MUSIC,
			PixelmonConfigProxy.getBattle().getBattleMusicVolume(),
			1.0F,
			repeat,
			0,
			ISound.AttenuationType.NONE,
			0.0F,
			0.0F,
			0.0F
		);
		song = record;
		PixelmonMusic.fadeSoundToStart(record, 2000L);
	}

	/**
	 * @author StrangeOne101
	 * @reason Rewrites the battle music logic, so we can use music for specific battles
	 */
	@Overwrite(remap = false)
	public static void endBattleMusic() {
		Minecraft mc = Minecraft.getInstance();
		if (isPlaying()) {
			if (pixelTweaks$isPixelmonSongPlaying()) {
				PixelmonMusic.fadeSoundToStop(song, 2000L, CustomBattleMusic::pixelTweaks$unpause);
			} else {
				CompletableFuture[] futures = new CompletableFuture[SoundManager.BATTLE_MUSIC.size()];
				int num = 0;

				for (ChainedMusic music : SoundManager.BATTLE_MUSIC) {
					CompletableFuture<Void> future = new CompletableFuture<>();
					music.finish(() -> {
						future.complete(null);
						SoundManager.ALL_MUSIC.remove(music);
					});
					futures[num++] = future;
				}
				SoundManager.BATTLE_MUSIC.clear();
				CompletableFuture.allOf(futures).thenAccept(v -> {
					pixelTweaks$unpause();
				});
			}
		} else if (mc.getMusicManager() instanceof VoidMusicTicker) {
			pixelTweaks$unpause();
		}

		song = null;
	}

	@Unique
	private static void pixelTweaks$unpause() {
		VoidMusicTicker.restoreMusicTicker();
		//pixelTweaks$unpauseAmbienceMod();
		SoundManager.resumeAllMusic();
	}

	@Unique
	private static void pixelTweaks$pause() {
		VoidMusicTicker.replaceMusicTicker();
		//pixelTweaks$pauseAmbienceMod();
		SoundManager.pauseAllMusic();
	}

	/**
	 * @author StrangeOne101
	 * @reason See above
	 */
	@Overwrite(remap = false)
	public static boolean isPlaying() {
		return (SoundManager.BATTLE_MUSIC.size() > 0 && SoundManager.BATTLE_MUSIC.stream().anyMatch(ChainedMusic::isPlaying)) || (pixelTweaks$isPixelmonSongPlaying());
	}

	@Unique
	private static boolean pixelTweaks$isPixelmonSongPlaying() {
		return song != null && PixelmonMusic.getSoundHandler().isActive(song);
	}
}
