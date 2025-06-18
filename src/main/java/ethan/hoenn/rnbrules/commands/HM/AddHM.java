package ethan.hoenn.rnbrules.commands.HM;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import ethan.hoenn.rnbrules.utils.enums.HiddenMachine;
import ethan.hoenn.rnbrules.utils.managers.HiddenMachineManager;
import java.util.UUID;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

public class AddHM {

	private static final SuggestionProvider<CommandSource> HM_SUGGESTIONS = (context, builder) -> {
		builder.suggest("ALL");
		for (HiddenMachine hm : HiddenMachine.values()) {
			builder.suggest(hm.getHmId());
		}
		return builder.buildFuture();
	};

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("addhm")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("player", EntityArgument.player()).then(
						Commands.argument("hm", StringArgumentType.string())
							.suggests(HM_SUGGESTIONS)
							.executes(context -> {
								ServerPlayerEntity player = EntityArgument.getPlayer(context, "player");
								String hmId = StringArgumentType.getString(context, "hm");

								if (hmId.equals("ALL")) {
									HiddenMachineManager hmManager = HiddenMachineManager.get((ServerWorld) player.level);
									UUID playerUUID = player.getUUID();

									int addedCount = 0;
									for (HiddenMachine hm : HiddenMachine.values()) {
										if (!hmManager.hasHM(playerUUID, hm.getHmId())) {
											hmManager.addHM(playerUUID, hm.getHmId());
											addedCount++;
										}
									}

									context.getSource().sendSuccess(new StringTextComponent("Added ALL HMs to " + player.getDisplayName().getString() + " (" + addedCount + " new HMs)"), true);
									return 1;
								}

								HiddenMachine hm = HiddenMachine.fromString(hmId);
								if (hm != null) {
									HiddenMachineManager hmManager = HiddenMachineManager.get((ServerWorld) player.level);
									UUID playerUUID = player.getUUID();

									if (!hmManager.hasHM(playerUUID, hm.getHmId())) {
										hmManager.addHM(playerUUID, hm.getHmId());
										context.getSource().sendSuccess(new StringTextComponent("Added HM " + hm.getHmId() + " to " + player.getDisplayName().getString()), true);
									} else {
										context.getSource().sendFailure(new StringTextComponent(player.getDisplayName().getString() + " already has HM " + hm.getHmId() + "."));
									}
								} else {
									context.getSource().sendFailure(new StringTextComponent("Invalid HM ID: " + hmId));
								}
								return 1;
							})
					)
				)
		);
	}
}
