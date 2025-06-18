package ethan.hoenn.rnbrules.mixins;

import com.pixelmonmod.pixelmon.api.battles.attack.AttackRegistry;
import com.pixelmonmod.pixelmon.api.config.PixelmonConfigProxy;
import com.pixelmonmod.pixelmon.api.pokemon.stats.Moveset;
import com.pixelmonmod.pixelmon.entities.pixelmon.AbstractInteractionsEntity;
import ethan.hoenn.rnbrules.utils.managers.HiddenMachineManager;
import java.util.Optional;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(AbstractInteractionsEntity.class)
public class CustomAbstractInteractionsEntity {

	/**
	 * @author ethan
	 * @reason Change to use HMManager instead of checking if the pokemon has Surf
	 */
	@Overwrite(remap = false)
	public boolean canSurf() {
		AbstractInteractionsEntity entity = (AbstractInteractionsEntity) (Object) this;

		if (!PixelmonConfigProxy.getGeneral().getRiding().isRequireHM()) {
			return entity.getForm().getMovement().canSurf();
		} else {
			if (entity.getForm().getMovement().canSurf()) {
				PlayerEntity owner = (PlayerEntity) entity.getOwner();
				if (owner != null) {
					World world = owner.level;
					if (!world.isClientSide) {
						ServerWorld serverWorld = (ServerWorld) world;
						HiddenMachineManager hmManager = HiddenMachineManager.get(serverWorld);
						if (hmManager.hasHM(owner.getUUID(), "surf")) {
							return true;
						}
					}
				}
			}
			Moveset moves = entity.getPokemon().getMoveset();
			return entity.getForm().getMovement().canSurf() && moves.hasAttack(new Optional[] { AttackRegistry.SURF });
		}
	}
}
