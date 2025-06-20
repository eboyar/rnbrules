package ethan.hoenn.rnbrules.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class PreDamage {

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

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("predamage").then(
				Commands.argument("slot", IntegerArgumentType.integer(1, 6))
					.suggests(PARTY_SLOT_SUGGESTIONS)
					.then(
						Commands.argument("health", IntegerArgumentType.integer(1)).executes(context ->
							executePreDamage(context, IntegerArgumentType.getInteger(context, "slot"), IntegerArgumentType.getInteger(context, "health"), context.getSource().getPlayerOrException())
						)
					)
			)
		);
	}

	public static int executePreDamage(CommandContext<CommandSource> context, int pos, int health, ServerPlayerEntity spe) {
		try {
			if (BattleRegistry.getBattle(spe) != null) {
				spe.sendMessage(new StringTextComponent(TextFormatting.RED + "You cannot use this command while in battle."), spe.getUUID());
				return 0;
			}

			PlayerPartyStorage pps = StorageProxy.getParty(spe);

			if (pos < 1 || pos > 6 || pps.get(pos - 1) == null) {
				spe.sendMessage(new StringTextComponent(TextFormatting.RED + "Invalid party position. Please choose a position between 1-6 with a Pokémon."), spe.getUUID());
				return 0;
			}

			Pokemon target = pps.get(pos - 1);

			//redundant?
			if (health <= 0) {
				spe.sendMessage(new StringTextComponent(TextFormatting.RED + "Health must be greater than 0."), spe.getUUID());
				return 0;
			}

			if (target.getMaxHealth() < health) {
				spe.sendMessage(new StringTextComponent(TextFormatting.RED + "Cannot predamage Pokémon to health higher than its max health (" + target.getMaxHealth() + ")."), spe.getUUID());
				return 0;
			}

			if (target.getHealth() < health) {
				spe.sendMessage(new StringTextComponent(TextFormatting.RED + "You cannot use this command to heal a Pokémon."), spe.getUUID());
				return 0;
			}

			if (target.getHealth() == health) {
				spe.sendMessage(new StringTextComponent(TextFormatting.YELLOW + "Pokémon's health is already at " + health + "."), spe.getUUID());
				return 0;
			}

			target.setHealth(health);

			spe.sendMessage(
				new StringTextComponent(
					TextFormatting.GREEN +
					"Successfully predamaged " +
					TextFormatting.GOLD +
					target.getSpecies().getLocalizedName() +
					TextFormatting.GREEN +
					" to " +
					TextFormatting.RED +
					health +
					TextFormatting.GREEN +
					" health."
				),
				spe.getUUID()
			);

			return 1;
		} catch (Exception e) {
			spe.sendMessage(new StringTextComponent(TextFormatting.GRAY + "Error executing command: " + e.getMessage()), spe.getUUID());
			e.printStackTrace();
			return 0;
		}
	}
}
