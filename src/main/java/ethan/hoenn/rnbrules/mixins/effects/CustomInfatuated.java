package ethan.hoenn.rnbrules.mixins.effects;

import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.Infatuated;
import com.pixelmonmod.pixelmon.battles.status.StatusType;
import com.pixelmonmod.pixelmon.comm.ChatHandler;
import net.minecraft.util.text.TranslationTextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = Infatuated.class, remap = false)
public class CustomInfatuated {

	/**
	 * @author ethan
	 * @reason remove gender compatibility check for infatuation
	 */
	@Overwrite(remap = false)
	public static boolean infatuate(PixelmonWrapper user, PixelmonWrapper target, boolean showMessage) {
		if (target.hasStatus(new StatusType[] { StatusType.Infatuated })) {
			if (showMessage) {
				user.bc.sendToAll("pixelmon.effect.already", new Object[] { target.getNickname() });
				user.setAttackFailed();
			}
			return false;
		}

		TranslationTextComponent message = null;
		if (showMessage) {
			message = ChatHandler.getMessage("pixelmon.effect.falleninlove", new Object[] { target.getNickname() });
		}

		return target.addStatus(new Infatuated(user), user, message);
	}
}
