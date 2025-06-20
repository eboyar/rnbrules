package ethan.hoenn.rnbrules.utils.misc;

import com.pixelmonmod.pixelmon.blocks.PokemonEggBlock;
import com.pixelmonmod.pixelmon.items.AbilityCapsuleItem;
import com.pixelmonmod.pixelmon.items.AbilityPatchItem;
import com.pixelmonmod.pixelmon.items.BadgeItem;
import com.pixelmonmod.pixelmon.items.BikeItem;
import com.pixelmonmod.pixelmon.items.BottlecapItem;
import com.pixelmonmod.pixelmon.items.EvolutionStoneItem;
import com.pixelmonmod.pixelmon.items.ExpCandyItem;
import com.pixelmonmod.pixelmon.items.HMItem;
import com.pixelmonmod.pixelmon.items.HeldItem;
import com.pixelmonmod.pixelmon.items.MintItem;
import com.pixelmonmod.pixelmon.items.PokeBagItem;
import com.pixelmonmod.pixelmon.items.QuestItem;
import com.pixelmonmod.pixelmon.items.TechnicalMoveItem;
import com.pixelmonmod.pixelmon.items.ValuableItem;
import com.pixelmonmod.pixelmon.items.heldItems.MegaStoneItem;
import dev.ftb.mods.ftbquests.item.QuestBookItem;
import ethan.hoenn.rnbrules.items.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

public class ItemDropValidator {

	private static final Set<Class<? extends Item>> NON_DROPPABLE_ITEM_CLASSES = new HashSet<>(
		Arrays.asList(
			TechnicalMoveItem.class,
			HMItem.class,
			BadgeItem.class,
			HeldItem.class,
			PokeBagItem.class,
			EvolutionStoneItem.class,
			ExpCandyItem.class,
			ValuableItem.class,
			QuestItem.class,
			BottlecapItem.class,
			AbilityCapsuleItem.class,
			AbilityPatchItem.class,
			MintItem.class,
			BikeItem.class,
			MegaStoneItem.class,
			EndlessCandy.class,
			PartyRestore.class,
			MaxPartyRestore.class,
			Battery.class,
			MegaGem.class,
			BigBackpack.class,
			GenericUpgradeItem.class,
			GenericUpgradeComponentItem.class,
			MaxHealingSerum.class,
			QuestBookItem.class
		)
	);

	private static final Set<Predicate<Item>> SPECIAL_VALIDATION_RULES = new HashSet<>(Arrays.asList(item -> item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof PokemonEggBlock));

	public static boolean isNonDroppableItem(Item item) {
		for (Class<? extends Item> itemClass : NON_DROPPABLE_ITEM_CLASSES) {
			if (itemClass.isInstance(item)) {
				return true;
			}
		}

		for (Predicate<Item> rule : SPECIAL_VALIDATION_RULES) {
			if (rule.test(item)) {
				return true;
			}
		}

		return false;
	}

	public static void addNonDroppableItemClass(Class<? extends Item> itemClass) {
		NON_DROPPABLE_ITEM_CLASSES.add(itemClass);
	}

	public static void removeNonDroppableItemClass(Class<? extends Item> itemClass) {
		NON_DROPPABLE_ITEM_CLASSES.remove(itemClass);
	}

	public static void addSpecialValidationRule(Predicate<Item> rule) {
		SPECIAL_VALIDATION_RULES.add(rule);
	}

	public static void removeSpecialValidationRule(Predicate<Item> rule) {
		SPECIAL_VALIDATION_RULES.remove(rule);
	}

	public static Set<Class<? extends Item>> getNonDroppableItemClasses() {
		return new HashSet<>(NON_DROPPABLE_ITEM_CLASSES);
	}

	public static int getSpecialValidationRuleCount() {
		return SPECIAL_VALIDATION_RULES.size();
	}

	@SafeVarargs
	public static void addNonDroppableItemClasses(Class<? extends Item>... itemClasses) {
		for (Class<? extends Item> itemClass : itemClasses) {
			NON_DROPPABLE_ITEM_CLASSES.add(itemClass);
		}
	}
}
