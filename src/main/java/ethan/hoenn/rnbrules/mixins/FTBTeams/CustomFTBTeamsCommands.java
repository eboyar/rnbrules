package ethan.hoenn.rnbrules.mixins.FTBTeams;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.ftb.mods.ftbteams.data.FTBTeamsCommands;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FTBTeamsCommands.class)
public class CustomFTBTeamsCommands {

	@Redirect(method = "register", at = @At(value = "INVOKE", target = "Lnet/minecraft/command/Commands;literal(Ljava/lang/String;)Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;", ordinal = 0))
	private LiteralArgumentBuilder<CommandSource> restrictFTBTeamsCommand(String literal) {
		return Commands.literal(literal).requires(source -> source.hasPermission(2));
	}
}
