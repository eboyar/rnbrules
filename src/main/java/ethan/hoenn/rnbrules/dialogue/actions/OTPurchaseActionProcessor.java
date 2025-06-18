package ethan.hoenn.rnbrules.dialogue.actions;

import com.pixelmonmod.pixelmon.api.economy.BankAccount;
import com.pixelmonmod.pixelmon.api.economy.BankAccountProxy;
import ethan.hoenn.rnbrules.utils.data.dialog.DialogueActionData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class OTPurchaseActionProcessor implements DialogueActionProcessor {

	private static final String ACTION_TYPE = "OT_PURCHASE";

	@Override
	public void processAction(DialogueActionData action, ServerPlayerEntity player) {
		if (action.getCommands() == null || action.getCost() == null || action.getCost().isEmpty()) {
			System.err.println("OT_PURCHASE action requires both commands and cost");
			return;
		}

		List<String> commands = new ArrayList<>(action.getCommands());
		int cost;

		try {
			cost = Integer.parseInt(action.getCost().get(0));
		} catch (NumberFormatException e) {
			System.err.println("Invalid cost format in OT_PURCHASE action: " + action.getCost().get(0));
			return;
		}

		if (player.getServer() != null && !commands.isEmpty() && cost > 0) {
			player
				.getServer()
				.execute(() -> {
					try {
						BankAccount userAccount = (BankAccount) BankAccountProxy.getBankAccount(player).orElseThrow(() -> new IllegalStateException("Bank account not found for player."));
						double balance = userAccount.getBalance().doubleValue();

						if (balance < cost) {
							int needed = cost - (int) balance;
							player.sendMessage(
								new StringTextComponent("You cannot afford this purchase! You need ")
									.withStyle(TextFormatting.RED)
									.append(new StringTextComponent("₽" + needed).withStyle(TextFormatting.GOLD))
									.append(new StringTextComponent(" more.").withStyle(TextFormatting.RED)),
								player.getUUID()
							);
						} else {
							userAccount.take(cost);

							CommandSource source = player.getServer().createCommandSourceStack().withEntity(player).withPosition(player.position()).withPermission(4);

							for (String command : commands) {
								String parsedCommand = command.replace("@p", player.getName().getString());
								player.getServer().getCommands().performCommand(source, parsedCommand);
							}

							DialogueActionData completeAction = new DialogueActionData();
							completeAction.setType("COMPLETE");
							DialogueActionManager.getInstance().processAction(completeAction, player);
						}
					} catch (Exception e) {
						System.err.println("Error processing OT_PURCHASE action: " + e.getMessage());
						e.printStackTrace();
					}
				});
		}
	}

	@Override
	public String getActionType() {
		return ACTION_TYPE;
	}
}
