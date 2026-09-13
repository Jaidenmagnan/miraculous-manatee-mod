package net.neetcoders.miraculousmanatee.registry;

import java.util.Set;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModPoiTypes {
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE,
            MiraculousManateeMod.MOD_ID);

    /**
     * Registering the PoiType is enough on its own: NeoForge's {@code PoiTypeCallbacks} automatically maps
     * every block state passed here to this type, so any placed {@code manatee_portal} block becomes
     * discoverable through the {@link net.minecraft.world.entity.ai.village.poi.PoiManager}. This is what lets
     * {@link net.neetcoders.miraculousmanatee.block.portal.ManateePortalTravel} find an existing portal on the
     * far side of a dimension instead of always carving a new one.
     */
    public static final DeferredHolder<PoiType, PoiType> MANATEE_PORTAL = POI_TYPES.register("manatee_portal",
            () -> new PoiType(Set.copyOf(ModBlocks.MANATEE_PORTAL.get().getStateDefinition().getPossibleStates()), 0, 1));

    private ModPoiTypes() {
    }
}
