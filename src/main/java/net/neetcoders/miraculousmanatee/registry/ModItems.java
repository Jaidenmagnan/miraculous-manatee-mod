package net.neetcoders.miraculousmanatee.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import net.neetcoders.miraculousmanatee.item.BlubberBlasterItem;
import net.neetcoders.miraculousmanatee.item.NamedBlockItem;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MiraculousManateeMod.MOD_ID);

    /** Places a blubber pile, but is named "Blubber" rather than after the block. */
    public static final DeferredItem<NamedBlockItem> BLUBBER = ITEMS.registerItem("blubber",
            properties -> new NamedBlockItem(ModBlocks.BLUBBER_PILE.get(), properties));
    public static final DeferredItem<BlockItem> BLUBBER_BLOCK = ITEMS.registerSimpleBlockItem(ModBlocks.BLUBBER_BLOCK);
    public static final DeferredItem<BlubberBlasterItem> BLUBBER_BLASTER = ITEMS.registerItem("blubber_blaster",
            BlubberBlasterItem::new, new Item.Properties().stacksTo(1));

    public static final DeferredItem<BlockItem> LUMINOUS_CATTAIL = ITEMS.registerSimpleBlockItem(ModBlocks.LUMINOUS_CATTAIL);
    public static final DeferredItem<BlockItem> MISTVEIL_FERN = ITEMS.registerSimpleBlockItem(ModBlocks.MISTVEIL_FERN);
    public static final DeferredItem<BlockItem> SPRINGHEART_BLOOM = ITEMS.registerSimpleBlockItem(ModBlocks.SPRINGHEART_BLOOM);
    public static final DeferredItem<BlockItem> AZURE_DEWCAP = ITEMS.registerSimpleBlockItem(ModBlocks.AZURE_DEWCAP);
    public static final DeferredItem<BlockItem> MOONLIT_LOTUS = ITEMS.registerSimpleBlockItem(ModBlocks.MOONLIT_LOTUS);

    public static final DeferredItem<DeferredSpawnEggItem> PENGUIN_SPAWN_EGG = ITEMS.registerItem("penguin_spawn_egg",
            properties -> new DeferredSpawnEggItem(ModEntities.PENGUIN, 0x3A3F44, 0xEDEBE8, properties));
    public static final DeferredItem<DeferredSpawnEggItem> MANATEE_SPAWN_EGG = ITEMS.registerItem("manatee_spawn_egg",
            properties -> new DeferredSpawnEggItem(ModEntities.MANATEE, 0x6B7D86, 0xD8D2C2, properties));
    public static final DeferredItem<DeferredSpawnEggItem> EVIL_MANATEE_SPAWN_EGG = ITEMS.registerItem(
            "evil_manatee_spawn_egg",
            properties -> new DeferredSpawnEggItem(ModEntities.EVIL_MANATEE, 0x1F1B2E, 0xE01010, properties));

    private ModItems() {
    }
}
