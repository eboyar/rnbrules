package ethan.hoenn.rnbrules.commands.multiplayer.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import ethan.hoenn.rnbrules.utils.managers.SettingsManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class ToggleProfanityFilter {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
            Commands.literal("toggleprofanityfilter")
                .executes(ToggleProfanityFilter::execute)
        );
        
        
        dispatcher.register(
            Commands.literal("togglefilter")
                .executes(ToggleProfanityFilter::execute)
        );
    }

    private static int execute(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();
        
        
        if (!(source.getEntity() instanceof ServerPlayerEntity)) {
            source.sendFailure(new StringTextComponent("This command can only be used by players!"));
            return 0;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
        SettingsManager settingsManager = SettingsManager.get();
        
        if (settingsManager == null) {
            source.sendFailure(new StringTextComponent(TextFormatting.RED + "Settings manager not available!"));
            return 0;
        }

        
        boolean newSetting = settingsManager.toggleProfanityFilter(player.getUUID());
        
        
        if (newSetting) {
            player.sendMessage(
                new StringTextComponent(TextFormatting.GREEN + "Profanity filter has been " + 
                    TextFormatting.BOLD + "enabled" + TextFormatting.RESET + TextFormatting.GREEN + 
                    ". Inappropriate language will be filtered from chat."), 
                player.getUUID()
            );
        } else {
            player.sendMessage(
                new StringTextComponent(TextFormatting.YELLOW + "Profanity filter has been " + 
                    TextFormatting.BOLD + "disabled" + TextFormatting.RESET + TextFormatting.YELLOW + 
                    ". You will see unfiltered chat messages."), 
                player.getUUID()
            );
        }

        return 1;
    }
}
