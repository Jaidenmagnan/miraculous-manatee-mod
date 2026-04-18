package net.neetcoders.MiraculousManateeMod;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(MiraculousManateeMod.MOD_ID);

    public static final DeferredItem<Item> BLUBBER =
        ITEMS.register("blubber", () -> new Item(new Item.Properties()));
}
