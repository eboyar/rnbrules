package ethan.hoenn.rnbrules.mixins.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.pixelmonmod.pixelmon.client.gui.battles.rules.TeamSelectScreen;
import com.pixelmonmod.pixelmon.comm.packetHandlers.battles.rules.selection.ShowTeamSelectPacket;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import ethan.hoenn.rnbrules.utils.data.gui.LinkedTrainerData;
import ethan.hoenn.rnbrules.utils.data.gui.LinkedTrainerData.TrainerLinkInfo;
import ethan.hoenn.rnbrules.utils.data.gui.TagBattleData;
import ethan.hoenn.rnbrules.utils.data.gui.TagBattleData.PartnerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TeamSelectScreen.class)
public class CustomTeamSelectScreen {

	@Shadow(remap = false)
	private NPCTrainer trainer;

	@Shadow(remap = false)
	public static ShowTeamSelectPacket teamSelectPacket;

	@Inject(method = "<init>", at = @At("RETURN"), remap = false)
	private void onConstructed(CallbackInfo ci) {
		if (this.trainer == null || teamSelectPacket == null) {
			return;
		}

		TrainerLinkInfo linkInfo = LinkedTrainerData.getLinkedTrainerData(this.trainer.getId());

		if (linkInfo != null && linkInfo.hasLinked()) {
			String originalName = this.trainer.getName("en_us");

			if (!linkInfo.getLinkedTrainerName().isEmpty()) {
				if (teamSelectPacket.npcName.equals(originalName)) {
					teamSelectPacket.npcName = originalName + " & " + linkInfo.getLinkedTrainerName();
				}

				if (!teamSelectPacket.showOpponentTeam) {
					teamSelectPacket.opponentSize = Math.min(teamSelectPacket.opponentSize + linkInfo.getLinkedTeamSize(), 6);
				}
			}
		}

		PartnerInfo partnerInfo = TagBattleData.getPartnerData(this.trainer.getId());

		if (partnerInfo != null && !partnerInfo.getPartnerTrainerName().isEmpty()) {
			if (!teamSelectPacket.showOpponentTeam) {
				//todo: display pokemon of tag partner?
			}
		}
	}

	@Inject(
		method = "render(Lcom/mojang/blaze3d/matrix/MatrixStack;IIF)V",
		at = @At(value = "INVOKE", target = "Lcom/pixelmonmod/pixelmon/client/gui/battles/VersusScreen;render(Lcom/mojang/blaze3d/matrix/MatrixStack;IIF)V", shift = At.Shift.AFTER)
	)
	private void modifyNameTextZLevel(MatrixStack matrix, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		matrix.pushPose();
		matrix.translate(0, 0, 100);
	}

	@Inject(
		method = "render(Lcom/mojang/blaze3d/matrix/MatrixStack;IIF)V",
		at = @At(value = "INVOKE", target = "Lcom/pixelmonmod/pixelmon/client/gui/battles/VersusScreen;render(Lcom/mojang/blaze3d/matrix/MatrixStack;IIF)V", shift = At.Shift.BEFORE)
	)
	private void resetZLevel(MatrixStack matrix, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		matrix.popPose();
	}
}
