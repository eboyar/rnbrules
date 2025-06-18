package ethan.hoenn.rnbrules.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import ethan.hoenn.rnbrules.multiplayer.PlayerListHandler;
import ethan.hoenn.rnbrules.multiplayer.Rank;
import ethan.hoenn.rnbrules.multiplayer.StaffRank;
import ethan.hoenn.rnbrules.utils.managers.ProgressionManager;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

public class Progression {
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("progression")
				.requires(source -> source.hasPermission(4))
				.then(Commands.literal("reset")
						.then(Commands.argument("player", EntityArgument.player())
								.executes(Progression::executeReset)
						)
				)
				.then(Commands.literal("rank")
						.then(Commands.argument("player", EntityArgument.player())
								.then(Commands.argument("rank", StringArgumentType.string())
										.suggests(Progression::suggestRanks)
										.executes(Progression::executeSetRank)
								)
						)
				)				
				.then(Commands.literal("staff")
						.then(Commands.argument("player", EntityArgument.player())
								.then(Commands.argument("staffrank", StringArgumentType.string())
										.suggests(Progression::suggestStaffRanks)
										.executes(Progression::executeSetStaffRank)
								)
						)
						.then(Commands.literal("remove")
								.then(Commands.argument("player", EntityArgument.player())
										.executes(Progression::executeRemoveStaffRank)
								)
						)
				)
		);
	}

	private static int executeReset(CommandContext<CommandSource> context) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		ServerPlayerEntity targetPlayer = EntityArgument.getPlayer(context, "player");
		ServerWorld world = source.getLevel();
		UUID playerUUID = targetPlayer.getUUID();
		String playerName = targetPlayer.getName().getString();

		try {
			Map<String, Boolean> resetResults = ProgressionManager.globalResetPlayerData(world, playerUUID);

			source.sendSuccess(new StringTextComponent("§aPlayer data reset completed for: §e" + playerName), true);

			int mwd = 0;
			for (Map.Entry<String, Boolean> entry : resetResults.entrySet()) {
				if (entry.getValue()) {
					mwd++;
				}
			}

			source.sendSuccess(new StringTextComponent(
					String.format("§7Reset data in §e%d§7 out of §e%d§7 managers",
							mwd, resetResults.size())
			), false);

			if (targetPlayer.isAlive()) {
				targetPlayer.connection.disconnect(new StringTextComponent(
						"§cYour progression data has been reset by an administrator."
				));
			}

			return 1;
		} catch (Exception e) {
			source.sendFailure(new StringTextComponent("§cFailed to reset player data: " + e.getMessage()));
			e.printStackTrace();
			return 0;
		}
	}

	private static CompletableFuture<Suggestions> suggestRanks(CommandContext<CommandSource> context, SuggestionsBuilder builder) {
		
		builder.suggest(Rank.NEWCOMER.getId());
		builder.suggest(Rank.NOVICE.getId());
		builder.suggest(Rank.ROOKIE.getId());
		builder.suggest(Rank.JUNIOR.getId());
		builder.suggest(Rank.ADEPT.getId());
		builder.suggest(Rank.ACE.getId());
		builder.suggest(Rank.VETERAN.getId());
		builder.suggest(Rank.EXPERT.getId());
		builder.suggest(Rank.MASTER.getId());
		builder.suggest(Rank.CHAMPION.getId());
		return builder.buildFuture();
	}

	private static CompletableFuture<Suggestions> suggestStaffRanks(CommandContext<CommandSource> context, SuggestionsBuilder builder) {
		
		builder.suggest(StaffRank.STAFF.getId());
		builder.suggest(StaffRank.ADMIN.getId());
		return builder.buildFuture();
	}
	private static int executeSetRank(CommandContext<CommandSource> context) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		ServerPlayerEntity targetPlayer = EntityArgument.getPlayer(context, "player");
		String rankId = StringArgumentType.getString(context, "rank");
		UUID playerUUID = targetPlayer.getUUID();
		String playerName = targetPlayer.getName().getString();

		
		Rank rank = findRankById(rankId);
		if (rank == null) {
			source.sendFailure(new StringTextComponent("§cUnknown rank: " + rankId));
			return 0;
		}		try {
			ProgressionManager.get().setPlayerRank(playerUUID, rank);
			
			
			PlayerListHandler.updatePlayerRankDisplay(targetPlayer);
			
			source.sendSuccess(new StringTextComponent(
				"§aSet rank for §e" + playerName + "§a to §r" + rank.getFormattedPrefix()
			), true);
			
			return 1;
		} catch (Exception e) {
			source.sendFailure(new StringTextComponent("§cFailed to set rank: " + e.getMessage()));
			e.printStackTrace();
			return 0;
		}
	}
	private static int executeSetStaffRank(CommandContext<CommandSource> context) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		ServerPlayerEntity targetPlayer = EntityArgument.getPlayer(context, "player");
		String staffRankId = StringArgumentType.getString(context, "staffrank");
		UUID playerUUID = targetPlayer.getUUID();
		String playerName = targetPlayer.getName().getString();

		
		StaffRank staffRank = findStaffRankById(staffRankId);
		if (staffRank == null) {
			source.sendFailure(new StringTextComponent("§cUnknown staff rank: " + staffRankId));
			return 0;
		}		try {
			ProgressionManager.get().setPlayerStaffRank(playerUUID, staffRank);
			
			
			PlayerListHandler.updatePlayerRankDisplay(targetPlayer);
			
			source.sendSuccess(new StringTextComponent(
				"§aSet staff rank for §e" + playerName + "§a to §r" + staffRank.getNameColor() + staffRank.getDisplayName()
			), true);
			
			
			if (targetPlayer.isAlive()) {
				targetPlayer.sendMessage(new StringTextComponent(
					"§aYour staff rank has been set to §r" + staffRank.getNameColor() + staffRank.getDisplayName()
				), targetPlayer.getUUID());
			}
			
			return 1;
		} catch (Exception e) {
			source.sendFailure(new StringTextComponent("§cFailed to set staff rank: " + e.getMessage()));
			e.printStackTrace();
			return 0;
		}
	}
	private static int executeRemoveStaffRank(CommandContext<CommandSource> context) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		ServerPlayerEntity targetPlayer = EntityArgument.getPlayer(context, "player");
		UUID playerUUID = targetPlayer.getUUID();
		String playerName = targetPlayer.getName().getString();try {
			StaffRank currentRank = ProgressionManager.get().getPlayerStaffRankObject(playerUUID);
			if (currentRank == null) {
				source.sendFailure(new StringTextComponent("§e" + playerName + "§c does not have a staff rank to remove."));
				return 0;
			}			ProgressionManager.get().removePlayerStaffRank(playerUUID);
			
			
			PlayerListHandler.updatePlayerRankDisplay(targetPlayer);
			
			source.sendSuccess(new StringTextComponent(
				"§aRemoved staff rank §r" + currentRank.getNameColor() + currentRank.getDisplayName() + "§a from §e" + playerName
			), true);
			
			
			if (targetPlayer.isAlive()) {
				targetPlayer.sendMessage(new StringTextComponent(
					"§cYour staff rank has been removed."
				), targetPlayer.getUUID());
			}
			
			return 1;
		} catch (Exception e) {
			source.sendFailure(new StringTextComponent("§cFailed to remove staff rank: " + e.getMessage()));
			e.printStackTrace();
			return 0;
		}
	}

	private static Rank findRankById(String id) {
		switch (id.toLowerCase()) {
			case "newcomer":
				return Rank.NEWCOMER;
			case "novice":
				return Rank.NOVICE;
			case "rookie":
				return Rank.ROOKIE;
			case "junior":
				return Rank.JUNIOR;
			case "adept":
				return Rank.ADEPT;
			case "ace":
				return Rank.ACE;
			case "veteran":
				return Rank.VETERAN;
			case "expert":
				return Rank.EXPERT;
			case "master":
				return Rank.MASTER;
			case "champion":
				return Rank.CHAMPION;
			default:
				return null;
		}
	}

	private static StaffRank findStaffRankById(String id) {
		switch (id.toLowerCase()) {
			case "staff":
				return StaffRank.STAFF;
			case "admin":
				return StaffRank.ADMIN;
			default:
				return null;
		}
	}
}