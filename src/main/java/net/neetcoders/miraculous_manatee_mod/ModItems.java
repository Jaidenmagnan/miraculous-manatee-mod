package net.neetcoders.miraculous_manatee_mod;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(miraculous_manatee_mod.MOD_ID);

    // This is the placeable item. It places BLUBBER_PILE.
    public static final DeferredItem<Item> BLUBBER =
        ITEMS.register("blubber", () ->
            new BlockItem(ModBlocks.BLUBBER_PILE.get(), new Item.Properties())
        );

    // Item form of the full block (needed for crafting output/placeability)
    public static final DeferredItem<Item> BLUBBER_BLOCK =
        ITEMS.register("blubber_block", () ->
            new BlockItem(ModBlocks.BLUBBER_BLOCK.get(), new Item.Properties())
        );
}