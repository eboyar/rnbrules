/**
 * Ported over from PixelTweaks by Strangeone101
 * https://github.com/StrangeOne101/PixelTweaks
 */
package ethan.hoenn.rnbrules.integration.ftbquests;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import ethan.hoenn.rnbrules.integration.tasks.PokeDollarsTask;
import java.math.BigDecimal;
import net.minecraft.client.Minecraft;

public class TaskUtils {

	public static void updateClientPokedollars(BigDecimal amount) {
		TeamData data = ClientQuestFile.INSTANCE.getData(Minecraft.getInstance().player);

		ClientQuestFile.INSTANCE.collect(PokeDollarsTask.class).forEach(task -> {
			data.setProgress(task, amount.intValue());
		});
	}
}
