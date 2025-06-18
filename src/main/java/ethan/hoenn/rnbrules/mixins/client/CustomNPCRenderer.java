package ethan.hoenn.rnbrules.mixins.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.pixelmonmod.pixelmon.client.render.entity.renderers.NPCRenderer;
import com.pixelmonmod.pixelmon.entities.npcs.NPCEntity;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NPCRenderer.class)
public abstract class CustomNPCRenderer {

	@Unique
	private static final double PROXIMITY_THRESHOLD = 2.0;

	@Unique
	private static final WeakHashMap<NPCTrainer, Boolean> rnbrules$sTCache = new WeakHashMap<>();

	@Invoker(value = "renderLivingLabel", remap = false)
	public abstract void callRenderLivingLabel(
		NPCEntity npc,
		String displayText,
		String subtitleText,
		float entityYaw,
		float partialTicks,
		MatrixStack matrix,
		IRenderTypeBuffer buffer,
		int packedLight
	);

	@Inject(
		method = "drawNameTag",
		at = @At(
			value = "INVOKE",
			target = "Lcom/pixelmonmod/pixelmon/client/render/entity/renderers/NPCRenderer;renderLivingLabel(Lcom/pixelmonmod/pixelmon/entities/npcs/NPCEntity;Ljava/lang/String;Ljava/lang/String;FFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V"
		),
		cancellable = true,
		remap = false
	)
	private void onDrawNameTag(NPCEntity npc, float entityYaw, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int packedLight, CallbackInfo ci) {
		if (npc instanceof NPCTrainer) {
			NPCTrainer trainer = (NPCTrainer) npc;
			String displayName = trainer.getName().getString();

			CompoundNBT persistentData = trainer.getPersistentData();
			if (persistentData.contains("CopyTrainer") && persistentData.getBoolean("CopyTrainer")) {
				ci.cancel();
				return;
			}

			List<NPCTrainer> nearbyTrainers = rnbrules$findNearbyNPCTrainers(trainer);

			if (nearbyTrainers.size() > 1 && !trainer.equals(nearbyTrainers.get(0))) {
				ci.cancel();
				return;
			}

			if (nearbyTrainers.size() > 1) {
				try {
					rnbrules$renderSideBySideNametag(nearbyTrainers, entityYaw, partialTicks, matrix, buffer, packedLight);
					ci.cancel();
					return;
				} catch (Exception ignored) {}
			}

			callRenderLivingLabel(npc, displayName, "", entityYaw, partialTicks, matrix, buffer, packedLight);
			ci.cancel();
		}
	}

	@Unique
	private List<NPCTrainer> rnbrules$findNearbyNPCTrainers(NPCTrainer trainer) {
		List<NPCTrainer> nearbyTrainers = new ArrayList<>();
		nearbyTrainers.add(trainer);

		double searchRadius = PROXIMITY_THRESHOLD * 2.0;
		AxisAlignedBB searchBox = new AxisAlignedBB(
			trainer.getX() - searchRadius,
			trainer.getY() - 2,
			trainer.getZ() - searchRadius,
			trainer.getX() + searchRadius,
			trainer.getY() + 4,
			trainer.getZ() + searchRadius
		);

		if (Minecraft.getInstance().level != null) {
			List<NPCTrainer> foundTrainers = new ArrayList<>();
			for (Entity entity : Minecraft.getInstance().level.getEntities(trainer, searchBox)) {
				if (entity instanceof NPCTrainer && !entity.equals(trainer)) {
					NPCTrainer otherTrainer = (NPCTrainer) entity;
					double dx = trainer.getX() - otherTrainer.getX();
					double dz = trainer.getZ() - otherTrainer.getZ();
					double horizontalDistSq = dx * dx + dz * dz;
					if (horizontalDistSq < PROXIMITY_THRESHOLD * PROXIMITY_THRESHOLD) {
						foundTrainers.add(otherTrainer);
					}
				}
			}
			nearbyTrainers.addAll(foundTrainers);
		}
		nearbyTrainers.sort(Comparator.comparingInt(Entity::getId));
		return nearbyTrainers;
	}

	@Unique
	private void rnbrules$renderSideBySideNametag(List<NPCTrainer> trainers, float entityYaw, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int packedLight) {
		matrix.pushPose();

		try {
			NPCTrainer trainer1 = trainers.get(0);
			NPCTrainer trainer2 = trainers.get(1);

			String name1 = trainer1.getName().getString();
			String name2 = trainer2.getName().getString();

			String combinedDisplayName;
			if (name1.equals(name2)) {
				combinedDisplayName = name1;
			} else {
				combinedDisplayName = name1 + " & " + name2;
			}

			double t1X = trainer1.getX();
			double t1Y = trainer1.getY();
			double t1Z = trainer1.getZ();
			float h1 = trainer1.getBbHeight() + 0.5F;

			double t2X = trainer2.getX();
			double t2Y = trainer2.getY();
			double t2Z = trainer2.getZ();
			float h2 = trainer2.getBbHeight() + 0.5F;

			double y1 = t1Y + h1;
			double y2 = t2Y + h2;

			boolean trainer1IsShort = rnbrules$isSTCached(trainer1);
			boolean trainer2IsShort = rnbrules$isSTCached(trainer2);

			float extraOffset = 0.0F;
			if (trainer1IsShort && !trainer2IsShort) {
				extraOffset = 0.35F;
			} else if (!trainer1IsShort && trainer2IsShort) {
				extraOffset = 0.35F;
			}

			double tY = Math.max(y1, y2) - (t1Y + h1) + extraOffset;
			double tX = (t2X - t1X) / 2.0;
			double tZ = (t2Z - t1Z) / 2.0;

			matrix.translate(tX, tY, tZ);

			callRenderLivingLabel(trainer1, combinedDisplayName, "", entityYaw, partialTicks, matrix, buffer, packedLight);
		} catch (Exception ignored) {} finally {
			matrix.popPose();
		}
	}

	@Unique
	private boolean rnbrules$isSTCached(NPCTrainer trainer) {
		return rnbrules$sTCache.computeIfAbsent(trainer, t -> rnbrules$isShortTrainer(t.getName().getString()));
	}

	@Unique
	private boolean rnbrules$isShortTrainer(String trainerName) {
		String[] shortTrainerTypes = { "Youngster", "Lass", "Ninja Boy", "Tuber" };
		for (String shortType : shortTrainerTypes) {
			if (trainerName.contains(shortType)) {
				return true;
			}
		}
		return false;
	}
}
