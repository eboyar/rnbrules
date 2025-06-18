package ethan.hoenn.rnbrules.mixins;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import ethan.hoenn.rnbrules.utils.misc.GrowthHelper;
import java.util.Optional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Pokemon.class)
public class CustomPokemon {

	@Redirect(method = "initialize", at = @At(value = "INVOKE", target = "Lcom/pixelmonmod/pixelmon/enums/EnumGrowth;getRandomGrowth()Lcom/pixelmonmod/pixelmon/enums/EnumGrowth;"), remap = false)
	private EnumGrowth redirectGetRandomGrowth() {
		Pokemon thisPokemon = (Pokemon) (Object) this;

		Optional<EnumGrowth> restrictedGrowthOpt = GrowthHelper.getRestrictedGrowth(thisPokemon.getSpecies());
		if (restrictedGrowthOpt.isPresent()) {
			return restrictedGrowthOpt.get();
		}

		return GrowthHelper.getDefaultWeightedGrowth();
	}
}
