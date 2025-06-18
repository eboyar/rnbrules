package ethan.hoenn.rnbrules.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.pixelmonmod.pixelmon.init.registry.SoundRegistration;
import ethan.hoenn.rnbrules.utils.managers.LevelCapManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

public class SetLevelCap {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("setlevelcap")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("player", EntityArgument.player()).then(
						Commands.argument("levelcap", IntegerArgumentType.integer(1)).executes(context -> {
							ServerPlayerEntity player = EntityArgument.getPlayer(context, "player");
							int newLevelCap = IntegerArgumentType.getInteger(context, "levelcap");

							ServerWorld world = player.getLevel();
							LevelCapManager manager = LevelCapManager.get(world);

							int oldLevelCap = manager.getLevelCap(player.getUUID());
							manager.setLevelCap(player.getUUID(), newLevelCap);

							context.getSource().sendSuccess(new StringTextComponent("Set " + player.getName().getString() + "'s level cap to " + newLevelCap), true);

							displayLevelCapChangeTitle(player, oldLevelCap, newLevelCap);
							return 1;
						})
					)
				)
		);
	}

	private static void displayLevelCapChangeTitle(ServerPlayerEntity player, int oldCap, int newCap) {
		player.connection.send(new STitlePacket(STitlePacket.Type.TIMES, null, 10, 65, 20));

		StringTextComponent subtitle = new StringTextComponent("Level Cap Increase!");
		subtitle.withStyle(TextFormatting.YELLOW);
		player.connection.send(new STitlePacket(STitlePacket.Type.SUBTITLE, subtitle));

		StringTextComponent oldCapText = new StringTextComponent(String.valueOf(oldCap));
		oldCapText.withStyle(TextFormatting.BOLD, TextFormatting.AQUA);

		StringTextComponent arrowText = new StringTextComponent(" -> ");
		arrowText.withStyle(TextFormatting.WHITE);

		StringTextComponent newCapText = new StringTextComponent(String.valueOf(newCap));
		newCapText.withStyle(TextFormatting.BOLD, TextFormatting.GREEN);

		StringTextComponent title = new StringTextComponent("");
		title.append(oldCapText);
		title.append(arrowText);
		title.append(newCapText);

		player.connection.send(new STitlePacket(STitlePacket.Type.TITLE, title));

		player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
	}
}
