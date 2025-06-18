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

public class GiveCastform {

	//TODO: CHANGE THIS STRING BASED ON IF THE PLAYER TOOK AN ENCOUNTER IN ROUTE 119 OR NOT, REMOVE HI:LIFE_ORB IF IT DID
	private static final PokemonSpecification CASTFORM_SPEC = PokemonSpecificationProxy.create("species:castform lvl:5 hi:life_orb numivs:3 cl:winstitute");

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("givecastform")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("player", EntityArgument.player()).executes(context -> {
						ServerPlayerEntity player = EntityArgument.getPlayer(context, "player");

						Pokemon pokemon = CASTFORM_SPEC.create();

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
