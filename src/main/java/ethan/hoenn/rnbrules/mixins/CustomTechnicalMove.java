package ethan.hoenn.rnbrules.mixins;

import com.pixelmonmod.pixelmon.items.TechnicalMoveItem;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TechnicalMoveItem.class)
public class CustomTechnicalMove {

	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/pixelmonmod/pixelmon/items/PixelmonItem;<init>(Lnet/minecraft/item/Item$Properties;)V"), index = 0, remap = false)
	private static Item.Properties addDurability(Item.Properties properties) {
		return properties.durability(3);
	}

	@Inject(
		method = "inventoryTick(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;IZ)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setDamageValue(I)V"),
		cancellable = true
	)
	private void preventDamageReset(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected, CallbackInfo ci) {
		ci.cancel();
	}
}
