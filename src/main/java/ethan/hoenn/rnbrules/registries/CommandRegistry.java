package ethan.hoenn.rnbrules.registries;

import com.mojang.brigadier.CommandDispatcher;
import ethan.hoenn.rnbrules.commands.*;
import ethan.hoenn.rnbrules.commands.HM.AddHM;
import ethan.hoenn.rnbrules.commands.HM.ListHM;
import ethan.hoenn.rnbrules.commands.HM.RemoveHM;
import ethan.hoenn.rnbrules.commands.badge.AddBadge;
import ethan.hoenn.rnbrules.commands.badge.ResetBadges;
import ethan.hoenn.rnbrules.commands.battledependency.AddBattleDep;
import ethan.hoenn.rnbrules.commands.battledependency.AddPlayerDep;
import ethan.hoenn.rnbrules.commands.battledependency.ListBattleDeps;
import ethan.hoenn.rnbrules.commands.battledependency.LootAddBattleDep;
import ethan.hoenn.rnbrules.commands.battledependency.NPCAddBattleDep;
import ethan.hoenn.rnbrules.commands.battledependency.NPCRemoveBattleDeps;
import ethan.hoenn.rnbrules.commands.battledependency.RemovePlayerDep;
import ethan.hoenn.rnbrules.commands.battledependency.SetBattleDepDescription;
import ethan.hoenn.rnbrules.commands.bikeshop.NPCAddBikeshop;
import ethan.hoenn.rnbrules.commands.bikeshop.NPCRemoveBikeshop;
import ethan.hoenn.rnbrules.commands.dialogue.DialogueYaml;
import ethan.hoenn.rnbrules.commands.dialogue.NPCAddDialogue;
import ethan.hoenn.rnbrules.commands.dialogue.NPCAddDialogues;
import ethan.hoenn.rnbrules.commands.dialogue.NPCClearCompletedDialogue;
import ethan.hoenn.rnbrules.commands.dialogue.NPCListDialogues;
import ethan.hoenn.rnbrules.commands.dialogue.NPCRemoveAllDialogues;
import ethan.hoenn.rnbrules.commands.dialogue.NPCRemoveDialogue;
import ethan.hoenn.rnbrules.commands.dialogue.NPCRemoveDialogueSpecific;
import ethan.hoenn.rnbrules.commands.encounters.ForcedEncounter;
import ethan.hoenn.rnbrules.commands.encounters.ResetRoamerData;
import ethan.hoenn.rnbrules.commands.encounters.SetForcedEncounter;
import ethan.hoenn.rnbrules.commands.encounters.ToggleRoamers;
import ethan.hoenn.rnbrules.commands.environment.ListLocationEnvironments;
import ethan.hoenn.rnbrules.commands.environment.SetEnvironment;
import ethan.hoenn.rnbrules.commands.environment.SetLocationEnvironment;
import ethan.hoenn.rnbrules.commands.environment.SetPlayerEnvironment;
import ethan.hoenn.rnbrules.commands.ferry.AddFerry;
import ethan.hoenn.rnbrules.commands.ferry.Ferry;
import ethan.hoenn.rnbrules.commands.ferry.NPCAddFerry;
import ethan.hoenn.rnbrules.commands.ferry.NPCRemoveFerry;
import ethan.hoenn.rnbrules.commands.ferry.RemoveFerry;
import ethan.hoenn.rnbrules.commands.flight.AddFlight;
import ethan.hoenn.rnbrules.commands.flight.Fly;
import ethan.hoenn.rnbrules.commands.flight.RemoveFlight;
import ethan.hoenn.rnbrules.commands.fossil.Fossil;
import ethan.hoenn.rnbrules.commands.fossil.NPCAddFossil;
import ethan.hoenn.rnbrules.commands.fossil.NPCAddUnderpass;
import ethan.hoenn.rnbrules.commands.fossil.SetFossilCompletionCommand;
import ethan.hoenn.rnbrules.commands.fossil.Underpass;
import ethan.hoenn.rnbrules.commands.gamecorner.Gamecorner;
import ethan.hoenn.rnbrules.commands.gamecorner.NPCAddGamecorner;
import ethan.hoenn.rnbrules.commands.gamecorner.SetGamecornerCompletionCommand;
import ethan.hoenn.rnbrules.commands.gauntlet.AddGauntlet;
import ethan.hoenn.rnbrules.commands.gauntlet.AddGauntletNPC;
import ethan.hoenn.rnbrules.commands.gauntlet.ListGauntlets;
import ethan.hoenn.rnbrules.commands.gauntlet.RemoveGauntletNPC;
import ethan.hoenn.rnbrules.commands.gauntlet.SetGauntletHealing;
import ethan.hoenn.rnbrules.commands.gauntlet.SetNextGauntlet;
import ethan.hoenn.rnbrules.commands.globalot.AddGlobalOT;
import ethan.hoenn.rnbrules.commands.globalot.AddPlayerGlobalOT;
import ethan.hoenn.rnbrules.commands.globalot.ListPlayerGlobalOTs;
import ethan.hoenn.rnbrules.commands.globalot.NPCAddGlobalOT;
import ethan.hoenn.rnbrules.commands.globalot.NPCRemoveGlobalOT;
import ethan.hoenn.rnbrules.commands.globalot.RemovePlayerGlobalOT;
import ethan.hoenn.rnbrules.commands.heartscale.HeartscaleExchange;
import ethan.hoenn.rnbrules.commands.heartscale.NPCAddHSE;
import ethan.hoenn.rnbrules.commands.heartscale.Natures;
import ethan.hoenn.rnbrules.commands.intriguingstone.IntriguingStone;
import ethan.hoenn.rnbrules.commands.intriguingstone.NPCAddIntriguingStone;
import ethan.hoenn.rnbrules.commands.itemupgrade.ItemUpgrade;
import ethan.hoenn.rnbrules.commands.itemupgrade.NPCAddItemUpgrade;
import ethan.hoenn.rnbrules.commands.league.*;
import ethan.hoenn.rnbrules.commands.multiplayer.player.Player;
import ethan.hoenn.rnbrules.commands.multiplayer.player.ToggleProfanityFilter;
import ethan.hoenn.rnbrules.commands.multiplayer.staff.Staff;
import ethan.hoenn.rnbrules.commands.reward.*;
import ethan.hoenn.rnbrules.commands.safarizone.NPCAddSafariZone;
import ethan.hoenn.rnbrules.commands.safarizone.SetSafariCompletionCommand;
import ethan.hoenn.rnbrules.commands.safarizone.SetSafariEntry;
import ethan.hoenn.rnbrules.commands.safarizone.SetSafariExit;
import ethan.hoenn.rnbrules.commands.starter.SetStarterSelection;
import ethan.hoenn.rnbrules.commands.starter.StarterMisc;
import java.util.Arrays;
import java.util.List;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class CommandRegistry {

	public static void registerAll(CommandDispatcher<CommandSource> dispatcher) {
		registerBattleDependencyCommands(dispatcher);
		registerRewardCommands(dispatcher);
		registerFerryCommands(dispatcher);
		registerFlightCommands(dispatcher);
		registerHMCommands(dispatcher);
		registerHeartscaleCommands(dispatcher);
		registerIntriguingStoneCommands(dispatcher);
		registerEnvironmentCommands(dispatcher);
		registerGauntletCommands(dispatcher);
		registerBadgeCommands(dispatcher);
		registerDialogueCommands(dispatcher);
		registerItemUpgradeCommands(dispatcher);
		registerGamecornerCommands(dispatcher);
		registerFossilCommands(dispatcher);
		registerStarterCommands(dispatcher);
		registerGlobalOTCommands(dispatcher);
		registerEncounterCommands(dispatcher);
		registerQoLCommands(dispatcher);
		registerBikeshopCommands(dispatcher);
		registerSafariZoneCommands(dispatcher);
		registerLeagueCommands(dispatcher);
		registerMultiplayerCommands(dispatcher);
		registerMiscCommands(dispatcher);
	}

	private static void registerBattleDependencyCommands(CommandDispatcher<CommandSource> dispatcher) {
		AddBattleDep.register(dispatcher);
		ListBattleDeps.register(dispatcher);
		AddPlayerDep.register(dispatcher);
		RemovePlayerDep.register(dispatcher);
		NPCAddBattleDep.register(dispatcher);
		NPCRemoveBattleDeps.register(dispatcher);
		SetBattleDepDescription.register(dispatcher);
		LootAddBattleDep.register(dispatcher);
	}

	private static void registerRewardCommands(CommandDispatcher<CommandSource> dispatcher) {
		NPCAddTMReward.register(dispatcher);
		NPCAddOTReward.register(dispatcher);
		NPCRemoveOTClaimers.register(dispatcher);
		GiveCastform.register(dispatcher);
		GiveKubfu.register(dispatcher);
		GiveRandomHoennStarter.register(dispatcher);
	}

	private static void registerFerryCommands(CommandDispatcher<CommandSource> dispatcher) {
		SetDestination.register(dispatcher);
		NPCAddFerry.register(dispatcher);
		NPCRemoveFerry.register(dispatcher);
		Ferry.register(dispatcher);
		AddFerry.register(dispatcher);
		RemoveFerry.register(dispatcher);
	}

	private static void registerFlightCommands(CommandDispatcher<CommandSource> dispatcher) {
		AddFlight.register(dispatcher);
		RemoveFlight.register(dispatcher);
		Fly.register(dispatcher);
	}

	private static void registerHMCommands(CommandDispatcher<CommandSource> dispatcher) {
		AddHM.register(dispatcher);
		RemoveHM.register(dispatcher);
		ListHM.register(dispatcher);
	}

	private static void registerHeartscaleCommands(CommandDispatcher<CommandSource> dispatcher) {
		Natures.register(dispatcher);
		HeartscaleExchange.register(dispatcher);
		NPCAddHSE.register(dispatcher);
	}

	private static void registerIntriguingStoneCommands(CommandDispatcher<CommandSource> dispatcher) {
		IntriguingStone.register(dispatcher);
		NPCAddIntriguingStone.register(dispatcher);
	}

	private static void registerEnvironmentCommands(CommandDispatcher<CommandSource> dispatcher) {
		SetEnvironment.register(dispatcher);
		SetPlayerEnvironment.register(dispatcher);
		SetLocationEnvironment.register(dispatcher);
		ListLocationEnvironments.register(dispatcher);
	}

	private static void registerGauntletCommands(CommandDispatcher<CommandSource> dispatcher) {
		AddGauntletNPC.register(dispatcher);
		AddGauntlet.register(dispatcher);
		RemoveGauntletNPC.register(dispatcher);
		ListGauntlets.register(dispatcher);
		SetGauntletHealing.register(dispatcher);
		SetNextGauntlet.register(dispatcher);
	}

	private static void registerBadgeCommands(CommandDispatcher<CommandSource> dispatcher) {
		ResetBadges.register(dispatcher);
		AddBadge.register(dispatcher);
	}

	private static void registerDialogueCommands(CommandDispatcher<CommandSource> dispatcher) {
		DialogueYaml.register(dispatcher);
		NPCAddDialogue.register(dispatcher);
		NPCRemoveDialogue.register(dispatcher);
		NPCClearCompletedDialogue.register(dispatcher);
		NPCAddDialogues.register(dispatcher);
		NPCListDialogues.register(dispatcher);
		NPCRemoveDialogueSpecific.register(dispatcher);
		NPCRemoveAllDialogues.register(dispatcher);
	}

	private static void registerItemUpgradeCommands(CommandDispatcher<CommandSource> dispatcher) {
		ItemUpgrade.register(dispatcher);
		NPCAddItemUpgrade.register(dispatcher);
	}

	private static void registerGamecornerCommands(CommandDispatcher<CommandSource> dispatcher) {
		Gamecorner.register(dispatcher);
		NPCAddGamecorner.register(dispatcher);
		SetGamecornerCompletionCommand.register(dispatcher);
	}

	private static void registerFossilCommands(CommandDispatcher<CommandSource> dispatcher) {
		Fossil.register(dispatcher);
		NPCAddFossil.register(dispatcher);
		Underpass.register(dispatcher);
		NPCAddUnderpass.register(dispatcher);
		SetFossilCompletionCommand.register(dispatcher);
	}

	private static void registerStarterCommands(CommandDispatcher<CommandSource> dispatcher) {
		SetStarterSelection.register(dispatcher);
		StarterMisc.register(dispatcher);
	}

	private static void registerGlobalOTCommands(CommandDispatcher<CommandSource> dispatcher) {
		AddGlobalOT.register(dispatcher);
		AddPlayerGlobalOT.register(dispatcher);
		RemovePlayerGlobalOT.register(dispatcher);
		ListPlayerGlobalOTs.register(dispatcher);
		NPCAddGlobalOT.register(dispatcher);
		NPCRemoveGlobalOT.register(dispatcher);
	}

	private static void registerEncounterCommands(CommandDispatcher<CommandSource> dispatcher) {
		ForcedEncounter.register(dispatcher);
		SetForcedEncounter.register(dispatcher);
		ResetRoamerData.register(dispatcher);
		ToggleRoamers.register(dispatcher);
	}

	private static void registerQoLCommands(CommandDispatcher<CommandSource> dispatcher) {
		PreDamage.register(dispatcher);
		PreStatus.register(dispatcher);
	}

	private static void registerBikeshopCommands(CommandDispatcher<CommandSource> dispatcher) {
		NPCAddBikeshop.register(dispatcher);
		NPCRemoveBikeshop.register(dispatcher);
	}

	private static void registerSafariZoneCommands(CommandDispatcher<CommandSource> dispatcher) {
		SetSafariEntry.register(dispatcher);
		SetSafariExit.register(dispatcher);
		SetSafariCompletionCommand.register(dispatcher);
		NPCAddSafariZone.register(dispatcher);
	}

	private static void registerLeagueCommands(CommandDispatcher<CommandSource> dispatcher) {
		SetLeaguePlayerPosition.register(dispatcher);
		SetLeagueOpponentPosition.register(dispatcher);
		ClearLeagueDialoguesCommand.register(dispatcher);
		ResetLeagueProgressCommand.register(dispatcher);
		LeagueCommand.register(dispatcher);
		NPCAddLeague.register(dispatcher);
	}

	private static void registerMiscCommands(CommandDispatcher<CommandSource> dispatcher) {
		ChangeTutorName.register(dispatcher);
		LinkNPCTrainer.register(dispatcher);
		LinkPairedTrainer.register(dispatcher);
		LinkPlayerPartner.register(dispatcher);
		SetNPCTradeCommand.register(dispatcher);
		SetNPCRival.register(dispatcher);
		SetLevelCap.register(dispatcher);
		GeneralTestCommand.register(dispatcher);
		RenderStatueCommand.register(dispatcher);
		Progression.register(dispatcher);
		SetLocationCommand.register(dispatcher);
		LocationInfoCommand.register(dispatcher);
	}

	private static void registerMultiplayerCommands(CommandDispatcher<CommandSource> dispatcher) {
		ToggleProfanityFilter.register(dispatcher);
		Player.register(dispatcher);
		Staff.register(dispatcher);
	}

	private static final List<String> COMMANDS_TO_RESTRICT = Arrays.asList("pokesell", "pokebattle", "pokekill", "pokestats", "evs", "dexcheck", "breed", "battlelog");

	public static void deRegisterAll(CommandDispatcher<CommandSource> dispatcher) {
		for (String command : COMMANDS_TO_RESTRICT) {
			dispatcher.register(
				Commands.literal(command)
					.requires(source -> source.hasPermission(2))
					.executes(context -> {
						context.getSource().sendFailure(new StringTextComponent("You do not have permission to use /" + command));
						return 1;
					})
			);
		}
	}
}
