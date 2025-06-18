package ethan.hoenn.rnbrules.interactions;

import com.pixelmonmod.pixelmon.api.events.pokemon.ItemInteractionEvent;
import com.pixelmonmod.pixelmon.api.interactions.IInteraction;
import com.pixelmonmod.pixelmon.api.pokemon.ability.Ability;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.items.AbilityPatchItem;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;

public class InteractionCustomAbilityPatch implements IInteraction {

	private static final Set<String> BANNED_HIDDEN_ABILITIES = new HashSet<>();

	static {
		BANNED_HIDDEN_ABILITIES.add("Drizzle");
		BANNED_HIDDEN_ABILITIES.add("Drought");
		BANNED_HIDDEN_ABILITIES.add("Snow Warning");
		BANNED_HIDDEN_ABILITIES.add("Sand Stream");

		BANNED_HIDDEN_ABILITIES.add("Grassy Surge");
		BANNED_HIDDEN_ABILITIES.add("Psychic Surge");
		BANNED_HIDDEN_ABILITIES.add("Misty Surge");
		BANNED_HIDDEN_ABILITIES.add("Electric Surge");

		BANNED_HIDDEN_ABILITIES.add("Speed Boost");
		BANNED_HIDDEN_ABILITIES.add("Moxie");
		BANNED_HIDDEN_ABILITIES.add("Gale Wings");
		BANNED_HIDDEN_ABILITIES.add("Contrary");

		BANNED_HIDDEN_ABILITIES.add("Magic Bounce");
		BANNED_HIDDEN_ABILITIES.add("Magic Guard");
		BANNED_HIDDEN_ABILITIES.add("Regenerator");
		BANNED_HIDDEN_ABILITIES.add("Flash Fire");
		BANNED_HIDDEN_ABILITIES.add("Sap Sipper");
		BANNED_HIDDEN_ABILITIES.add("Lightning Rod");
		BANNED_HIDDEN_ABILITIES.add("Motor Drive");
		BANNED_HIDDEN_ABILITIES.add("Volt Absorb");
		BANNED_HIDDEN_ABILITIES.add("Water Absorb");
		BANNED_HIDDEN_ABILITIES.add("Storm Drain");
		BANNED_HIDDEN_ABILITIES.add("Slush Rush");
		BANNED_HIDDEN_ABILITIES.add("Sand Veil");
		BANNED_HIDDEN_ABILITIES.add("Snow Cloak");

		BANNED_HIDDEN_ABILITIES.add("Prankster");
		BANNED_HIDDEN_ABILITIES.add("Sturdy");
		BANNED_HIDDEN_ABILITIES.add("Unburden");
		BANNED_HIDDEN_ABILITIES.add("Serene Grace");
		BANNED_HIDDEN_ABILITIES.add("Tinted Lens");
		BANNED_HIDDEN_ABILITIES.add("Simple");
	}

	public InteractionCustomAbilityPatch() {}

	public boolean processInteract(PixelmonEntity pixelmon, PlayerEntity player, Hand hand, ItemStack itemStack) {
		if (player instanceof ServerPlayerEntity && pixelmon.getOwner() == player) {
			Item item = itemStack.getItem();
			if (item instanceof AbilityPatchItem) {
				Ability[] hiddenAbilities = pixelmon.getPokemon().getForm().getAbilities().getHiddenAbilities();

				System.out.println(Arrays.toString(hiddenAbilities));

				for (Ability ability : hiddenAbilities) {
					if (BANNED_HIDDEN_ABILITIES.contains(ability.getLocalizedName())) {
						player.sendMessage(new StringTextComponent("§6" + pixelmon.getLocalizedName() + "§7 has a banned Hidden Ability."), player.getUUID());
						return true;
					}
				}

				AbilityPatchItem patch = (AbilityPatchItem) item;
				if (MinecraftForge.EVENT_BUS.post(new ItemInteractionEvent(player, pixelmon, itemStack))) {
					return false;
				}

				if (patch.useOnEntity(pixelmon, player) && !player.isCreative()) {
					player.getItemInHand(hand).shrink(1);
				}

				return true;
			}
		}
		return false;
	}
}
