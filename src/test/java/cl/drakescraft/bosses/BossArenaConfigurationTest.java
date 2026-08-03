package cl.drakescraft.bosses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

/** Guards the single canonical boss-arena block used for paid arena admission. */
final class BossArenaConfigurationTest {

    @Test
    void arenaConfigurationHasOneExplicitWorldContract() throws Exception {
        Path configPath = Path.of("src", "main", "resources", "config.yml");
        String source = Files.readString(configPath);
        assertEquals(1, source.split("(?m)^boss-arena:", -1).length - 1,
                "config.yml must have exactly one boss-arena block");

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configPath.toFile());
        assertEquals("drakes_bosses", config.getString("boss-arena.world-name"));
        assertFalse(config.getBoolean("boss-arena.allow-auto-create", true));
        assertNotNull(config.getConfigurationSection("boss-arena.entry-fees"));
        assertEquals("drakesbosses.bosswarp.free",
                config.getString("boss-arena.entry-fees.free-permission"));
    }
}
