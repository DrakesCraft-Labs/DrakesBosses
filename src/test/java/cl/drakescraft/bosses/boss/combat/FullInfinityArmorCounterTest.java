package cl.drakescraft.bosses.boss.combat;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Guards the stable material identifier without booting Paper registries. */
final class FullInfinityArmorCounterTest {

    @Test
    void detectsOnlyTheExactInfinitySingularityMaterial() {
        assertTrue(FullInfinityArmorCounter.isInfinitySingularityMaterialId("INFINITY_SINGULARITY"));
        assertTrue(FullInfinityArmorCounter.isInfinitySingularityMaterialId("infinity_singularity"));
        assertTrue(!FullInfinityArmorCounter.isInfinitySingularityMaterialId("DAXI_SATURATION"));
        assertTrue(!FullInfinityArmorCounter.isInfinitySingularityMaterialId("INFINITY_INGOT"));
        assertTrue(!FullInfinityArmorCounter.isInfinitySingularityMaterialId(null));
    }
}
