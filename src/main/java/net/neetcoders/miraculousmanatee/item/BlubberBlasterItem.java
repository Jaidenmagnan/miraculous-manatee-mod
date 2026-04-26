package net.neetcoders.miraculousmanatee.item;

import java.util.function.Consumer;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neetcoders.miraculousmanatee.config.ModServerConfig;
import net.neetcoders.miraculousmanatee.entity.BlubberProjectile;
import net.neetcoders.miraculousmanatee.registry.ModItems;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BlubberBlasterItem extends Item implements GeoItem {
    private static final float PROJECTILE_VELOCITY = 1.5f;
    private static final float PROJECTILE_INACCURACY = 1.0f;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BlubberBlasterItem(Properties properties) {
        super(properties);
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack blaster = player.getItemInHand(hand);
        ItemStack ammo = findAmmo(player);

        if (ammo.isEmpty() && !player.getAbilities().instabuild) {
            return InteractionResultHolder.fail(blaster);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SLIME_BLOCK_HIT,
                SoundSource.PLAYERS, 0.6f, 1.2f);

        if (!level.isClientSide()) {
            BlubberProjectile projectile = new BlubberProjectile(level, player);
            projectile.setItem(new ItemStack(ModItems.BLUBBER.get()));
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, PROJECTILE_VELOCITY,
                    PROJECTILE_INACCURACY);
            level.addFreshEntity(projectile);

            if (level instanceof ServerLevel serverLevel) {
                GeoItem.getOrAssignId(blaster, serverLevel);
            }
        }

        if (!player.getAbilities().instabuild) {
            ammo.shrink(1);
        }

        player.getCooldowns().addCooldown(this, ModServerConfig.BLUBBER_BLASTER_COOLDOWN_TICKS.get());
        return InteractionResultHolder.sidedSuccess(blaster, level.isClientSide());
    }

    private static ItemStack findAmmo(Player player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack itemStack = player.getInventory().getItem(slot);
            if (itemStack.is(ModItems.BLUBBER.get())) {
                return itemStack;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoItemRenderer<BlubberBlasterItem> renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (renderer == null) {
                    renderer = new GeoItemRenderer<>(BlubberBlasterItem.this);
                }

                return renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
