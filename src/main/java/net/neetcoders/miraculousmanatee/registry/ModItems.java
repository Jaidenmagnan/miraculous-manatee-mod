package net.neetcoders.miraculousmanatee.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import net.neetcoders.miraculousmanatee.ModBlocks;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private ModItems() {
    }

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MiraculousManateeMod.MOD_ID);

    public static final DeferredItem<Item> BLUBBER = ITEMS.register("blubber", () ->
            new NamedBlockItem(ModBlocks.BLUBBER_PILE.get(), new Item.Properties()));

    public static final DeferredItem<Item> BLUBBER_BLOCK = ITEMS.register("blubber_block", () ->
            new BlockItem(ModBlocks.BLUBBER_BLOCK.get(), new Item.Properties()));

    public static final DeferredItem<Item> PENGUIN_SPAWN_EGG = ITEMS.register("penguin_spawn_egg", () ->
            new DeferredSpawnEggItem(ModEntities.PENGUIN, 0x3A3F44, 0xEDEBE8, new Item.Properties()));

    public static final DeferredItem<Item> MANATEE_SPAWN_EGG = ITEMS.register("manatee_spawn_egg", () ->
            new DeferredSpawnEggItem(ModEntities.MANATEE, 0x6B7D86, 0xD8D2C2, new Item.Properties()));
}
