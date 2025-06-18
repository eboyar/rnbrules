package ethan.hoenn.rnbrules.mixins.items;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.items.PixelmonBootsItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PixelmonBootsItem.class)
public abstract class CustomRunningBootsItem {

	@Inject(method = "onArmorTick", at = @At("HEAD"), cancellable = true, remap = false)
	private void removeNewRunningBootsDamage(ItemStack itemStack, World world, PlayerEntity player, CallbackInfo ci) {
		if (itemStack.getItem() == PixelmonItems.new_running_boots) {
			ci.cancel();
			if (itemStack.isDamaged()) {
				itemStack.setDamageValue(0);
			}
		}
	}
}
