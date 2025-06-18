package ethan.hoenn.rnbrules.mixins.balls;

import com.pixelmonmod.pixelmon.api.pokemon.item.pokeball.logic.SafariBall;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SafariBall.class)
public class CustomSafariBall {
	/**
	 * @author ethan
	 * @reason make Safari Ball act like Master Ball with "route: safari" spec
	 */
	/*
    @Overwrite
    public double getCatchBonus(PokeBall type, PokeBallEntity entity, PlayerEntity thrower, Pokemon pokemon, PokeBallMode mode) {
        // Check if Pokemon has the spec "route: safari"
        if (pokemon.hasSpec("route: safari")) {
            return 255.0; // Master Ball catch rate
        }

        // Original behavior with exact same biome finding logic
        Optional<PixelmonEntity> o = pokemon.getPixelmonEntity();
        if (o.isPresent()) {
            Biome biome = o.get().level.getBiome(new BlockPos(MathHelper.floor(o.get().getX()), 0, MathHelper.floor(o.get().getZ())));
            if (biome.getRegistryName().getPath().contains("plains") || biome.getRegistryName().getPath().contains("savanna")) {
                return (double)1.5F;
            }
        }

        return (double)1.0F;
    }
}
    */
}
