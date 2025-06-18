package ethan.hoenn.rnbrules.mixins.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.pixelmonmod.pixelmon.client.gui.battles.VersusScreen;
import com.pixelmonmod.pixelmon.client.gui.battles.rules.TeamSelectScreen;
import com.pixelmonmod.pixelmon.comm.packetHandlers.battles.rules.selection.ShowTeamSelectPacket;
import ethan.hoenn.rnbrules.utils.data.gui.TagBattleData;
import ethan.hoenn.rnbrules.utils.data.gui.TagBattleData.PartnerInfo;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(VersusScreen.class)
public abstract class CustomVersusScreen {

	@Redirect(method = "drawEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;draw(Lcom/mojang/blaze3d/matrix/MatrixStack;Ljava/lang/String;FFI)I"))
	private int modifyTextZLevel(FontRenderer fontRenderer, MatrixStack matrixStack, String text, float x, float y, int color) {
		matrixStack.pushPose();
		matrixStack.translate(0, 0, 100);
		int result = fontRenderer.draw(matrixStack, text, x, y, color);
		matrixStack.popPose();
		return result;
	}

	@ModifyArg(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lcom/pixelmonmod/pixelmon/client/gui/battles/VersusScreen;drawEntity(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/entity/LivingEntity;Ljava/lang/String;IIIIIIIIIIIZ)V",
			ordinal = 0
		),
		index = 4
	)
	private String modifyPlayerNameArg(String originalEntityName) {
		String modifiedEntityName = originalEntityName;

		if ((Object) this instanceof TeamSelectScreen) {
			ShowTeamSelectPacket packet = TeamSelectScreen.teamSelectPacket;
			if (packet != null && packet.npcID > 0) {
				PartnerInfo partnerInfo = TagBattleData.getPartnerData(packet.npcID);
				if (partnerInfo != null && !partnerInfo.getPartnerTrainerName().isEmpty()) {
					modifiedEntityName = originalEntityName + " & " + partnerInfo.getPartnerTrainerName();
				}
			}
		}

		return modifiedEntityName;
	}
}
