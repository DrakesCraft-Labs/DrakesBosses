package cl.drakescraft.bosses.boss.combat;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import cl.drakescraft.bosses.DrakesBosses;
import cl.drakescraft.bosses.boss.OdysseyBoss;
import cl.drakescraft.bosses.boss.combat.BossCombatProfile.AttackFamily;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Runs bounded, telegraphed combat mechanics shared by every mythic boss. */
public final class BossCombatDirector implements BossCombatSupport {

    /** Los ataques disponibles, agrupados por familia. */
    private final Map<AttackFamily, List<BossAttack>> ataquesPorFamilia = new java.util.EnumMap<>(AttackFamily.class);
    private static final Particle.DustOptions AERIAL_DUST = new Particle.DustOptions(Color.fromRGB(92, 182, 255), 1.45F);
    private static final Particle.DustOptions GROUND_DUST = new Particle.DustOptions(Color.fromRGB(255, 118, 42), 1.55F);
    private static final Particle.DustOptions RANGED_DUST = new Particle.DustOptions(Color.fromRGB(174, 74, 255), 1.45F);

    private final DrakesBosses plugin;
    private final Map<UUID, Long> nextAttackAt = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> rotations = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> pendingAttacks = new ConcurrentHashMap<>();

    public BossCombatDirector(DrakesBosses plugin) {
        this.plugin = plugin;

        // El orden importa: define la rotacion dentro de cada familia.
        registrarAtaque(new cl.drakescraft.bosses.boss.combat.attack.StarfallAttack());
        registrarAtaque(new cl.drakescraft.bosses.boss.combat.attack.GravityWellAttack());
        registrarAtaque(new cl.drakescraft.bosses.boss.combat.attack.AirSlamAttack());
        registrarAtaque(new cl.drakescraft.bosses.boss.combat.attack.TempestCageAttack());
        registrarAtaque(new cl.drakescraft.bosses.boss.combat.attack.SkyLanceAttack());
        registrarAtaque(new cl.drakescraft.bosses.boss.combat.attack.CyclonePrisonAttack());
        registrarAtaque(new cl.drakescraft.bosses.boss.combat.attack.WarStompAttack());
        registrarAtaque(new cl.drakescraft.bosses.boss.combat.attack.VortexPullAttack());
        registrarAtaque(new cl.drakescraft.bosses.boss.combat.attack.ShieldBashAttack());
        registrarAtaque(new cl.drakescraft.bosses.boss.combat.attack.RuptureWaveAttack());
        registrarAtaque(new cl.drakescraft.bosses.boss.combat.attack.SeismicSpikesAttack());
        registrarAtaque(new cl.drakescraft.bosses.boss.combat.attack.GravitySlamAttack());
        registrarAtaque(new cl.drakescraft.bosses.boss.combat.attack.ChainLightningAttack());
        registrarAtaque(new cl.drakescraft.bosses.boss.combat.attack.SpiritBeamAttack());
        registrarAtaque(new cl.drakescraft.bosses.boss.combat.attack.ArcaneMissilesAttack());
        registrarAtaque(new cl.drakescraft.bosses.boss.combat.attack.HunterMarkAttack());
        registrarAtaque(new cl.drakescraft.bosses.boss.combat.attack.VoidRiftAttack());
        registrarAtaque(new cl.drakescraft.bosses.boss.combat.attack.SolarFlareAttack());
    }

    /** Selects one contextual attack while allowing at most one pending impact per boss. */
    public void tick(OdysseyBoss boss) {
        if (!plugin.getConfig().getBoolean("boss-balance.combat-director.enabled", true)) {
            return;
        }
        LivingEntity entity = boss.getEntity();
        UUID bossId = entity.getUniqueId();
        if (!isAlive(entity) || pendingAttacks.containsKey(bossId)) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now < nextAttackAt.getOrDefault(bossId, 0L)) {
            return;
        }

        double targetRange = Math.clamp(plugin.getConfig().getDouble(
                "boss-balance.combat-director.target-range", 44.0D), 16.0D, 72.0D);
        Player target = boss.nearestCombatTarget(targetRange);
        if (target == null) {
            return;
        }

        int rotation = rotations.merge(bossId, 1, Integer::sum) - 1;
        AttackFamily family = chooseFamily(boss, target, rotation);
        launch(boss, target, family, rotation);

        long baseCooldown = Math.clamp(plugin.getConfig().getLong(
                "boss-balance.combat-director.cooldown-seconds", 9L), 5L, 20L);
        long phaseReduction = Math.max(0, boss.getCurrentPhase() - 1) * 1000L;
        nextAttackAt.put(bossId, now + Math.max(4500L, baseCooldown * 1000L - phaseReduction));
    }

    public void cleanup(UUID bossId) {
        BukkitTask task = pendingAttacks.remove(bossId);
        if (task != null) {
            task.cancel();
        }
        nextAttackAt.remove(bossId);
        rotations.remove(bossId);
    }

    public void shutdown() {
        pendingAttacks.values().forEach(BukkitTask::cancel);
        pendingAttacks.clear();
        nextAttackAt.clear();
        rotations.clear();
    }

    private AttackFamily chooseFamily(OdysseyBoss boss, Player target, int rotation) {
        BossCombatProfile profile = BossCombatProfile.forBoss(boss.getId());
        List<AttackFamily> candidates = new ArrayList<>(profile.families());
        candidates.sort(Comparator.comparingInt(Enum::ordinal));
        double distanceSquared = target.getLocation().distanceSquared(boss.getEntity().getLocation());
        if (distanceSquared <= 64.0D && candidates.contains(AttackFamily.GROUND)) {
            return AttackFamily.GROUND;
        }
        if (distanceSquared >= 225.0D && candidates.contains(AttackFamily.RANGED)) {
            return AttackFamily.RANGED;
        }
        return candidates.get(Math.floorMod(rotation, candidates.size()));
    }

    @Override
    public Particle.DustOptions dustAereo() {
        return AERIAL_DUST;
    }

    @Override
    public Particle.DustOptions dustTerrestre() {
        return GROUND_DUST;
    }

    @Override
    public Particle.DustOptions dustDistancia() {
        return RANGED_DUST;
    }

    /**
     * Elige y lanza un ataque de la familia indicada.
     *
     * Antes esto era un switch con cadenas de "if variant == 0 ... else if variant == 1" por cada
     * familia. Anadir un ataque obligaba a tocar el switch, renumerar las variantes y confiar en
     * no descuadrar ninguna. Ahora los ataques se registran y aqui solo se elige uno.
     */
    private void launch(OdysseyBoss boss, Player target, AttackFamily family, int rotation) {
        List<BossAttack> disponibles = ataquesPorFamilia.get(family);
        if (disponibles == null || disponibles.isEmpty()) {
            return;
        }
        BossAttack ataque = disponibles.get(Math.floorMod(rotation / 2, disponibles.size()));
        ataque.execute(this, boss, target);
    }

    /** Registra un ataque para que entre en la rotacion de su familia. */
    public void registrarAtaque(BossAttack ataque) {
        ataquesPorFamilia.computeIfAbsent(ataque.getFamily(), f -> new ArrayList<>()).add(ataque);
    }





    /** Traps a marked area briefly without changing blocks or forcing chunk loads. */

    /** A high-damage vertical strike that is readable and never modifies terrain. */

    /** A short aerial lock that gives players a telegraphed window to reposition. */




    /** Expanding ground pulse that punishes players who remain around a melee boss. */

    /** Creates a temporary visual minefield without placing blocks or damaging structures. */

    /** Punishes a stationary target with a strong localized impact. */




    /** Marks one target and gives nearby combatants a short, visible escape window. */

    /** A short-lived void burst that pressures ranged players without block damage. */

    /** A visible flash that rewards looking away and staying mobile. */

    @Override
    public void telegraph(OdysseyBoss boss, String attackName, Location center,
                           Particle.DustOptions dust, Sound sound, Runnable impact) {
        if (center.getWorld() != boss.getEntity().getWorld()) return;
        boss.announceAttack(attackName);
        drawRing(center, 4.0D, dust);
        center.getWorld().playSound(center, sound, 0.9F, 0.85F);
        long delay = Math.clamp(plugin.getConfig().getLong(
                "boss-balance.combat-director.telegraph-ticks", 24L), 12L, 50L);
        scheduleImpact(boss, delay, impact);
    }

    private void scheduleImpact(OdysseyBoss boss, long delay, Runnable impact) {
        UUID bossId = boss.getEntity().getUniqueId();
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            pendingAttacks.remove(bossId);
            if (!isAlive(boss.getEntity())) return;
            try {
                impact.run();
            } catch (RuntimeException exception) {
                plugin.getLogger().warning("[BossDirector] Ataque de " + boss.getId()
                        + " cancelado: " + exception.getMessage());
            }
        }, delay);
        BukkitTask previous = pendingAttacks.put(bossId, task);
        if (previous != null) previous.cancel();
    }

    @Override
    public void damagePlayers(OdysseyBoss boss, Location center, double radius, double damage,
                               java.util.function.Consumer<Player> effect) {
        for (Player player : playersNear(center, radius)) {
            player.damage(boss.scaleArenaDamage(damage), boss.getEntity());
            effect.accept(player);
        }
    }

    @Override
    public List<Player> playersNear(Location center, double radius) {
        double radiusSquared = radius * radius;
        return center.getWorld().getPlayers().stream()
                .filter(player -> player.isOnline() && !player.isDead())
                .filter(player -> player.getGameMode() != org.bukkit.GameMode.CREATIVE)
                .filter(player -> player.getGameMode() != org.bukkit.GameMode.SPECTATOR)
                .filter(player -> player.getLocation().distanceSquared(center) <= radiusSquared)
                .sorted(Comparator.comparingDouble(player -> player.getLocation().distanceSquared(center)))
                .toList();
    }

    @Override
    public void drawRing(Location center, double radius, Particle.DustOptions dust) {
        for (int point = 0; point < 24; point++) {
            double angle = Math.PI * 2.0D * point / 24.0D;
            Location particle = center.clone().add(Math.cos(angle) * radius, 0.15D, Math.sin(angle) * radius);
            center.getWorld().spawnParticle(Particle.DUST, particle, 1, 0, 0, 0, 0, dust);
        }
    }

    @Override
    public void drawLine(Location start, Location end, Particle particle, Particle.DustOptions dust) {
        if (start.getWorld() != end.getWorld()) return;
        Vector path = end.toVector().subtract(start.toVector());
        double length = Math.min(40.0D, path.length());
        if (length < 0.05D) return;
        Vector step = path.normalize().multiply(0.65D);
        Location cursor = start.clone();
        for (double travelled = 0; travelled <= length; travelled += 0.65D) {
            if (dust == null) {
                start.getWorld().spawnParticle(particle, cursor, 1, 0, 0, 0, 0);
            } else {
                start.getWorld().spawnParticle(Particle.DUST, cursor, 1, 0, 0, 0, 0, dust);
                start.getWorld().spawnParticle(particle, cursor, 1, 0, 0, 0, 0);
            }
            cursor.add(step);
        }
    }

    private boolean isAlive(LivingEntity entity) {
        return entity != null && entity.isValid() && !entity.isDead();
    }
}
