package ethan.hoenn.rnbrules.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.client.gui.override.multiplayer.CustomServerList;
import ethan.hoenn.rnbrules.utils.managers.LeagueManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class GeneralTestCommand {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("testcommand")
				.requires(source -> source.hasPermission(2))
				.executes(context -> {
					CommandSource source = context.getSource();
					ServerPlayerEntity player = source.getPlayerOrException();
					ServerWorld world = player.getLevel();
					BlockPos origin = player.blockPosition();

					PlayerPartyStorage party = StorageProxy.getParty(player);
					party.heal();

					LeagueManager.initLeagueBattle(player, LeagueManager.LeagueMember.SIDNEY, false);

					return 1;
				})
		);
	}
}
