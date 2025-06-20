package ethan.hoenn.rnbrules.mixins;

import com.pixelmonmod.pixelmon.api.util.helpers.AIHelper;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import ethan.hoenn.rnbrules.ai.goal.CustomFlyingGoal;
import ethan.hoenn.rnbrules.ai.goal.ReturnToSpawnPointGoal;
import ethan.hoenn.rnbrules.ai.goal.SwimToSpawnPointGoal;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AIHelper.class)
public class CustomAIHelper {

	@Shadow(remap = false)
	int i;

	@Inject(
		method = "initGroundAI(Lcom/pixelmonmod/pixelmon/entities/pixelmon/PixelmonEntity;Lnet/minecraft/entity/ai/goal/GoalSelector;)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/entity/ai/goal/Goal;)V", ordinal = 1, shift = At.Shift.BEFORE)
	)
	private void injectGroundReturnToSpawnGoal(PixelmonEntity entity, GoalSelector tasks, CallbackInfo ci) {
		int needsSpawnPoint = entity.getPokemon().getPersistentData().getInt("NeedsSpawnPoint");
		if (needsSpawnPoint != 0) {
			tasks.addGoal(1, new ReturnToSpawnPointGoal(entity, entity.getAttribute(Attributes.MOVEMENT_SPEED).getValue(), needsSpawnPoint));
		}
	}

	@Inject(
		method = "initSwimmingAI(Lcom/pixelmonmod/pixelmon/entities/pixelmon/PixelmonEntity;Lnet/minecraft/entity/ai/goal/GoalSelector;)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/entity/ai/goal/Goal;)V", ordinal = 0, shift = At.Shift.BEFORE)
	)
	private void injectSwimToSpawnGoal(PixelmonEntity entity, GoalSelector tasks, CallbackInfo ci) {
		int needsSpawnPoint = entity.getPokemon().getPersistentData().getInt("NeedsSpawnPoint");
		if (needsSpawnPoint != 0) {
			tasks.addGoal(1, new SwimToSpawnPointGoal(entity, entity.getAttribute(Attributes.MOVEMENT_SPEED).getValue(), needsSpawnPoint));
		}
	}

	@Redirect(
		method = "initFlyingAI(Lcom/pixelmonmod/pixelmon/entities/pixelmon/PixelmonEntity;Lnet/minecraft/entity/ai/goal/GoalSelector;)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/entity/ai/goal/Goal;)V", ordinal = 4)
	)
	private void replaceFlyingGoal(GoalSelector tasks, int priority, net.minecraft.entity.ai.goal.Goal goal, PixelmonEntity pixelmon) {
		tasks.addGoal(priority, new CustomFlyingGoal(pixelmon));
	}
}
