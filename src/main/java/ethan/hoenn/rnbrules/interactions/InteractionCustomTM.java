package ethan.hoenn.rnbrules.interactions;

import com.pixelmonmod.pixelmon.api.battles.attack.AttackRegistry;
import com.pixelmonmod.pixelmon.api.config.PixelmonConfigProxy;
import com.pixelmonmod.pixelmon.api.interactions.IInteraction;
import com.pixelmonmod.pixelmon.api.pokemon.LearnMoveController;
import com.pixelmonmod.pixelmon.api.pokemon.species.parameters.mounted.MountedFlyingParameters;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.attacks.ImmutableAttack;
import com.pixelmonmod.pixelmon.comm.ChatHandler;
import com.pixelmonmod.pixelmon.comm.EnumUpdateType;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.items.HMItem;
import ethan.hoenn.rnbrules.utils.managers.GauntletManager;
import java.util.Locale;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

public class InteractionCustomTM implements IInteraction {

	public InteractionCustomTM() {}

	public boolean processInteract(PixelmonEntity entityPixelmon, PlayerEntity player, Hand hand, ItemStack itemstack) {
		if (player instanceof ServerPlayerEntity && itemstack.getItem() instanceof HMItem) {
			ServerWorld world = ((ServerPlayerEntity) player).getLevel();
			GauntletManager gm = GauntletManager.get(world);

			if (gm.isPartOfAnyGauntlet(player.getUUID())) {
				player.sendMessage(new StringTextComponent("§7You must complete or fail your current §dGauntlet§7 to use a HM."), player.getUUID());
				return true;
			}

			if (player == entityPixelmon.getOwner()) {
				HMItem tm = (HMItem) itemstack.getItem();
				ImmutableAttack ab = (ImmutableAttack) AttackRegistry.getAttackBase(tm.attackName).orElse((ImmutableAttack) null);
				if (ab == null) {
					ChatHandler.sendChat(entityPixelmon.getOwner(), ((HMItem) itemstack.getItem()).attackName + " is corrupted", new Object[0]);
					return true;
				}

				if (!entityPixelmon.getForm().getMoves().getHMMoves().contains(ab)) {
					ChatHandler.sendChat(player, "pixelmon.interaction.tmcantlearn", new Object[] { entityPixelmon.getNickname(), ab.getTranslatedName() });
					return true;
				}

				if (entityPixelmon.getPokemon().getMoveset().hasAttack(new ImmutableAttack[] { ab })) {
					ChatHandler.sendChat(entityPixelmon.getOwner(), "pixelmon.interaction.tmknown", new Object[] { entityPixelmon.getNickname(), ab.getTranslatedName() });
					return true;
				}

				if (entityPixelmon.getPokemon().getMoveset().size() >= 4) {
					if (!player.isCreative() && !PixelmonConfigProxy.getGeneral().getTMs().isAllowTMReuse()) {
						ItemStack cost = itemstack.copy();
						cost.setCount(1);
						LearnMoveController.sendLearnMove((ServerPlayerEntity) player, entityPixelmon.getUUID(), ab, LearnMoveController.itemCostCondition(cost));
					} else {
						LearnMoveController.sendLearnMove((ServerPlayerEntity) player, entityPixelmon.getUUID(), ab);
					}
				} else {
					entityPixelmon.getPokemon().getMoveset().add(new Attack(ab));
					ChatHandler.sendChat(entityPixelmon.getOwner(), "pixelmon.stats.learnedmove", new Object[] { entityPixelmon.getNickname(), ab.getTranslatedName() });
				}

				entityPixelmon.update(new EnumUpdateType[] { EnumUpdateType.Moveset });
			} else {
				ChatHandler.sendChat(
					entityPixelmon.getOwner(),
					"pixelmon.interaction.tmcantlearn",
					new Object[] { entityPixelmon.getNickname(), new TranslationTextComponent("attack." + ((HMItem) itemstack.getItem()).attackName.replace(" ", "_").toLowerCase(Locale.ROOT) + "") }
				);
			}

			return true;
		} else {
			return false;
		}
	}
}
