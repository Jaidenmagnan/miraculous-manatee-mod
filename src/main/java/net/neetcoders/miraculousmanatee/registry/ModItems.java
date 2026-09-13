package net.neetcoders.miraculousmanatee.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import net.neetcoders.miraculousmanatee.item.BlubberBlasterItem;
import net.neetcoders.miraculousmanatee.item.ManateeKeyItem;
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

    /** Drop from manatees, evil manatees and elder manatees respectively; craft into {@link #MANATEE_KEY}. */
    public static final DeferredItem<Item> MANATEE_KEY_PIECE = ITEMS.registerSimpleItem("manatee_key_piece",
            new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> EVIL_MANATEE_KEY_PIECE = ITEMS.registerSimpleItem("evil_manatee_key_piece",
            new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> ELDER_MANATEE_KEY_PIECE = ITEMS.registerSimpleItem("elder_manatee_key_piece",
            new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<ManateeKeyItem> MANATEE_KEY = ITEMS.registerItem("manatee_key",
            ManateeKeyItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.RARE));

    public static final DeferredItem<BlockItem> LUMINOUS_CATTAIL = ITEMS.registerSimpleBlockItem(ModBlocks.LUMINOUS_CATTAIL);
    public static final DeferredItem<BlockItem> MISTVEIL_FERN = ITEMS.registerSimpleBlockItem(ModBlocks.MISTVEIL_FERN);
    public static final DeferredItem<BlockItem> SPRINGHEART_BLOOM = ITEMS.registerSimpleBlockItem(ModBlocks.SPRINGHEART_BLOOM);
    public static final DeferredItem<BlockItem> AZURE_DEWCAP = ITEMS.registerSimpleBlockItem(ModBlocks.AZURE_DEWCAP);
    public static final DeferredItem<BlockItem> MOONLIT_LOTUS = ITEMS.registerSimpleBlockItem(ModBlocks.MOONLIT_LOTUS);

    public static final DeferredItem<BlockItem> MANATEE_PORTAL_ALTAR = ITEMS.registerSimpleBlockItem(ModBlocks.MANATEE_PORTAL_ALTAR);
    public static final DeferredItem<BlockItem> MANATEE_HEAD = ITEMS.registerSimpleBlockItem(ModBlocks.MANATEE_HEAD);

    public static final DeferredItem<DeferredSpawnEggItem> PENGUIN_SPAWN_EGG = ITEMS.registerItem("penguin_spawn_egg",
            properties -> new DeferredSpawnEggItem(ModEntities.PENGUIN, 0x3A3F44, 0xEDEBE8, properties));
    public static final DeferredItem<DeferredSpawnEggItem> MANATEE_SPAWN_EGG = ITEMS.registerItem("manatee_spawn_egg",
            properties -> new DeferredSpawnEggItem(ModEntities.MANATEE, 0x6B7D86, 0xD8D2C2, properties));
    public static final DeferredItem<DeferredSpawnEggItem> EVIL_MANATEE_SPAWN_EGG = ITEMS.registerItem(
            "evil_manatee_spawn_egg",
            properties -> new DeferredSpawnEggItem(ModEntities.EVIL_MANATEE, 0x1F1B2E, 0xE01010, properties));
    public static final DeferredItem<DeferredSpawnEggItem> ELDER_MANATEE_SPAWN_EGG = ITEMS.registerItem(
            "elder_manatee_spawn_egg",
            properties -> new DeferredSpawnEggItem(ModEntities.ELDER_MANATEE, 0x0E4B5A, 0xE8C15A, properties));

    private ModItems() {
    }
}
