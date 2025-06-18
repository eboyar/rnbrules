package ethan.hoenn.rnbrules.mixins.client;

import com.pixelmonmod.pixelmon.client.gui.battles.pokemonOverlays.AllyElement;
import com.pixelmonmod.pixelmon.client.gui.battles.pokemonOverlays.OverlayNew;
import java.util.Comparator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(OverlayNew.class)
public class CustomOverlayNew {

	@ModifyArg(method = "generateElements", at = @At(value = "INVOKE", target = "Ljava/util/List;sort(Ljava/util/Comparator;)V"), index = 0, remap = false)
	private Comparator<AllyElement> fixPokemonPositionSorting(Comparator<AllyElement> comparator) {
		return Comparator.comparingInt(a -> a.getAlly().position);
	}
}
