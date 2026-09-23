package cl.drakescraft.bosses.boss.ritual;

import cl.drakescraft.bosses.DrakesBosses;
import cl.drakescraft.bosses.boss.BossManager;
import cl.drakescraft.bosses.boss.OdysseyBoss;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor de Altares Físicos e Invocaciones Rituales en el Mundo para DrakesBosses.
 * Permite invocar a los 26 Dioses mediante ofrendas en estructuras físicas en el Overworld, Nether y End,
 * sin depender exclusivamente del comando /bosswarp.
 */
public final class BossAltarManager implements Listener {

    private final DrakesBosses plugin;
    private final BossManager bossManager;
    private final Map<UUID, Long> altarCooldowns = new ConcurrentHashMap<>();

    // Mapeo canónico de ofrendas para invocar Dioses
    private static final Map<Material, String> OFFERING_TO_BOSS = Map.ofEntries(
            Map.entry(Material.LIGHTNING_ROD, "zeus"),
            Map.entry(Material.HEART_OF_THE_SEA, "poseidon"),
            Map.entry(Material.WITHER_SKELETON_SKULL, "hades"),
            Map.entry(Material.NETHERITE_SWORD, "ares"),
            Map.entry(Material.SPECTRAL_ARROW, "artemisa"),
            Map.entry(Material.FIRE_CHARGE, "prometeo"),
            Map.entry(Material.AMETHYST_SHARD, "circe"),
            Map.entry(Material.FERMENTED_SPIDER_EYE, "polifemo"),
            Map.entry(Material.IRON_BLOCK, "thor"),
            Map.entry(Material.ENDER_PEARL, "loki"),
            Map.entry(Material.GOLD_BLOCK, "odin"),
            Map.entry(Material.BLAZE_ROD, "heimdall"),
            Map.entry(Material.NETHERITE_AXE, "kratos"),
            Map.entry(Material.PRISMARINE_SHARD, "hidra"),
            Map.entry(Material.BONE_BLOCK, "cerbero"),
            Map.entry(Material.MAGMA_BLOCK, "tifon"),
            Map.entry(Material.ECHO_SHARD, "coloso_end"),
            Map.entry(Material.NETHER_STAR, "garou_cosmico"),
            Map.entry(Material.GOLDEN_CARROT, "ra"),
            Map.entry(Material.FEATHER, "isis"),
            Map.entry(Material.ROTTEN_FLESH, "anubis"),
            Map.entry(Material.REDSTONE_BLOCK, "set")
    );

    public BossAltarManager(DrakesBosses plugin, BossManager bossManager) {
        this.plugin = plugin;
        this.bossManager = bossManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAltarInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clicked = event.getClickedBlock();
        if (clicked == null || !isValidAltarCore(clicked.getType())) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack held = event.getItem();
        if (held == null || held.getType().isAir()) {
            return;
        }

        String bossType = OFFERING_TO_BOSS.get(held.getType());
        if (bossType == null) {
            return;
        }

        if (!plugin.getConfig().getBoolean("altars.enabled", true)) {
            return;
        }

        World world = clicked.getWorld();
        if (world.getName().equalsIgnoreCase("drakes_bosses")) {
            // En el mundo de arenas /bosswarp no se usan altares
            return;
        }

        UUID playerId = player.getUniqueId();
        long now = System.currentTimeMillis();
        long cooldownEnd = altarCooldowns.getOrDefault(playerId, 0L);
        if (now < cooldownEnd) {
            long remainingSec = (cooldownEnd - now) / 1000L;
            player.sendMessage(ChatColor.RED + "El altar aún está canalizando energía residual. Espera " + remainingSec + "s.");
            return;
        }

        // Validar presencia de velas, antorchas o pilares rituales alrededor
        if (!hasRitualSurroundings(clicked)) {
            player.sendMessage(ChatColor.YELLOW + "El altar está incompleto. Coloca al menos 2 velas, antorchas o pilares alrededor para canalizar la energía.");
            return;
        }

        // Consumir la ofrenda (1 unidad)
        if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            held.setAmount(held.getAmount() - 1);
        }

        altarCooldowns.put(playerId, now + 30_000L); // 30s de cooldown por jugador

        Location altarCenter = clicked.getLocation().add(0.5, 1.0, 0.5);
        executeInvocationRitual(world, altarCenter, player, bossType);
    }

    private boolean isValidAltarCore(Material mat) {
        return mat == Material.CRYING_OBSIDIAN
                || mat == Material.LODESTONE
                || mat == Material.RESPAWN_ANCHOR
                || mat == Material.BEACON
                || mat == Material.CHISELED_POLISHED_BLACKSTONE
                || mat == Material.GILDED_BLACKSTONE;
    }

    private boolean hasRitualSurroundings(Block core) {
        int ritualAnchors = 0;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) continue;
                Material m = core.getRelative(dx, 0, dz).getType();
                Material mAbove = core.getRelative(dx, 1, dz).getType();
                if (isRitualComponent(m) || isRitualComponent(mAbove)) {
                    ritualAnchors++;
                }
            }
        }
        return ritualAnchors >= 2;
    }

    private boolean isRitualComponent(Material m) {
        return m == Material.RED_CANDLE
                || m == Material.SOUL_TORCH
                || m == Material.SOUL_LANTERN
                || m == Material.SOUL_CAMPFIRE
                || m == Material.END_ROD
                || m == Material.GOLD_BLOCK
                || m == Material.PURPLE_CANDLE
                || m == Material.BLACK_CANDLE;
    }

    private void executeInvocationRitual(World world, Location loc, Player summoner, String bossType) {
        // Efectos de partículas en anillo pentagrama
        world.playSound(loc, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 2.0F, 0.8F);
        world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5F, 0.9F);

        for (int i = 0; i < 36; i++) {
            double angle = (i * Math.PI) / 18.0;
            double x = Math.cos(angle) * 3.5;
            double z = Math.sin(angle) * 3.5;
            world.spawnParticle(Particle.FLAME, loc.clone().add(x, 0.2, z), 2, 0, 0.05, 0, 0.02);
            world.spawnParticle(Particle.SOUL_FIRE_FLAME, loc.clone().add(x * 0.7, 0.5, z * 0.7), 1, 0, 0.05, 0, 0.01);
        }

        world.spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1);

        // Despertar al jefe
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            OdysseyBoss boss = bossManager.spawnBoss(bossType, loc);
            if (boss != null) {
                String bossName = boss.getDisplayName();
                summoner.sendTitle(
                        ChatColor.GOLD + "✦ " + bossName + " ✦",
                        ChatColor.RED + "¡Ha respondido a tu ofrenda sagrada!",
                        10, 50, 20
                );
                world.strikeLightningEffect(loc);
                plugin.getLogger().info("[AltarRitual] " + summoner.getName() + " invocó a " + bossType + " en " + loc.toVector());
            } else {
                summoner.sendMessage(ChatColor.RED + "La energía de la ofrenda se disipó sin despertar al Dios.");
            }
        }, 30L);
    }
}
