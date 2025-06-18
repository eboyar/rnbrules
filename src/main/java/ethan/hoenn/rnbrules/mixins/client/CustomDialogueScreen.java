package ethan.hoenn.rnbrules.mixins.client;

import com.pixelmonmod.pixelmon.client.gui.custom.dialogue.DialogueScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.SoundEvents;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DialogueScreen.class)
public class CustomDialogueScreen {

	@Inject(method = "next", at = @At("HEAD"), remap = false)
	private void rnbrules$onNextDialogue(CallbackInfo ci) {
		Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	@Inject(method = "mouseClicked", at = @At(value = "FIELD", target = "Lcom/pixelmonmod/pixelmon/client/gui/custom/dialogue/DialogueScreen;pause:Z", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
	private void rnbrules$onDialogueChoiceClicked(double mouseX, double mouseY, int mouseButton, CallbackInfoReturnable<Boolean> cir) {
		Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}
}
