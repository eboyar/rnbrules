package ethan.hoenn.rnbrules.commands.badge;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import ethan.hoenn.rnbrules.utils.enums.Badge;
import ethan.hoenn.rnbrules.utils.managers.BadgeManager;
import java.util.UUID;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

public class AddBadge {

	private static final SuggestionProvider<CommandSource> BADGE_SUGGESTIONS = (context, builder) -> {
		for (Badge badge : Badge.values()) {
			builder.suggest(badge.getBadgeId());
		}
		return builder.buildFuture();
	};

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("addbadge")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("player", EntityArgument.player()).then(
						Commands.argument("badge", StringArgumentType.string())
							.suggests(BADGE_SUGGESTIONS)
							.executes(context -> {
								ServerPlayerEntity player = EntityArgument.getPlayer(context, "player");
								String badgeId = StringArgumentType.getString(context, "badge");

								Badge badge = Badge.fromString(badgeId);
								if (badge != null) {
									BadgeManager badgeManager = BadgeManager.get((ServerWorld) player.level);
									UUID playerUUID = player.getUUID();

									if (!badgeManager.hasBadge(playerUUID, badge.getBadgeId())) {
										badgeManager.addBadge(playerUUID, badge.getBadgeId());
										context.getSource().sendSuccess(new StringTextComponent("Added " + badge.getBadgeId() + " to " + player.getDisplayName().getString()), true);
									} else {
										context.getSource().sendFailure(new StringTextComponent(player.getDisplayName().getString() + " already has the " + badge.getBadgeId() + "."));
									}
								} else {
									context.getSource().sendFailure(new StringTextComponent("Invalid badge ID: " + badgeId));
								}
								return 1;
							})
					)
				)
		);
	}
}
