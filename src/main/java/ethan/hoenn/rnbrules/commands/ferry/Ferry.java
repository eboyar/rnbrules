package ethan.hoenn.rnbrules.commands.ferry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import ethan.hoenn.rnbrules.gui.ferry.FerryGui;
import ethan.hoenn.rnbrules.utils.enums.FerryRoute;
import java.util.Arrays;
import java.util.stream.Collectors;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public class Ferry {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("ferry")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("route", StringArgumentType.word())
						.suggests((context, builder) -> ISuggestionProvider.suggest(Arrays.stream(FerryRoute.values()).map(route -> route.name().toLowerCase()).collect(Collectors.toList()), builder))
						.executes(context -> {
							ServerPlayerEntity player = context.getSource().getPlayerOrException();
							String routeName = StringArgumentType.getString(context, "route").toUpperCase();

							try {
								FerryRoute route = FerryRoute.valueOf(routeName);
								openFerryGui(player, route);

								return 1;
							} catch (IllegalArgumentException e) {
								context.getSource().sendFailure(new StringTextComponent("Invalid ferry route: " + routeName));
								return 0;
							}
						})
				)
		);
	}

	private static void openFerryGui(ServerPlayerEntity player, FerryRoute route) {
		FerryGui.openGui(player, route, null);
	}
}
