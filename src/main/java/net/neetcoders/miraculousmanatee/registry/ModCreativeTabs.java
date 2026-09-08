package net.neetcoders.miraculousmanatee.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, MiraculousManateeMod.MOD_ID);

    /** One tab with everything the mod adds. New items should be appended here. */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.miraculousmanatee.main"))
                    .icon(() -> new ItemStack(ModItems.BLUBBER.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.BLUBBER);
                        output.accept(ModItems.BLUBBER_BLOCK);
                        output.accept(ModItems.BLUBBER_BLASTER);
                        output.accept(ModItems.LUMINOUS_CATTAIL);
                        output.accept(ModItems.MISTVEIL_FERN);
                        output.accept(ModItems.SPRINGHEART_BLOOM);
                        output.accept(ModItems.AZURE_DEWCAP);
                        output.accept(ModItems.MOONLIT_LOTUS);
                        output.accept(ModItems.PENGUIN_SPAWN_EGG);
                        output.accept(ModItems.MANATEE_SPAWN_EGG);
                        output.accept(ModItems.EVIL_MANATEE_SPAWN_EGG);
                    })
                    .build());

    private ModCreativeTabs() {
    }

    /** Also lists items in the vanilla tabs where players would look for them. Called from {@link ModEventHandlers}. */
    public static void addToVanillaTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.BLUBBER);
            event.accept(ModItems.BLUBBER_BLOCK);
        } else if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(ModItems.LUMINOUS_CATTAIL);
            event.accept(ModItems.MISTVEIL_FERN);
            event.accept(ModItems.SPRINGHEART_BLOOM);
            event.accept(ModItems.AZURE_DEWCAP);
            event.accept(ModItems.MOONLIT_LOTUS);
        }
    }
}
