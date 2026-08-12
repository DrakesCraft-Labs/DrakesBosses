package cl.drakescraft.bosses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;
import java.util.Set;
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
}
