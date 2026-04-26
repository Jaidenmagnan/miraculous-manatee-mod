package net.neetcoders.miraculousmanatee.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    private ModCreativeTabs() {
    }

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, MiraculousManateeMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.miraculousmanatee.main"))
                    .icon(() -> new ItemStack(ModItems.BLUBBER.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.BLUBBER.get());
                        output.accept(ModItems.BLUBBER_BLOCK.get());
                        output.accept(ModItems.BLUBBER_BLASTER.get());
                        output.accept(ModItems.PENGUIN_SPAWN_EGG.get());
                        output.accept(ModItems.MANATEE_SPAWN_EGG.get());
                    })
                    .build());
}
