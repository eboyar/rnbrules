package ethan.hoenn.rnbrules.commands.fossil;

import com.mojang.brigadier.CommandDispatcher;
import ethan.hoenn.rnbrules.gui.fossils.underpass.UnderpassGui;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public class Underpass {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
            Commands.literal("underpass")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    try {
                        ServerPlayerEntity player = context.getSource().getPlayerOrException();
                        UnderpassGui.openGui(player);
                        return 1;
                    } catch (Exception e) {
                        context.getSource().sendFailure(new StringTextComponent("Failed to open Underpass GUI: " + e.getMessage()));
                        return 0;
                    }
                })
        );
    }
}
