package cl.drakescraft.bosses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;
import java.util.Set;
import cl.drakescraft.bosses.items.OdysseyItemManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

/** Prevents general Odysseia systems from leaking back into DrakesBosses. */
final class BossConfigScopeTest {

    @Test
    void defaultConfigContainsOnlyBossOwnedSections() {
        Path path = Path.of("src", "main", "resources", "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());

        assertEquals(Set.of(
                        "discord",
                        "integrations",
                        "boss-balance",
                        "boss-arena",
                        "natural-spawn",
                        "boss-domains",
                        "bosses",
                        "boss-loot"),
                config.getKeys(false));
    }

    @Test
    void naturalBossesNeverLeakIntoClassicWorlds() {
        Path path = Path.of("src", "main", "resources", "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());

        var worlds = config.getStringList("natural-spawn.worlds");
        assertFalse(worlds.isEmpty(), "an empty list means every world");
        assertFalse(worlds.stream().anyMatch(world -> world.startsWith("clasico")));
    }

    @Test
    void everyBossHasAConfiguredAndRegisteredRelic() {
        Path path = Path.of("src", "main", "resources", "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
        Set<String> expectedBosses = Set.of(
                "circe", "polifemo", "dios_corrupto", "thor", "ares", "hades", "poseidon", "zeus",
                "loki", "odin", "kratos", "heimdall", "hidra", "cerbero", "artemisa", "tifon",
                "prometeo", "coloso_end", "wither_storm", "dragon_ancestral", "ra", "isis", "anubis",
                "set", "garou_cosmico", "jax");
        var bosses = config.getConfigurationSection("boss-loot.bosses");

        assertEquals(expectedBosses, bosses.getKeys(false));
        for (String bossId : expectedBosses) {
            var drops = bosses.getConfigurationSection(bossId + ".drops");
            assertFalse(drops.getKeys(false).isEmpty(), bossId + " necesita al menos una reliquia");
            for (String itemId : drops.getKeys(false)) {
                org.junit.jupiter.api.Assertions.assertTrue(
                        OdysseyItemManager.isRegisteredBossDropId(itemId),
                        () -> bossId + " declara una reliquia inexistente: " + itemId);
            }
        }
    }
}
