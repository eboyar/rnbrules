package ethan.hoenn.rnbrules.commands.encounters;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import ethan.hoenn.rnbrules.utils.managers.EncounterManager;
import ethan.hoenn.rnbrules.utils.managers.GlobalOTManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class ForcedEncounter {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("forcedencounter")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("player", EntityArgument.player()).then(
						Commands.argument("encounterID", StringArgumentType.string()).executes(context -> {
							CommandSource source = context.getSource();
							ServerPlayerEntity player = EntityArgument.getPlayer(context, "player");
							String encounterID = StringArgumentType.getString(context, "encounterID");
							GlobalOTManager globalot = GlobalOTManager.get(player.getLevel());

							EncounterManager em = EncounterManager.get(player.getLevel());

							if (globalot.playerHasGlobalOT(player.getUUID(), encounterID)) {
								source.sendFailure(new StringTextComponent(TextFormatting.RED + "Failed to start encounter '" + encounterID + "'. The player has had this encounter before."));
								return 0;
							}

							boolean success = EncounterManager.startForcedEncounterBattle(player, em.createForcedEncounter(encounterID));

							if (success) {
								globalot.addGlobalOT(encounterID);
								globalot.addPlayerGlobalOT(player.getUUID(), encounterID);
								source.sendSuccess(new StringTextComponent(TextFormatting.GREEN + "Started encounter '" + encounterID + "' for " + player.getDisplayName().getString()), true);
							} else {
								source.sendFailure(new StringTextComponent(TextFormatting.RED + "Failed to start encounter '" + encounterID + "'. The player might not have any available Pokémon."));
							}

							return success ? 1 : 0;
						})
					)
				)
		);
	}
}
