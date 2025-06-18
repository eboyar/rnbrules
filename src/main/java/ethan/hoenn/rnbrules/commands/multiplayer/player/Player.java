package ethan.hoenn.rnbrules.commands.multiplayer.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import ethan.hoenn.rnbrules.utils.managers.SettingsManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class Player {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("pl")
				.then(Commands.literal("mute").then(Commands.argument("player", EntityArgument.player()).executes(Player::togglePersonalMute)))
				.then(Commands.literal("unmute").then(Commands.argument("player", EntityArgument.player()).executes(Player::unmutePlayer)))
				.then(Commands.literal("listmuted").executes(Player::listMutedPlayers))
				.then(Commands.literal("muteall").executes(Player::toggleMuteAll))
		);

		dispatcher.register(
			Commands.literal("player")
				.then(Commands.literal("mute").then(Commands.argument("player", EntityArgument.player()).executes(Player::togglePersonalMute)))
				.then(Commands.literal("unmute").then(Commands.argument("player", EntityArgument.player()).executes(Player::unmutePlayer)))
				.then(Commands.literal("listmuted").executes(Player::listMutedPlayers))
				.then(Commands.literal("muteall").executes(Player::toggleMuteAll))
		);
	}

	private static int togglePersonalMute(CommandContext<CommandSource> context) {
		CommandSource source = context.getSource();

		if (!(source.getEntity() instanceof ServerPlayerEntity)) {
			source.sendFailure(new StringTextComponent("This command can only be used by players!"));
			return 0;
		}

		ServerPlayerEntity mutingPlayer = (ServerPlayerEntity) source.getEntity();

		try {
			ServerPlayerEntity targetPlayer = EntityArgument.getPlayer(context, "player");

			if (mutingPlayer.getUUID().equals(targetPlayer.getUUID())) {
				source.sendFailure(new StringTextComponent(TextFormatting.RED + "You cannot mute yourself!"));
				return 0;
			}

			SettingsManager settingsManager = SettingsManager.get();
			if (settingsManager == null) {
				source.sendFailure(new StringTextComponent(TextFormatting.RED + "Settings manager not available!"));
				return 0;
			}

			boolean isMuted = settingsManager.togglePersonalMute(mutingPlayer.getUUID(), targetPlayer.getUUID());

			if (isMuted) {
				mutingPlayer.sendMessage(
					new StringTextComponent(
						TextFormatting.GREEN + "You have muted " + TextFormatting.YELLOW + targetPlayer.getDisplayName().getString() + TextFormatting.GREEN + ". You will no longer see their chat messages."
					),
					mutingPlayer.getUUID()
				);
			} else {
				mutingPlayer.sendMessage(
					new StringTextComponent(
						TextFormatting.GREEN + "You have unmuted " + TextFormatting.YELLOW + targetPlayer.getDisplayName().getString() + TextFormatting.GREEN + ". You will now see their chat messages again."
					),
					mutingPlayer.getUUID()
				);
			}

			return 1;
		} catch (Exception e) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Error: " + e.getMessage()));
			return 0;
		}
	}

	private static int unmutePlayer(CommandContext<CommandSource> context) {
		CommandSource source = context.getSource();

		if (!(source.getEntity() instanceof ServerPlayerEntity)) {
			source.sendFailure(new StringTextComponent("This command can only be used by players!"));
			return 0;
		}

		ServerPlayerEntity mutingPlayer = (ServerPlayerEntity) source.getEntity();

		try {
			ServerPlayerEntity targetPlayer = EntityArgument.getPlayer(context, "player");

			SettingsManager settingsManager = SettingsManager.get();
			if (settingsManager == null) {
				source.sendFailure(new StringTextComponent(TextFormatting.RED + "Settings manager not available!"));
				return 0;
			}

			if (!settingsManager.isPersonallyMuted(mutingPlayer.getUUID(), targetPlayer.getUUID())) {
				source.sendFailure(new StringTextComponent(TextFormatting.RED + "You have not muted " + targetPlayer.getDisplayName().getString() + "!"));
				return 0;
			}

			settingsManager.removePersonalMute(mutingPlayer.getUUID(), targetPlayer.getUUID());

			mutingPlayer.sendMessage(
				new StringTextComponent(
					TextFormatting.GREEN + "You have unmuted " + TextFormatting.YELLOW + targetPlayer.getDisplayName().getString() + TextFormatting.GREEN + ". You will now see their chat messages again."
				),
				mutingPlayer.getUUID()
			);

			return 1;
		} catch (Exception e) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Error: " + e.getMessage()));
			return 0;
		}
	}

	private static int listMutedPlayers(CommandContext<CommandSource> context) {
		CommandSource source = context.getSource();

		if (!(source.getEntity() instanceof ServerPlayerEntity)) {
			source.sendFailure(new StringTextComponent("This command can only be used by players!"));
			return 0;
		}

		ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();

		SettingsManager settingsManager = SettingsManager.get();
		if (settingsManager == null) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Settings manager not available!"));
			return 0;
		}

		java.util.Set<java.util.UUID> mutedPlayers = settingsManager.getPersonalMutes(player.getUUID());

		if (mutedPlayers.isEmpty()) {
			player.sendMessage(new StringTextComponent(TextFormatting.YELLOW + "You have not muted any players."), player.getUUID());
			return 1;
		}

		StringBuilder message = new StringBuilder();
		message.append(TextFormatting.GREEN + "Your muted players: ");

		boolean first = true;
		for (java.util.UUID mutedUUID : mutedPlayers) {
			if (!first) {
				message.append(TextFormatting.WHITE + ", ");
			}

			ServerPlayerEntity mutedPlayer = player.getServer().getPlayerList().getPlayer(mutedUUID);
			if (mutedPlayer != null) {
				message.append(TextFormatting.YELLOW + mutedPlayer.getDisplayName().getString());
			} else {
				message.append(TextFormatting.GRAY + mutedUUID.toString().substring(0, 8) + "...");
			}
			first = false;
		}

		player.sendMessage(new StringTextComponent(message.toString()), player.getUUID());
		return 1;
	}

	private static int toggleMuteAll(CommandContext<CommandSource> context) {
		CommandSource source = context.getSource();

		if (!(source.getEntity() instanceof ServerPlayerEntity)) {
			source.sendFailure(new StringTextComponent("This command can only be used by players!"));
			return 0;
		}

		ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();

		SettingsManager settingsManager = SettingsManager.get();
		if (settingsManager == null) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "Settings manager not available!"));
			return 0;
		}

		boolean newSetting = settingsManager.toggleMuteAll(player.getUUID());

		if (newSetting) {
			player.sendMessage(
				new StringTextComponent(TextFormatting.GREEN + "Mute All " + TextFormatting.YELLOW + "enabled" + TextFormatting.GREEN + ". You will only see critical messages."),
				player.getUUID()
			);
		} else {
			player.sendMessage(
				new StringTextComponent(TextFormatting.GREEN + "Mute All " + TextFormatting.YELLOW + "disabled" + TextFormatting.GREEN + ". You will now see all chat messages."),
				player.getUUID()
			);
		}

		return 1;
	}
}
