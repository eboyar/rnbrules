package ethan.hoenn.rnbrules.commands.reward;

import com.mojang.brigadier.CommandDispatcher;
import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;

public class GiveKubfu {

	private static final PokemonSpecification KUBFU_SPEC = PokemonSpecificationProxy.create("species:kubfu lvl:89 numivs:3 cl:chosen");

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("givekubfu")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("player", EntityArgument.player()).executes(context -> {
						ServerPlayerEntity player = EntityArgument.getPlayer(context, "player");

						Pokemon pokemon = KUBFU_SPEC.create();

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
