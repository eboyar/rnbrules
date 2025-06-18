package ethan.hoenn.rnbrules.commands.battledependency;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.pixelmonmod.pixelmon.blocks.tileentity.PokeChestTileEntity;
import ethan.hoenn.rnbrules.utils.managers.BattleDependencyManager;
import java.util.Set;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

public class LootAddBattleDep {

	private static final SuggestionProvider<CommandSource> DEPENDENCY_SUGGESTIONS = (context, builder) -> {
		try {
			ServerPlayerEntity player = context.getSource().getPlayerOrException();
			BattleDependencyManager manager = BattleDependencyManager.get(player.getLevel());
			Set<String> dependencies = manager.getAllDependencyIds();
			return ISuggestionProvider.suggest(dependencies, builder);
		} catch (Exception e) {
			return builder.buildFuture();
		}
	};

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("lootaddbattledep")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("depId", StringArgumentType.string()).suggests(DEPENDENCY_SUGGESTIONS).executes(context -> addDependencyToChest(context, StringArgumentType.getString(context, "depId")))
				)
		);
	}

	private static int addDependencyToChest(CommandContext<CommandSource> context, String depId) {
		CommandSource source = context.getSource();

		if (!(source.getEntity() instanceof ServerPlayerEntity)) {
			source.sendFailure(new StringTextComponent("This command must be executed by a player"));
			return 0;
		}

		ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
		ServerWorld world = player.getLevel();
		BattleDependencyManager depManager = BattleDependencyManager.get(world);

		if (!depManager.dependencyExists(depId)) {
			source.sendFailure(new StringTextComponent("The dependency '" + depId + "' does not exist"));
			return 0;
		}

		Vector3d eyePos = player.getEyePosition(1.0F);
		Vector3d lookVec = player.getViewVector(1.0F);
		double reach = 5.0;
		Vector3d endPos = eyePos.add(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);

		BlockRayTraceResult rayTrace = world.clip(new RayTraceContext(eyePos, endPos, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, player));

		BlockPos pos = rayTrace.getBlockPos();
		TileEntity tileEntity = world.getBlockEntity(pos);

		if (!(tileEntity instanceof PokeChestTileEntity)) {
			source.sendFailure(new StringTextComponent("You must be looking at a PokeChest"));
			return 0;
		}

		PokeChestTileEntity chest = (PokeChestTileEntity) tileEntity;

		if (depManager.addDependencyToPokeChest(chest, depId)) {
			String depDescription = depManager.getDependency(depId).getDescription();
			source.sendSuccess(new StringTextComponent("Added dependency '" + depId + "' (" + depDescription + ") to the PokeChest"), true);
			return 1;
		} else {
			source.sendFailure(new StringTextComponent("The PokeChest already has this dependency"));
			return 0;
		}
	}
}
