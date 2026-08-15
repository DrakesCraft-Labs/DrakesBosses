package cl.drakescraft.bosses.boss.combat;

import cl.drakescraft.bosses.DrakesBosses;
import cl.drakescraft.bosses.boss.OdysseyBoss;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Applies a bounded response only when a complete Infinity Singularity Tinker
 * set completely nullifies boss damage. It never edits equipment or kills a player.
 */
public final class FullInfinityArmorCounter {

    private static final Set<String> ARMOUR_MATERIAL_KEYS = Set.of(
            "st_material_plate", "st_material_links", "st_material_gambeson");
    private final DrakesBosses plugin;
    private final Map<UUID, Map<UUID, Long>> cooldowns = new ConcurrentHashMap<>();

    public FullInfinityArmorCounter(DrakesBosses plugin) {
        this.plugin = plugin;
    }

    /** Responds after other plugins have resolved the original boss hit. */
    public void applyIfFullyNegated(OdysseyBoss boss, Player player, EntityDamageByEntityEvent event) {
        String prefix = "boss-balance.adaptive-counters.full-infinity-armor";
        if (!plugin.getConfig().getBoolean(prefix + ".enabled", true)
                || !isFullInfinitySingularitySet(player.getInventory().getArmorContents())) {
            return;
        }

        double threshold = Math.max(0.0D, plugin.getConfig().getDouble(prefix + ".final-damage-threshold", 0.05D));
        if (!event.isCancelled() && event.getFinalDamage() > threshold) {
            return;
        }

        long now = System.currentTimeMillis();
        long cooldownMillis = Math.max(1L, plugin.getConfig().getLong(prefix + ".cooldown-seconds", 5L)) * 1_000L;
        Map<UUID, Long> bossCooldowns = cooldowns.computeIfAbsent(boss.getEntity().getUniqueId(), ignored -> new ConcurrentHashMap<>());
        if (now < bossCooldowns.getOrDefault(player.getUniqueId(), 0L)) {
            return;
        }
        bossCooldowns.put(player.getUniqueId(), now + cooldownMillis);

        double minimumHealth = Math.max(1.0D, plugin.getConfig().getDouble(prefix + ".minimum-health", 4.0D));
        double resultingHealth = calculateResultingHealth(
                player.getHealth(),
                minimumHealth,
                plugin.getConfig().getDouble(prefix + ".minimum-health-loss", 2.0D),
                plugin.getConfig().getDouble(prefix + ".health-loss-fraction", 0.50D));
        if (resultingHealth < player.getHealth()) {
            player.setHealth(resultingHealth);
        }

        player.setAbsorptionAmount(Math.max(0.0D, player.getAbsorptionAmount() - 2.0D));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,
                Math.max(20, plugin.getConfig().getInt(prefix + ".weakness-ticks", 80)), 0, true, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
                Math.max(20, plugin.getConfig().getInt(prefix + ".slowness-ticks", 40)), 0, true, true, true));
        player.getWorld().spawnParticle(Particle.SCULK_SOUL, player.getLocation().add(0, 1.0D, 0), 28, 0.35D, 0.65D, 0.35D, 0.03D);
        player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, player.getLocation().add(0, 1.0D, 0), 14, 0.3D, 0.5D, 0.3D, 0.04D);
        player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.9F, 0.6F);
        player.sendTitle(
                ChatColor.DARK_RED + "PODER DIVINO",
                ChatColor.GOLD + "Tu infinidad no es nada frente a mi poder divino.",
                8,
                50,
                16);
        player.sendActionBar(ChatColor.DARK_PURPLE + "El jefe quebranta tu Infinity Singularity: tu defensa no es absoluta.");
    }

    /** Removes per-boss cooldown entries when an encounter is cleaned up. */
    public void cleanup(UUID bossId) {
        cooldowns.remove(bossId);
    }

    static boolean isFullInfinitySingularitySet(ItemStack[] armour) {
        if (armour == null || armour.length != 4) {
            return false;
        }
        for (ItemStack piece : armour) {
            if (piece == null || piece.getType().isAir() || !hasInfinitySingularityMaterial(piece)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasInfinitySingularityMaterial(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().getKeys().stream()
                .filter(key -> isInfinityArmourMaterialKey(key.getKey()))
                .map(key -> meta.getPersistentDataContainer().get(key, PersistentDataType.STRING))
                .anyMatch(FullInfinityArmorCounter::isInfinitySingularityMaterialId);
    }

    /** SlimeTinker 1.21 stores PDC key paths in lowercase as required by Paper. */
    static boolean isInfinityArmourMaterialKey(String key) {
        return key != null && ARMOUR_MATERIAL_KEYS.contains(key.toLowerCase(java.util.Locale.ROOT));
    }

    static boolean isInfinitySingularityMaterialId(String materialId) {
        return materialId != null && "INFINITY_SINGULARITY".equalsIgnoreCase(materialId);
    }

    /** Calculates the punitive health reduction while preserving a recoverable floor. */
    static double calculateResultingHealth(double currentHealth, double minimumHealth, double minimumLoss, double lossFraction) {
        double healthLoss = Math.max(Math.max(0.0D, minimumLoss),
                currentHealth * Math.clamp(lossFraction, 0.01D, 0.95D));
        return Math.max(Math.max(1.0D, minimumHealth), currentHealth - healthLoss);
    }
}
