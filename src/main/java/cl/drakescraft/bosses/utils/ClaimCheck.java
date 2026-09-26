package cl.drakescraft.bosses.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.lang.reflect.Method;

/**
 * Utilidad fail-closed para verificar si una ubicación pertenece a un claim
 * de ProtectionStones o WorldGuard.
 */
public final class ClaimCheck {

    private static Method psFromLocationMethod;
    private static boolean psLookupAttempted = false;

    private ClaimCheck() {}

    public static boolean isProtected(Location location) {
        if (location == null || location.getWorld() == null) return false;

        // 1. ProtectionStones
        if (!psLookupAttempted) {
            psLookupAttempted = true;
            try {
                Class<?> psRegionClass = Class.forName("dev.espi.protectionstones.PSRegion");
                psFromLocationMethod = psRegionClass.getMethod("fromLocation", Location.class);
            } catch (Throwable ignored) {
                psFromLocationMethod = null;
            }
        }

        if (psFromLocationMethod != null) {
            try {
                Object psRegion = psFromLocationMethod.invoke(null, location);
                if (psRegion != null) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        // 2. WorldGuard
        try {
            if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
                Class<?> worldGuardType = Class.forName("com.sk89q.worldguard.WorldGuard");
                Object worldGuard = worldGuardType.getMethod("getInstance").invoke(null);
                Object platform = worldGuard.getClass().getMethod("getPlatform").invoke(worldGuard);
                Object container = platform.getClass().getMethod("getRegionContainer").invoke(platform);
                Object query = container.getClass().getMethod("createQuery").invoke(container);
                Class<?> adapter = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
                Object adapted = adapter.getMethod("adapt", Location.class).invoke(null, location);
                Class<?> worldEditLocation = Class.forName("com.sk89q.worldedit.util.Location");
                Object regions = query.getClass().getMethod("getApplicableRegions", worldEditLocation).invoke(query, adapted);
                int size = ((Number) regions.getClass().getMethod("size").invoke(regions)).intValue();
                if (size > 0) {
                    return true;
                }
            }
        } catch (Throwable ignored) {}

        return false;
    }

    public static boolean isAreaProtected(Location center, double radius) {
        if (center == null || center.getWorld() == null) return false;
        if (isProtected(center)) return true;
        // Sample surrounding perimeter
        for (int i = 0; i < 8; i++) {
            double angle = (Math.PI * 2.0 * i) / 8.0;
            Location sample = center.clone().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
            if (isProtected(sample)) {
                return true;
            }
        }
        return false;
    }
}
