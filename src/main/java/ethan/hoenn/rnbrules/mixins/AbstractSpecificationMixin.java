/**
 * Ported over from PixelTweaks by Strangeone101
 * https://github.com/StrangeOne101/PixelTweaks
 */
package ethan.hoenn.rnbrules.mixins;

import com.pixelmonmod.api.AbstractSpecification;
import com.pixelmonmod.api.Specification;
import com.pixelmonmod.api.requirement.Requirement;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractSpecification.class)
public interface AbstractSpecificationMixin<A, B> extends Specification<A, B> {
	@Accessor(value = "requirements", remap = false)
	public List<Requirement<A, B, ?>> getRequirements();
}
