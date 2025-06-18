package ethan.hoenn.rnbrules.commands.reward;

import com.mojang.brigadier.CommandDispatcher;
import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import java.util.Random;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;

public class GiveRandomHoennStarter {

	private static final PokemonSpecification[] HOENN_STARTERS = new PokemonSpecification[] {
		PokemonSpecificationProxy.create("species:treecko egg numivs:3 cl:hoennstarter"),
		PokemonSpecificationProxy.create("species:torchic egg numivs:3 cl:hoennstarter"),
		PokemonSpecificationProxy.create("species:mudkip egg numivs:3 cl:hoennstarter"),
	};

	private static final Random random = new Random();

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("giverandomhoennstarter")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("player", EntityArgument.player()).executes(context -> {
						ServerPlayerEntity player = EntityArgument.getPlayer(context, "player");

						PokemonSpecification spec = HOENN_STARTERS[random.nextInt(HOENN_STARTERS.length)];
						Pokemon pokemon = spec.create();

						if (BattleRegistry.getBattle(player) != null) {
							StorageProxy.getPCForPlayer(player.getUUID()).add(pokemon);
						} else {
							StorageProxy.getParty(player.getUUID()).add(pokemon);
						}

						return 1;
					})
				)
		);
	}
}
