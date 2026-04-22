package net.neetcoders.miraculousmanatee.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.EnumMap;
import java.util.List;

public class ModArmorMaterials {

    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister
            .create(Registries.ARMOR_MATERIAL, "miraculousmanatee");

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> BLUBBER = ARMOR_MATERIALS.register("blubber",
            () -> {

                // Defense values per armor slot
                EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
                defense.put(ArmorItem.Type.BOOTS, 2);
                defense.put(ArmorItem.Type.LEGGINGS, 5);
                defense.put(ArmorItem.Type.CHESTPLATE, 6);
                defense.put(ArmorItem.Type.HELMET, 2);
                defense.put(ArmorItem.Type.BODY, 5); // for horse armor etc, just set a value

                return new ArmorMaterial(
                        defense,
                        15, // enchantability (gold=25, diamond=10, iron=9)
                        SoundEvents.ARMOR_EQUIP_LEATHER, // equip sound
                        () -> Ingredient.EMPTY, // repair ingredient (or use Ingredient.of(ModItems.BLUBBER_ITEM))
                        List.of(new ArmorMaterial.Layer(
                                ResourceLocation.fromNamespaceAndPath("miraculousmanatee", "blubber"))),
                        0.0f, // toughness (diamond=2, netherite=3)
                        0.0f // knockback resistance (netherite=0.1)
                );
            });
}
