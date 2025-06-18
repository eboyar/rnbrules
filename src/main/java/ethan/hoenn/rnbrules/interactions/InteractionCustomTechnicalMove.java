package ethan.hoenn.rnbrules.interactions;

import com.pixelmonmod.pixelmon.api.interactions.IInteraction;
import com.pixelmonmod.pixelmon.api.pokemon.LearnMoveController;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.attacks.ImmutableAttack;
import com.pixelmonmod.pixelmon.comm.ChatHandler;
import com.pixelmonmod.pixelmon.comm.EnumUpdateType;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.enums.technicalmoves.Gen8TechnicalRecords;
import com.pixelmonmod.pixelmon.enums.technicalmoves.ITechnicalMove;
import com.pixelmonmod.pixelmon.items.TechnicalMoveItem;
import ethan.hoenn.rnbrules.utils.managers.GauntletManager;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

public class InteractionCustomTechnicalMove implements IInteraction {

	private static final Set<String> ONE_TIME_USE_MOVES = new HashSet<>(Arrays.asList("Facade", "Swagger", "Draining Kiss", "Tailwind", "Curse", "Explosion"));

	private static final Set<String> TWO_TIME_USE_MOVES = new HashSet<>(
		Arrays.asList(
			"Thunderbolt",
			"Flamethrower",
			"Ice Beam",
			"Hyper Voice",
			"Body Press",
			"Aura Sphere",
			"Shadow Ball",
			"Psychic",
			"Play Rough",
			"Roost",
			"Sludge Bomb",
			"Flash Cannon",
			"Dark Pulse",
			"Earth Power",
			"Dual Wingbeat"
		)
	);

	public InteractionCustomTechnicalMove() {}

	public boolean processInteract(PixelmonEntity pixelmon, PlayerEntity player, Hand hand, ItemStack stack) {
		ServerWorld world = ((ServerPlayerEntity) player).getLevel();
		GauntletManager gm = GauntletManager.get(world);

		if (!(stack.getItem() instanceof TechnicalMoveItem)) {
			return false;
		} else if (!pixelmon.isOwnedBy(player)) {
			return false;
		} else if (gm.isPartOfAnyGauntlet(player.getUUID())) {
			player.sendMessage(new StringTextComponent("§7You must complete or fail your current §dGauntlet§7 to use a TM."), player.getUUID());
			return true;
		} else {
			Pokemon pokemon = pixelmon.getPokemon();
			ITechnicalMove technicalMove = TechnicalMoveItem.getMove(stack);
			if (technicalMove == null) {
				return true;
			} else if (!pixelmon.getForm().getMoves().canLearn(technicalMove)) {
				if (pixelmon.getForm().getMoves().canLearnViaOtherSet(technicalMove)) {
					ChatHandler.sendChat(player, "pixelmon.interaction.tmcantlearncanother", new Object[] { pixelmon.getNickname(), technicalMove.getAttack().getTranslatedName() });
				} else {
					ChatHandler.sendChat(player, "pixelmon.interaction.tmcantlearn", new Object[] { pixelmon.getNickname(), technicalMove.getAttack().getTranslatedName() });
				}

				return true;
			} else if (pokemon.getMoveset().hasAttack(new ImmutableAttack[] { technicalMove.getAttack() })) {
				ChatHandler.sendChat(player, "pixelmon.interaction.tmknown", new Object[] { pixelmon.getNickname(), technicalMove.getAttack().getTranslatedName() });
				return true;
			} else {
				if (pokemon.getMoveset().size() >= 4) {
					if (!player.isCreative()) {
						ItemStack cost = stack.copy();
						cost.setCount(1);
						Predicate<ServerPlayerEntity> condition = null;
						if (technicalMove instanceof Gen8TechnicalRecords) {
							condition = this.addReminderMove(technicalMove, pokemon);
							condition = condition.and(this.damageTM(stack, technicalMove));
						} else {
							condition = this.damageTM(stack, technicalMove);
						}

						LearnMoveController.sendLearnMove((ServerPlayerEntity) player, pokemon.getUUID(), technicalMove.getAttack(), condition);
					} else {
						LearnMoveController.sendLearnMove((ServerPlayerEntity) player, pokemon.getUUID(), technicalMove.getAttack());
					}
				} else {
					pokemon.getMoveset().add(new Attack(technicalMove.getAttack()));
					if (technicalMove instanceof Gen8TechnicalRecords && !pokemon.getMoveset().getReminderMoves().contains(technicalMove.getAttack())) {
						pokemon.getMoveset().getReminderMoves().add(technicalMove.getAttack());
					}

					pixelmon.update(new EnumUpdateType[] { EnumUpdateType.Moveset });
					ChatHandler.sendChat(player, "pixelmon.stats.learnedmove", new Object[] { pixelmon.getNickname(), technicalMove.getAttack().getTranslatedName() });

					if (!player.isCreative()) {
						applyTMDamage(stack, technicalMove);
					}
				}

				return true;
			}
		}
	}

	private void applyTMDamage(ItemStack stack, ITechnicalMove technicalMove) {
		String moveName = technicalMove.getAttack().getAttackName();

		if (ONE_TIME_USE_MOVES.contains(moveName)) {
			stack.shrink(1);
		} else if (TWO_TIME_USE_MOVES.contains(moveName)) {
			int currentDamage = stack.getDamageValue();
			int maxDamage = stack.getMaxDamage();

			if (currentDamage + 2 >= maxDamage) {
				stack.shrink(1);
			} else {
				stack.setDamageValue(currentDamage + 2);
			}
		} else {
			int currentDamage = stack.getDamageValue();
			int maxDamage = stack.getMaxDamage();

			if (currentDamage + 1 >= maxDamage) {
				stack.shrink(1);
			} else {
				stack.setDamageValue(currentDamage + 1);
			}
		}
	}

	private Predicate<ServerPlayerEntity> damageTM(ItemStack stack, ITechnicalMove technicalMove) {
		return player -> {
			applyTMDamage(stack, technicalMove);
			return true;
		};
	}

	public Predicate<ServerPlayerEntity> addReminderMove(ITechnicalMove move, Pokemon pokemon) {
		return p -> {
			if (!pokemon.getMoveset().getReminderMoves().contains(move.getAttack())) {
				pokemon.getMoveset().getReminderMoves().add(move.getAttack());
			}

			return true;
		};
	}
}
