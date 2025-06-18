package ethan.hoenn.rnbrules.commands.badge;

import com.mojang.brigadier.CommandDispatcher;
import ethan.hoenn.rnbrules.utils.managers.BadgeManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

public class ResetBadges {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("resetbadges")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("player", EntityArgument.player()).executes(context -> {
						ServerPlayerEntity player = EntityArgument.getPlayer(context, "player");
						ServerWorld world = player.getLevel();
						BadgeManager badgeManager = BadgeManager.get(world);

						badgeManager.resetBadges(player.getUUID());

						context.getSource().sendSuccess(new StringTextComponent("Reset all badges for " + player.getName().getString()), true);
						return 1;
					})
				)
		);
	}
}
