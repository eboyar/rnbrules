package ethan.hoenn.rnbrules.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ethan.hoenn.rnbrules.blocks.LocationBlockTileEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class SetLocationCommand {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("setlocation")
			.requires(source -> source.hasPermission(2))
			.then(Commands.argument("name", StringArgumentType.greedyString()).executes(context -> setLocationName(context, StringArgumentType.getString(context, "name"))));

		dispatcher.register(command);
	}

	private static int setLocationName(CommandContext<CommandSource> context, String locationName) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayerOrException();
		World world = player.level;

		Vector3d eyePosition = player.getEyePosition(1.0F);
		Vector3d lookVector = player.getLookAngle();
		Vector3d reachVector = eyePosition.add(lookVector.x * 5, lookVector.y * 5, lookVector.z * 5);

		BlockRayTraceResult rayTrace = world.clip(new RayTraceContext(eyePosition, reachVector, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, player));

		BlockPos blockPos = rayTrace.getBlockPos();
		TileEntity tileEntity = world.getBlockEntity(blockPos);

		if (tileEntity instanceof LocationBlockTileEntity) {
			LocationBlockTileEntity locationBlock = (LocationBlockTileEntity) tileEntity;
			String oldName = locationBlock.getLocationName();

			locationBlock.setLocationName(locationName);

			context
				.getSource()
				.sendSuccess(
					new StringTextComponent(
						TextFormatting.GREEN + "[Admin] Location name changed from " + TextFormatting.YELLOW + oldName + TextFormatting.GREEN + " to " + TextFormatting.YELLOW + locationName
					),
					true
				);

			return 1;
		} else {
			context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + "You must be looking at a Location Block to use this command."));
			return 0;
		}
	}
}
