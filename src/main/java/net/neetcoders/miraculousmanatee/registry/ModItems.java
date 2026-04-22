package net.neetcoders.miraculousmanatee.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ArmorItem;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import net.neetcoders.miraculousmanatee.item.BlubberArmorItem;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neetcoders.miraculousmanatee.registry.ModBlocks;

public final class ModItems {

    private ModItems() {
    }

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MiraculousManateeMod.MOD_ID);

    public static final DeferredItem<Item> BLUBBER = ITEMS.registerItem("blubber",
            props -> new BlockItem(ModBlocks.BLUBBER_PILE.get(), props));

    public static final DeferredItem<Item> BLUBBER_BLOCK = ITEMS.registerItem("blubber_block",
            props -> new BlockItem(ModBlocks.BLUBBER_BLOCK.get(), props));

    public static final DeferredItem<Item> PENGUIN_SPAWN_EGG = ITEMS.registerItem("penguin_spawn_egg",
            props -> new DeferredSpawnEggItem(ModEntities.PENGUIN, 0x3A3F44, 0xEDEBE8, props));

    public static final DeferredItem<Item> MANATEE_SPAWN_EGG = ITEMS.registerItem("manatee_spawn_egg",
            props -> new DeferredSpawnEggItem(ModEntities.MANATEE, 0x6B7D86, 0xD8D2C2, props));

    public static final DeferredItem<BlubberArmorItem> BLUBBER_HELMET = ITEMS.registerItem("blubber_helmet",
            props -> new BlubberArmorItem(ModArmorMaterials.BLUBBER, ArmorItem.Type.HELMET, props));

    public static final DeferredItem<BlubberArmorItem> BLUBBER_CHESTPLATE = ITEMS.registerItem("blubber_chestplate",
            props -> new BlubberArmorItem(ModArmorMaterials.BLUBBER, ArmorItem.Type.CHESTPLATE, props));

    public static final DeferredItem<BlubberArmorItem> BLUBBER_LEGGINGS = ITEMS.registerItem("blubber_leggings",
            props -> new BlubberArmorItem(ModArmorMaterials.BLUBBER, ArmorItem.Type.LEGGINGS, props));

    public static final DeferredItem<BlubberArmorItem> BLUBBER_BOOTS = ITEMS.registerItem("blubber_boots",
            props -> new BlubberArmorItem(ModArmorMaterials.BLUBBER, ArmorItem.Type.BOOTS, props));
}
