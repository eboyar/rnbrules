package ethan.hoenn.rnbrules.commands.environment;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import ethan.hoenn.rnbrules.utils.enums.Environment;
import ethan.hoenn.rnbrules.utils.managers.LocationManager;
import java.util.Map;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class ListLocationEnvironments {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("locationenvironments").requires(source -> source.hasPermission(2)).executes(ListLocationEnvironments::execute));
	}

	private static int execute(CommandContext<CommandSource> context) {
		CommandSource source = context.getSource();
		LocationManager locationManager = LocationManager.get(source.getLevel());
		Map<String, Environment> environments = locationManager.getLocationEnvironments();

		if (environments.isEmpty()) {
			source.sendSuccess(new StringTextComponent("No locations have environments set."), false);
			return 1;
		}

		source.sendSuccess(new StringTextComponent("=== Location Environments ===").withStyle(TextFormatting.YELLOW), false);

		environments
			.entrySet()
			.stream()
			.sorted(Map.Entry.comparingByKey())
			.forEach(entry -> {
				String location = entry.getKey();
				Environment env = entry.getValue();

				ITextComponent message = new StringTextComponent(" - ")
					.withStyle(TextFormatting.GRAY)
					.append(new StringTextComponent(location).withStyle(TextFormatting.WHITE))
					.append(new StringTextComponent(": ").withStyle(TextFormatting.GRAY))
					.append(new StringTextComponent(env.getId()).withStyle(getEnvironmentColor(env)));

				source.sendSuccess(message, false);
			});

		return 1;
	}

	private static TextFormatting getEnvironmentColor(Environment environment) {
		switch (environment) {
			case SANDSTORM:
				return TextFormatting.GOLD;
			case MAGMA_STORM:
			case HEAT_CAVE:
				return TextFormatting.RED;
			case TAILWIND:
				return TextFormatting.AQUA;
			case AURORA_VEIL:
			case AURORA_CAVE:
				return TextFormatting.LIGHT_PURPLE;
			case RAIN:
				return TextFormatting.BLUE;
			default:
				return TextFormatting.GREEN;
		}
	}
}
