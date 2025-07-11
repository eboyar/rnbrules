/**
 * Ported over from PixelTweaks by Strangeone101
 * https://github.com/StrangeOne101/PixelTweaks
 */
package ethan.hoenn.rnbrules.integration.tasks;

import com.pixelmonmod.pixelmon.api.pokedex.PlayerPokedex;
import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.species.Stats;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class PokedexTask extends Task {

	public enum PokedexFilter {
		ALL,
		GENERATION,
		TYPE,
		LEGEND,
		MYTHICAL,
		LEGEND_AND_MYTHICAL,
		ULTRA_BEAST,
		SINGLE_MON,
	}

	protected int maxPokedexSize = PixelmonSpecies.getAll().size();
	protected transient Set<Integer> filteredPokedex = new HashSet<>();

	public boolean caught = true;
	public PokedexFilter filter = PokedexFilter.ALL;
	public Element type = Element.NORMAL;
	public byte genMinFilter = 1;
	public byte genMaxFilter = 9;
	public boolean allowUndexable = false;
	public int singleMon = 0;

	public PokedexTask(Quest q) {
		super(q);
	}

	@Override
	public long getMaxProgress() {
		return maxPokedexSize;
	}

	@Override
	public void writeData(CompoundNBT nbt) {
		super.writeData(nbt);
		nbt.putByte("filter", (byte) filter.ordinal());
		nbt.putBoolean("caught", caught);
		nbt.putBoolean("allowUndexable", allowUndexable);
		if (filter == PokedexFilter.TYPE) {
			nbt.putByte("pokeType", (byte) type.ordinal());
		} else if (filter == PokedexFilter.GENERATION) {
			nbt.putByte("genMin", genMinFilter);
			nbt.putByte("genMax", genMaxFilter);
		} else if (filter == PokedexFilter.SINGLE_MON) {
			nbt.putInt("singleMon", singleMon);
		}
	}

	@Override
	public void readData(CompoundNBT nbt) {
		super.readData(nbt);
		filter = PokedexFilter.values()[nbt.getByte("filter")];
		caught = nbt.getBoolean("caught");
		allowUndexable = nbt.getBoolean("allowUndexable");
		if (filter == PokedexFilter.TYPE) {
			type = Element.values()[nbt.getByte("pokeType")];
		} else if (filter == PokedexFilter.GENERATION) {
			genMinFilter = nbt.getByte("genMin");
			genMaxFilter = nbt.getByte("genMax");
		} else if (filter == PokedexFilter.SINGLE_MON) {
			singleMon = nbt.getInt("singleMon");
		}
		calculateAmount();
	}

	@Override
	public void writeNetData(PacketBuffer buffer) {
		super.writeNetData(buffer);
		buffer.writeByte(filter.ordinal());
		buffer.writeBoolean(caught);
		buffer.writeBoolean(allowUndexable);
		if (filter == PokedexFilter.TYPE) {
			buffer.writeByte(type.ordinal());
		} else if (filter == PokedexFilter.GENERATION) {
			buffer.writeByte(genMinFilter);
			buffer.writeByte(genMaxFilter);
		} else if (filter == PokedexFilter.SINGLE_MON) {
			buffer.writeVarInt(singleMon);
		}
	}

	@Override
	public void readNetData(PacketBuffer buffer) {
		super.readNetData(buffer);
		filter = PokedexFilter.values()[buffer.readByte()];
		caught = buffer.readBoolean();
		allowUndexable = buffer.readBoolean();
		if (filter == PokedexFilter.TYPE) {
			type = Element.values()[buffer.readByte()];
		} else if (filter == PokedexFilter.GENERATION) {
			genMinFilter = buffer.readByte();
			genMaxFilter = buffer.readByte();
		} else if (filter == PokedexFilter.SINGLE_MON) {
			singleMon = buffer.readVarInt();
		}

		calculateAmount();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);

		PokedexFilter[] filterTypes = PokedexFilter.values();

		if (this instanceof PokedexPercentageTask) {
			filterTypes = new PokedexFilter[] {
				PokedexFilter.ALL,
				PokedexFilter.GENERATION,
				PokedexFilter.TYPE,
				PokedexFilter.LEGEND,
				PokedexFilter.MYTHICAL,
				PokedexFilter.LEGEND_AND_MYTHICAL,
				PokedexFilter.ULTRA_BEAST,
			};
		}

		config.addEnum(
			"filter",
			filter,
			v -> {
				filter = v;
				calculateAmount();
			},
			NameMap.of(PokedexFilter.ALL, filterTypes).nameKey(v -> "pixeltweaks.pokedex_filter_type." + v.name().toLowerCase()).create(),
			PokedexFilter.ALL
		);
		config.addBool("caught", caught, v -> caught = v, true);
		config.addEnum(
			"type",
			type,
			v -> {
				type = v;
				calculateAmount();
			},
			NameMap.of(Element.NORMAL, Element.getElements().toArray(new Element[0]))
				.nameKey(v -> "type." + v.name().toLowerCase())
				.icon(v -> Icon.getIcon(new ResourceLocation("pixeltweaks:textures/gui/types/" + v.name().toLowerCase() + ".png")))
				.create(),
			Element.NORMAL
		);

		config.addInt(
			"genMin",
			genMinFilter,
			v -> {
				genMinFilter = v.byteValue();
				calculateAmount();
			},
			(byte) 1,
			(byte) 1,
			(byte) 9
		);
		config.addInt(
			"genMax",
			genMaxFilter,
			v -> {
				genMaxFilter = v.byteValue();
				calculateAmount();
			},
			(byte) 9,
			(byte) 1,
			(byte) 9
		);

		if (!(this instanceof PokedexPercentageTask)) {
			config.addInt(
				"singleMon",
				singleMon,
				v -> {
					singleMon = v;
					filteredPokedex = new HashSet<>();
					filteredPokedex.add(singleMon);
					maxPokedexSize = 1;
				},
				0,
				1,
				5000
			);
		}
		config.addBool(
			"allowUndexable",
			allowUndexable,
			v -> {
				allowUndexable = v;
				calculateAmount();
			},
			false
		);
	}

	protected void calculateAmount() {
		Set<Integer> all = new HashSet<>();

		if (this.filter == PokedexFilter.SINGLE_MON) {
			all.add(this.singleMon);
			this.filteredPokedex = all;
			this.maxPokedexSize = all.size();
			return;
		}

		if (this.allowUndexable) {
			if (this.filter == PokedexFilter.TYPE) {
				PixelmonSpecies.getAll()
					.parallelStream()
					.forEach(species -> {
						Stats form = species.getDefaultForm();
						if (form.getTypes().contains(this.type)) {
							all.add(species.getDex());
						}
					});
			} else if (this.filter == PokedexFilter.GENERATION) {
				for (int current = this.genMinFilter; current <= this.genMaxFilter; current++) {
					all.addAll(PixelmonSpecies.getGenerationDex(current));
				}
			} else if (this.filter == PokedexFilter.LEGEND) {
				all.addAll(PixelmonSpecies.getLegendaries(true));
			} else if (this.filter == PokedexFilter.MYTHICAL) {
				all.addAll(PixelmonSpecies.getMythicals());
			} else if (this.filter == PokedexFilter.LEGEND_AND_MYTHICAL) {
				all.addAll(PixelmonSpecies.getLegendaries(false));
			} else if (this.filter == PokedexFilter.ULTRA_BEAST) {
				all.addAll(PixelmonSpecies.getUltraBeasts());
			} else {
				for (int gen : PixelmonSpecies.getGenerations()) {
					all.addAll(PixelmonSpecies.getGenerationDex(gen));
				}
			}
		} else {
			PixelmonSpecies.getAll()
				.parallelStream()
				.forEach(species -> {
					Stats form = species.getDefaultForm();

					if (form.hasTag("undexable")) return;

					if (this.filter == PokedexFilter.TYPE) {
						if (form.getTypes().contains(this.type)) {
							all.add(species.getDex());
						}
					} else if (this.filter == PokedexFilter.GENERATION) {
						if (species.getGeneration() >= this.genMinFilter && species.getGeneration() <= this.genMaxFilter) {
							all.add(species.getDex());
						}
					} else if (this.filter == PokedexFilter.LEGEND) {
						if (form.getTags().isLegendary(true)) {
							all.add(species.getDex());
						}
					} else if (this.filter == PokedexFilter.MYTHICAL) {
						if (form.getTags().isMythical()) {
							all.add(species.getDex());
						}
					} else if (this.filter == PokedexFilter.LEGEND_AND_MYTHICAL) {
						if (form.getTags().isLegendary(false)) {
							all.add(species.getDex());
						}
					} else if (this.filter == PokedexFilter.ULTRA_BEAST) {
						if (form.getTags().isUltraBeast()) {
							all.add(species.getDex());
						}
					} else {
						all.add(species.getDex());
					}
				});
		}

		this.filteredPokedex = all;
		this.maxPokedexSize = all.size();
	}

	public void updatePokedex(TeamData teamData, ServerPlayerEntity player) {
		if (teamData.isCompleted(this) || !teamData.file.isServerSide()) return;

		int ordinalToCheck = this.caught ? 2 : 1;

		PlayerPokedex dex = StorageProxy.getParty(player).playerPokedex;
		int progress = (int) dex.getSeenMap().entrySet().parallelStream().filter(entry -> this.filteredPokedex.contains(entry.getKey()) && entry.getValue().ordinal() >= ordinalToCheck).count();
		progress += (int) dex.formDex
			.rowMap()
			.entrySet()
			.parallelStream()
			.filter(entry -> this.filteredPokedex.contains(entry.getKey()) && entry.getValue().values().stream().anyMatch(e -> e >= ordinalToCheck))
			.count();
		progress = Math.min(progress, this.maxPokedexSize);

		teamData.setProgress(this, progress);
	}
}
