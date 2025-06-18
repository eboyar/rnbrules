package ethan.hoenn.rnbrules.commands.flight;

import com.mojang.brigadier.CommandDispatcher;
import ethan.hoenn.rnbrules.gui.flight.FlyGui;
import ethan.hoenn.rnbrules.utils.enums.HiddenMachine;
import ethan.hoenn.rnbrules.utils.managers.FlyManager;
import ethan.hoenn.rnbrules.utils.managers.HiddenMachineManager;
import ethan.hoenn.rnbrules.utils.managers.SafariManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

public class Fly {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("fly").executes(context -> {
				ServerPlayerEntity player = context.getSource().getPlayerOrException();
				return executeCommand(context.getSource(), player);
			})
		);
	}

	private static int executeCommand(CommandSource source, ServerPlayerEntity player) {
		try {
			HiddenMachineManager hmManager = HiddenMachineManager.get((ServerWorld) player.level);
			if (!hmManager.hasHM(player.getUUID(), HiddenMachine.FLY.getHmId())) {
				source.sendFailure(new StringTextComponent("You don't have the Fly HM.").withStyle(TextFormatting.RED));
				return 0;
			}

			if (!FlyManager.canFly(player)) {
				player.displayClientMessage(new StringTextComponent("You must wait before flying again.").withStyle(TextFormatting.RED), false);
				return 0;
			}

			SafariManager safariManager = SafariManager.get((ServerWorld) player.level);
			if (safariManager.isPlayerInSafari(player.getUUID())) {
				player.displayClientMessage(new StringTextComponent("You cannot use Fly while in the Safari Zone.").withStyle(TextFormatting.RED), false);
				return 0;
			}

			BlockPos pos = player.blockPosition();
			if (!(player.level.canSeeSky(pos))) {
				player.displayClientMessage(new StringTextComponent("You can't take flight from where you are.").withStyle(TextFormatting.RED), false);
				return 0;
			}

			openFlyGui(player);
			return 1;
		} catch (Exception e) {
			source.sendFailure(new StringTextComponent("Error: " + e.getMessage()));
			e.printStackTrace();
			return 0;
		}
	}

	private static void openFlyGui(ServerPlayerEntity player) {
		FlyGui.openGui(player);
	}
}
