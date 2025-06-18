package ethan.hoenn.rnbrules.commands.multiplayer.staff;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import ethan.hoenn.rnbrules.multiplayer.StaffRank;
import ethan.hoenn.rnbrules.utils.managers.ProgressionManager;
import ethan.hoenn.rnbrules.utils.managers.SettingsManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.MessageArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.management.BanList;
import net.minecraft.server.management.IPBanEntry;
import net.minecraft.server.management.IPBanList;
import net.minecraft.server.management.ProfileBanEntry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class Staff {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("st")
				.requires(Staff::hasStaffPermission)
				.then(
					Commands.literal("ban").then(
						Commands.argument("player", EntityArgument.player()).then(
							Commands.argument("reason", MessageArgument.message()).executes(context -> banPlayer(context, false)).then(Commands.literal("ip").executes(context -> banPlayer(context, true)))
						)
					)
				)
				.then(Commands.literal("kick").then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("reason", MessageArgument.message()).executes(Staff::kickPlayer))))
				.then(Commands.literal("mute").then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("reason", MessageArgument.message()).executes(Staff::mutePlayer))))
				.then(
					Commands.literal("tempmute").then(
						Commands.argument("player", EntityArgument.player()).then(
							Commands.argument("reason", MessageArgument.message()).then(Commands.argument("time", StringArgumentType.greedyString()).executes(Staff::tempMutePlayer))
						)
					)
				)
				.then(
					Commands.literal("tempban").then(
						Commands.argument("player", EntityArgument.player()).then(
							Commands.argument("reason", MessageArgument.message()).then(Commands.argument("time", StringArgumentType.greedyString()).executes(Staff::tempBanPlayer))
						)
					)
				)
				.then(Commands.literal("unmute").then(Commands.argument("player", EntityArgument.player()).executes(Staff::unmutePlayer)))
		);

		dispatcher.register(Commands.literal("staff").requires(Staff::hasStaffPermission).redirect(dispatcher.getRoot().getChild("st")));
	}

	private static boolean hasStaffPermission(CommandSource source) {
		if (!(source.getEntity() instanceof ServerPlayerEntity)) {
			return false;
		}

		ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
		ProgressionManager progressionManager = ProgressionManager.get();

		if (progressionManager == null) {
			return false;
		}

		StaffRank staffRank = progressionManager.getPlayerStaffRankObject(player.getUUID());
		return staffRank != null;
	}

	private static int banPlayer(CommandContext<CommandSource> context, boolean ipBan) {
		try {
			ServerPlayerEntity staff = (ServerPlayerEntity) context.getSource().getEntity();
			ServerPlayerEntity target = EntityArgument.getPlayer(context, "player");
			ITextComponent reason = MessageArgument.getMessage(context, "reason");

			if (!canModeratePlayer(staff, target)) {
				context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + "You cannot ban a player with equal or higher staff rank!"));
				return 0;
			}

			BanList banList = context.getSource().getServer().getPlayerList().getBans();
			ProfileBanEntry banEntry = new ProfileBanEntry(target.getGameProfile(), null, staff.getDisplayName().getString(), null, reason.getString());

			banList.add(banEntry);

			if (ipBan) {
				IPBanList ipBanList = context.getSource().getServer().getPlayerList().getIpBans();
				IPBanEntry ipBanEntry = new IPBanEntry(target.getIpAddress(), null, staff.getDisplayName().getString(), null, reason.getString());
				ipBanList.add(ipBanEntry);
			}

			target.connection.disconnect(new TranslationTextComponent("multiplayer.disconnect.banned"));

			String banType = ipBan ? "IP-banned" : "banned";
			broadcastToStaff(
				context.getSource().getServer(),
				TextFormatting.RED + staff.getDisplayName().getString() + " " + banType + " " + target.getDisplayName().getString() + " for: " + reason.getString()
			);

			context.getSource().sendSuccess(new StringTextComponent(TextFormatting.GREEN + "Successfully " + banType + " " + target.getDisplayName().getString()), true);

			return 1;
		} catch (Exception e) {
			context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + "Error executing ban command: " + e.getMessage()));
			return 0;
		}
	}

	private static int kickPlayer(CommandContext<CommandSource> context) {
		try {
			ServerPlayerEntity staff = (ServerPlayerEntity) context.getSource().getEntity();
			ServerPlayerEntity target = EntityArgument.getPlayer(context, "player");
			ITextComponent reason = MessageArgument.getMessage(context, "reason");

			if (!canModeratePlayer(staff, target)) {
				context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + "You cannot kick a player with equal or higher staff rank!"));
				return 0;
			}

			target.connection.disconnect(new StringTextComponent("Kicked by " + staff.getDisplayName().getString() + ": " + reason.getString()));

			broadcastToStaff(context.getSource().getServer(), TextFormatting.YELLOW + staff.getDisplayName().getString() + " kicked " + target.getDisplayName().getString() + " for: " + reason.getString());

			context.getSource().sendSuccess(new StringTextComponent(TextFormatting.GREEN + "Successfully kicked " + target.getDisplayName().getString()), true);

			return 1;
		} catch (Exception e) {
			context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + "Error executing kick command: " + e.getMessage()));
			return 0;
		}
	}

	private static int mutePlayer(CommandContext<CommandSource> context) {
		try {
			ServerPlayerEntity staff = (ServerPlayerEntity) context.getSource().getEntity();
			ServerPlayerEntity target = EntityArgument.getPlayer(context, "player");
			ITextComponent reason = MessageArgument.getMessage(context, "reason");

			if (!canModeratePlayer(staff, target)) {
				context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + "You cannot mute a player with equal or higher staff rank!"));
				return 0;
			}

			SettingsManager settingsManager = SettingsManager.get();
			if (settingsManager == null) {
				context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + "Settings manager not available!"));
				return 0;
			}

			settingsManager.mutePlayerPermanent(target.getUUID(), reason.getString());

			target.sendMessage(
				new StringTextComponent(TextFormatting.RED + "You have been permanently muted by " + staff.getDisplayName().getString() + ". Reason: " + reason.getString()),
				target.getUUID()
			);

			broadcastToStaff(
				context.getSource().getServer(),
				TextFormatting.RED + staff.getDisplayName().getString() + " permanently muted " + target.getDisplayName().getString() + " for: " + reason.getString()
			);

			context.getSource().sendSuccess(new StringTextComponent(TextFormatting.GREEN + "Successfully muted " + target.getDisplayName().getString()), true);

			return 1;
		} catch (Exception e) {
			context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + "Error executing mute command: " + e.getMessage()));
			return 0;
		}
	}

	private static int tempMutePlayer(CommandContext<CommandSource> context) {
		try {
			ServerPlayerEntity staff = (ServerPlayerEntity) context.getSource().getEntity();
			ServerPlayerEntity target = EntityArgument.getPlayer(context, "player");
			ITextComponent reason = MessageArgument.getMessage(context, "reason");
			String timeString = StringArgumentType.getString(context, "time");

			if (!canModeratePlayer(staff, target)) {
				context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + "You cannot mute a player with equal or higher staff rank!"));
				return 0;
			}

			SettingsManager settingsManager = SettingsManager.get();
			if (settingsManager == null) {
				context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + "Settings manager not available!"));
				return 0;
			}

			long duration = SettingsManager.parseTimeString(timeString);
			if (duration <= 0) {
				context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + "Invalid time format! Use format like '7h5d81m'"));
				return 0;
			}

			long expiryTime = System.currentTimeMillis() + duration;
			settingsManager.mutePlayer(target.getUUID(), reason.getString(), expiryTime);

			String durationStr = formatTime(duration);
			target.sendMessage(
				new StringTextComponent(TextFormatting.RED + "You have been muted for " + durationStr + " by " + staff.getDisplayName().getString() + ". Reason: " + reason.getString()),
				target.getUUID()
			);

			broadcastToStaff(
				context.getSource().getServer(),
				TextFormatting.RED + staff.getDisplayName().getString() + " muted " + target.getDisplayName().getString() + " for " + durationStr + ". Reason: " + reason.getString()
			);

			context.getSource().sendSuccess(new StringTextComponent(TextFormatting.GREEN + "Successfully muted " + target.getDisplayName().getString() + " for " + durationStr), true);

			return 1;
		} catch (Exception e) {
			context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + "Error executing tempmute command: " + e.getMessage()));
			return 0;
		}
	}

	private static int tempBanPlayer(CommandContext<CommandSource> context) {
		try {
			ServerPlayerEntity staff = (ServerPlayerEntity) context.getSource().getEntity();
			ServerPlayerEntity target = EntityArgument.getPlayer(context, "player");
			ITextComponent reason = MessageArgument.getMessage(context, "reason");
			String timeString = StringArgumentType.getString(context, "time");

			if (!canModeratePlayer(staff, target)) {
				context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + "You cannot ban a player with equal or higher staff rank!"));
				return 0;
			}

			SettingsManager settingsManager = SettingsManager.get();
			if (settingsManager == null) {
				context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + "Settings manager not available!"));
				return 0;
			}

			long duration = SettingsManager.parseTimeString(timeString);
			if (duration <= 0) {
				context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + "Invalid time format! Use format like '7h5d81m'"));
				return 0;
			}

			long expiryTime = System.currentTimeMillis() + duration;
			settingsManager.tempBanPlayer(target.getUUID(), reason.getString(), expiryTime);

			String durationStr = formatTime(duration);

			target.connection.disconnect(new StringTextComponent("Temporarily banned for " + durationStr + " by " + staff.getDisplayName().getString() + ". Reason: " + reason.getString()));

			broadcastToStaff(
				context.getSource().getServer(),
				TextFormatting.RED + staff.getDisplayName().getString() + " temporarily banned " + target.getDisplayName().getString() + " for " + durationStr + ". Reason: " + reason.getString()
			);

			context.getSource().sendSuccess(new StringTextComponent(TextFormatting.GREEN + "Successfully banned " + target.getDisplayName().getString() + " for " + durationStr), true);

			return 1;
		} catch (Exception e) {
			context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + "Error executing tempban command: " + e.getMessage()));
			return 0;
		}
	}

	private static int unmutePlayer(CommandContext<CommandSource> context) {
		try {
			ServerPlayerEntity staff = (ServerPlayerEntity) context.getSource().getEntity();
			ServerPlayerEntity target = EntityArgument.getPlayer(context, "player");

			SettingsManager settingsManager = SettingsManager.get();
			if (settingsManager == null) {
				context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + "Settings manager not available!"));
				return 0;
			}

			if (!settingsManager.isPlayerMuted(target.getUUID())) {
				context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + target.getDisplayName().getString() + " is not muted!"));
				return 0;
			}

			settingsManager.unmutePlayer(target.getUUID());

			target.sendMessage(new StringTextComponent(TextFormatting.GREEN + "You have been unmuted by " + staff.getDisplayName().getString()), target.getUUID());

			broadcastToStaff(context.getSource().getServer(), TextFormatting.GREEN + staff.getDisplayName().getString() + " unmuted " + target.getDisplayName().getString());

			context.getSource().sendSuccess(new StringTextComponent(TextFormatting.GREEN + "Successfully unmuted " + target.getDisplayName().getString()), true);

			return 1;
		} catch (Exception e) {
			context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + "Error executing unmute command: " + e.getMessage()));
			return 0;
		}
	}

	private static boolean canModeratePlayer(ServerPlayerEntity staff, ServerPlayerEntity target) {
		ProgressionManager progressionManager = ProgressionManager.get();
		if (progressionManager == null) {
			return false;
		}

		StaffRank staffRank = progressionManager.getPlayerStaffRankObject(staff.getUUID());
		StaffRank targetRank = progressionManager.getPlayerStaffRankObject(target.getUUID());

		if (targetRank == null) {
			return true;
		}

		return staffRank.getPriority() > targetRank.getPriority();
	}

	private static void broadcastToStaff(net.minecraft.server.MinecraftServer server, String message) {
		ProgressionManager progressionManager = ProgressionManager.get();
		if (progressionManager == null) {
			return;
		}

		for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
			StaffRank staffRank = progressionManager.getPlayerStaffRankObject(player.getUUID());
			if (staffRank != null) {
				player.sendMessage(new StringTextComponent(message), player.getUUID());
			}
		}
	}

	private static String formatTime(long milliseconds) {
		if (milliseconds <= 0) {
			return "0 seconds";
		}

		long seconds = milliseconds / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long days = hours / 24;

		StringBuilder result = new StringBuilder();

		if (days > 0) {
			result.append(days).append(" day").append(days > 1 ? "s" : "");
		}
		if (hours % 24 > 0) {
			if (result.length() > 0) result.append(", ");
			result.append(hours % 24).append(" hour").append(hours % 24 > 1 ? "s" : "");
		}
		if (minutes % 60 > 0) {
			if (result.length() > 0) result.append(", ");
			result.append(minutes % 60).append(" minute").append(minutes % 60 > 1 ? "s" : "");
		}
		if (seconds % 60 > 0 && result.length() == 0) {
			result.append(seconds % 60).append(" second").append(seconds % 60 > 1 ? "s" : "");
		}

		return result.toString();
	}
}
