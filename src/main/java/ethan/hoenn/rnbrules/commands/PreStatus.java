package ethan.hoenn.rnbrules.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.status.Burn;
import com.pixelmonmod.pixelmon.battles.status.Paralysis;
import com.pixelmonmod.pixelmon.battles.status.Poison;
import com.pixelmonmod.pixelmon.battles.status.Sleep;
import ethan.hoenn.rnbrules.utils.managers.GauntletManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class PreStatus {

	private static final List<String> VALID_STATUSES = Arrays.asList("Burn", "Poison", "Sleep", "Paralysis");

	private static final SuggestionProvider<CommandSource> PARTY_SLOT_SUGGESTIONS = (context, builder) -> {
		ServerPlayerEntity player = context.getSource().getPlayerOrException();
		PlayerPartyStorage pps = StorageProxy.getParty(player);
		List<String> slots = new ArrayList<>();

		for (int i = 0; i < 6; i++) {
			if (pps.get(i) != null) {
				slots.add(String.valueOf(i + 1));
			}
		}

		return ISuggestionProvider.suggest(slots, builder);
	};

	private static final SuggestionProvider<CommandSource> STATUS_SUGGESTIONS = (context, builder) -> {
		return ISuggestionProvider.suggest(VALID_STATUSES, builder);
	};

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("prestatus").then(
				Commands.argument("slot", IntegerArgumentType.integer(1, 6))
					.suggests(PARTY_SLOT_SUGGESTIONS)
					.then(
						Commands.argument("status", StringArgumentType.string())
							.suggests(STATUS_SUGGESTIONS)
							.executes(context ->
								executePreStatus(context, IntegerArgumentType.getInteger(context, "slot"), StringArgumentType.getString(context, "status"), context.getSource().getPlayerOrException())
							)
					)
			)
		);
	}

	public static int executePreStatus(CommandContext<CommandSource> context, int pos, String statusType, ServerPlayerEntity spe) {
		try {
			if (BattleRegistry.getBattle(spe) != null) {
				context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + "You cannot use this command while in battle."));
				return 0;
			}

			GauntletManager gm = GauntletManager.get(spe.getLevel());
			if (gm.isPartOfAnyGauntlet(spe.getUUID())) {
				context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + "You cannot use this command while in a gauntlet."));
				return 0;
			}

			PlayerPartyStorage pps = StorageProxy.getParty(spe);

			if (pos < 1 || pos > 6 || pps.get(pos - 1) == null) {
				context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + "Invalid party position. Please choose a position between 1-6 with a Pokémon."));
				return 0;
			}

			Pokemon target = pps.get(pos - 1);

			if (Objects.requireNonNull(target).isFainted()) {
				context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + "Cannot apply status to a fainted Pokémon."));
				return 0;
			}

			if (!VALID_STATUSES.contains(statusType)) {
				context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + "Invalid status type. Valid options: " + String.join(", ", VALID_STATUSES)));
				return 0;
			}

			switch (statusType) {
				case "Burn":
					target.setStatus(new Burn());
					break;
				case "Poison":
					target.setStatus(new Poison());
					break;
				case "Sleep":
					target.setStatus(new Sleep());
					break;
				case "Paralysis":
					target.setStatus(new Paralysis());
					break;
				default:
					context.getSource().sendFailure(new StringTextComponent(TextFormatting.RED + "Unknown status type: " + statusType));
					return 0;
			}

			context
				.getSource()
				.sendSuccess(
					new StringTextComponent(
						TextFormatting.GREEN +
						"Successfully applied " +
						TextFormatting.GOLD +
						statusType +
						TextFormatting.GREEN +
						" status to " +
						TextFormatting.AQUA +
						target.getDisplayName() +
						TextFormatting.GREEN +
						"."
					),
					true
				);

			return 1;
		} catch (Exception e) {
			context.getSource().sendFailure(new StringTextComponent(TextFormatting.GRAY + "Error executing command: " + e.getMessage()));
			e.printStackTrace();
			return 0;
		}
	}
}
