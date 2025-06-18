package ethan.hoenn.rnbrules.commands.starter;

import static ethan.hoenn.rnbrules.utils.managers.StarterSelectionManager.StarterChoice;

import com.mojang.brigadier.CommandDispatcher;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.PartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import ethan.hoenn.rnbrules.utils.managers.StarterSelectionManager;
import java.util.UUID;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;

public class SetStarterSelection {

	private static final String STARTER_CATCH_LOCATION = "starter";

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			Commands.literal("setplayerstarter")
				.requires(source -> source.hasPermission(2))
				.then(
					Commands.argument("target", EntityArgument.player()).executes(context -> {
						MinecraftServer server = context.getSource().getServer();
						ServerPlayerEntity target = EntityArgument.getPlayer(context, "target");

						server.execute(() -> {
							PartyStorage storage = StorageProxy.getParty(target);
							PCStorage pcstorage = StorageProxy.getPCForPlayer(target);

							Pokemon[] party = storage.getAll();
							Pokemon[] pc = pcstorage.getAll();

							StarterChoice foundStarter = null;
							boolean hasStarterTag = false;

							for (Pokemon p : party) {
								if (p == null) continue;

								boolean isStarterTagged = false;
								if (p.getPersistentData() != null && p.getPersistentData().contains("CatchLocation") && p.getPersistentData().getString("CatchLocation").equals(STARTER_CATCH_LOCATION)) {
									isStarterTagged = true;
								}

								if (isStarterTagged) {
									if (p.getSpecies().is(PixelmonSpecies.TURTWIG)) {
										foundStarter = StarterChoice.TURTWIG;
										hasStarterTag = true;
										break;
									} else if (p.getSpecies().is(PixelmonSpecies.CHIMCHAR)) {
										foundStarter = StarterChoice.CHIMCHAR;
										hasStarterTag = true;
										break;
									} else if (p.getSpecies().is(PixelmonSpecies.PIPLUP)) {
										foundStarter = StarterChoice.PIPLUP;
										hasStarterTag = true;
										break;
									}
								}
							}

							if (!hasStarterTag) {
								for (Pokemon p : pc) {
									if (p == null) continue;

									boolean isStarterTagged = false;
									if (p.getPersistentData() != null && p.getPersistentData().contains("CatchLocation") && p.getPersistentData().getString("CatchLocation").equals(STARTER_CATCH_LOCATION)) {
										isStarterTagged = true;
									}

									if (isStarterTagged) {
										if (p.getSpecies().is(PixelmonSpecies.TURTWIG)) {
											foundStarter = StarterChoice.TURTWIG;
											hasStarterTag = true;
											break;
										} else if (p.getSpecies().is(PixelmonSpecies.CHIMCHAR)) {
											foundStarter = StarterChoice.CHIMCHAR;
											hasStarterTag = true;
											break;
										} else if (p.getSpecies().is(PixelmonSpecies.PIPLUP)) {
											foundStarter = StarterChoice.PIPLUP;
											hasStarterTag = true;
											break;
										}
									}
								}
							}

							if (foundStarter == null || !hasStarterTag) {
								context
									.getSource()
									.sendFailure(new StringTextComponent("Could not detect a valid Sinnoh starter with 'starter' catch location in " + target.getName().getString() + "'s party or PC."));
								return;
							}

							UUID uuid = target.getUUID();
							StarterSelectionManager.get(target.getLevel()).setPlayerSelection(uuid, foundStarter);
							context.getSource().sendSuccess(new StringTextComponent("Set " + target.getName().getString() + "'s starter to " + foundStarter.name()), true);
						});

						return 1;
					})
				)
		);
	}
}
