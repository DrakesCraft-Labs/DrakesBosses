package cl.drakescraft.bosses.boss.combat;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    void counterRemovesHalfCurrentHealthWithoutExecutingThePlayer() {
        assertEquals(10.0D, FullInfinityArmorCounter.calculateResultingHealth(20.0D, 4.0D, 2.0D, 0.50D));
        assertEquals(4.0D, FullInfinityArmorCounter.calculateResultingHealth(6.0D, 4.0D, 2.0D, 0.50D));
    }
}
