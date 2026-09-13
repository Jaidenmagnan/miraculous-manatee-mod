package net.neetcoders.miraculousmanatee.item;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.neetcoders.miraculousmanatee.block.ManateePortalAltarBlock;
import net.neetcoders.miraculousmanatee.block.portal.ManateePortalShape;
import org.jetbrains.annotations.NotNull;

/**
 * The key crafted from the three manatee key pieces. Right-clicking it on a {@link ManateePortalAltarBlock}
 * activates the portal: if the 4x5 frame around the altar is clear, the frame is built (with the altar left
 * in place as the keystone) and the key is consumed. The axis the frame is built on is derived from the
 * direction the player is facing, turned 90 degrees so the frame runs across the player rather than into them.
 */
public class ManateeKeyItem extends Item {
    public ManateeKeyItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockState(context.getClickedPos()).getBlock() instanceof ManateePortalAltarBlock)) {
            return InteractionResult.PASS;
        }

        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        Direction.Axis axis = context.getHorizontalDirection().getClockWise().getAxis();
        if (!ManateePortalShape.canBuild(context.getLevel(), context.getClickedPos(), axis)) {
            Player player = context.getPlayer();
            if (player != null) {
                player.displayClientMessage(Component.translatable("message.miraculousmanatee.portal_blocked"), true);
            }
            return InteractionResult.FAIL;
        }

        ManateePortalShape.build(context.getLevel(), context.getClickedPos(), axis, true);
        context.getItemInHand().consume(1, context.getPlayer());
        context.getLevel().playSound(null, context.getClickedPos(), SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.CONSUME;
    }
}
