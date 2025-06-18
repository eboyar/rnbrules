package ethan.hoenn.rnbrules.commands.ferry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.pixelmonmod.pixelmon.entities.npcs.NPCChatting;
import ethan.hoenn.rnbrules.utils.enums.FerryDestination;
import java.util.Arrays;
import java.util.stream.Collectors;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class NPCAddFerry {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("npcaddferry")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("destination", StringArgumentType.word())
						.suggests((context, builder) -> ISuggestionProvider.suggest(Arrays.stream(FerryDestination.values()).map(FerryDestination::name).collect(Collectors.toList()), builder))
						.then(
							Commands.argument("npc", EntityArgument.entity()).executes(context -> {
								String destination = StringArgumentType.getString(context, "destination").toUpperCase();
								Entity entity = EntityArgument.getEntity(context, "npc");

								if (!(entity instanceof NPCChatting)) {
									context.getSource().sendFailure(new StringTextComponent("Target must be a Pixelmon NPC"));
									return 0;
								}

								FerryDestination ferryDest;
								try {
									ferryDest = FerryDestination.valueOf(destination);
								} catch (IllegalArgumentException e) {
									context.getSource().sendFailure(new StringTextComponent("Invalid ferry destination: " + destination));
									return 0;
								}

								NPCChatting npc = (NPCChatting) entity;
								CompoundNBT entityData = npc.getPersistentData();
								entityData.putString("Sailor", destination);

								context.getSource().sendSuccess(new StringTextComponent("Set ferry destination to " + ferryDest.getDisplayName()).withStyle(TextFormatting.GREEN), true);

								return 1;
							})
						)
				)
		);
	}
}
