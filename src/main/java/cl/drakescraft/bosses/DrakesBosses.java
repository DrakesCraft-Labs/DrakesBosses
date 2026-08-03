package cl.drakescraft.bosses;

import cl.drakescraft.bosses.boss.BossManager;
import cl.drakescraft.bosses.boss.arena.BossArenaService;
import cl.drakescraft.bosses.commands.BossCommand;
import cl.drakescraft.bosses.commands.BossWarpCommand;
import cl.drakescraft.bosses.listeners.BossItemListener;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/** Owns the boss lifecycle while exposing no dependency on the general server plugin. */
public final class DrakesBosses extends JavaPlugin {
    private static DrakesBosses instance;
    private BossManager bossManager;
    private BossArenaService bossArenas;

    public static DrakesBosses getInstance() {
        return instance;
    }

    /** Exposes boss creation to optional modules without letting them own arena lifecycle. */
    public BossManager getBossManager() {
        return bossManager;
    }

    public BossArenaService getBossArenas() {
        return bossArenas;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        bossManager = new BossManager(this);
        bossArenas = new BossArenaService(this, bossManager);
        BossWarpCommand bossWarp = new BossWarpCommand(bossArenas);
        BossCommand bossCommand = new BossCommand(this, bossManager);

        Objects.requireNonNull(getCommand("bosswarp")).setExecutor(bossWarp);
        Objects.requireNonNull(getCommand("bosswarp")).setTabCompleter(bossWarp);
        Objects.requireNonNull(getCommand("boss")).setExecutor(bossCommand);
        Objects.requireNonNull(getCommand("boss")).setTabCompleter(bossCommand);
        Objects.requireNonNull(getCommand("spawnallbosses")).setExecutor(bossCommand);
        Objects.requireNonNull(getCommand("spawnallbosses")).setTabCompleter(bossCommand);

        Bukkit.getPluginManager().registerEvents(bossManager, this);
        Bukkit.getPluginManager().registerEvents(bossArenas, this);
        Bukkit.getPluginManager().registerEvents(new BossItemListener(this), this);
        getLogger().info("DrakesBosses listo: bosses, arenas, loot y cobros aislados de Odysseia.");
    }

    @Override
    public void onDisable() {
        if (bossManager != null) {
            bossManager.shutdown();
        }
        instance = null;
    }

    /** Escapes dynamic Discord text without letting entity names break the payload. */
    public static String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
