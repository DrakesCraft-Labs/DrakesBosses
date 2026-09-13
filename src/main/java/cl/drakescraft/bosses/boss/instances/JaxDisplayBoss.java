package cl.drakescraft.bosses.boss.instances;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import cl.drakescraft.bosses.DrakesBosses;
import cl.drakescraft.bosses.boss.OdysseyBoss;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Boss backed by a hidden living entity and a display-entity sculpture.
 * The living core keeps combat, boss bars and reward tracking compatible with BossManager.
 */
public final class JaxDisplayBoss extends OdysseyBoss {

    private static final String MODEL_RESOURCE = "models/jax-model.command";

    /** Una causa permanente (recurso ausente) avisaba en cada spawn; se recuerda por arranque. */
    private static final Set<String> WARNED_MODEL_CAUSES = ConcurrentHashMap.newKeySet();

    private BlockDisplay visualRoot;

    public JaxDisplayBoss(LivingEntity entity) {
        super(entity, "jax", "§5§lJax §7§l- §dEl Centinela Fragmentado",
                configuredDouble("health", 1350.0D, 100.0D, 4000.0D), BarColor.PURPLE, BarStyle.SEGMENTED_12);

        var attackDamage = entity.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.setBaseValue(configuredDouble("core-damage", 16.0D, 1.0D, 40.0D));
        }
        var scale = entity.getAttribute(Attribute.SCALE);
        if (scale != null) {
            scale.setBaseValue(1.15D);
        }

        scheduleVisual(entity.getLocation());
    }

    @Override
    public void executeSkillsRotation() {
        if (entity.isDead()) {
            return;
        }
        Player target = findNearestPlayer(configuredDouble("target-range", 28.0D, 8.0D, 64.0D));
        if (target == null) {
            idlePulse();
            return;
        }
        charge(target);
    }

    @Override
    public void tickAura() {
        super.tickAura();
        if (visualRoot == null || !visualRoot.isValid()) {
            return;
        }
        Location at = entity.getLocation().add(0.0D, 1.15D, 0.0D);
        entity.getWorld().spawnParticle(Particle.REVERSE_PORTAL, at, currentPhase + 1, 0.32D, 0.55D, 0.32D, 0.015D);
    }

    @Override
    public void cleanup() {
        removeVisual(visualRoot, new HashSet<>());
        visualRoot = null;
        super.cleanup();
    }

    private void idlePulse() {
        Location at = entity.getLocation().add(0.0D, 1.15D, 0.0D);
        entity.getWorld().spawnParticle(Particle.END_ROD, at, 4, 0.25D, 0.45D, 0.25D, 0.004D);
    }

    private void charge(Player target) {
        announceAttack("Embate fracturado");
        Location from = entity.getLocation();
        Vector direction = target.getLocation().toVector().subtract(from.toVector()).setY(0.0D);
        if (direction.lengthSquared() < 0.04D) {
            return;
        }
        double speed = configuredDouble("charge-speed", 1.05D, 0.25D, 2.0D);
        entity.setVelocity(direction.normalize().multiply(speed).setY(0.16D));
        from.getWorld().spawnParticle(Particle.ENCHANTED_HIT, from.clone().add(0.0D, 1.0D, 0.0D), 24, 0.55D, 0.8D, 0.55D, 0.08D);
        from.getWorld().playSound(from, Sound.ENTITY_RAVAGER_ROAR, 1.0F, 0.72F);

        Bukkit.getScheduler().runTaskLater(DrakesBosses.getInstance(), () -> impact(target), 10L);
    }

    private void impact(Player target) {
        if (entity.isDead() || !target.isOnline() || target.isDead() || target.getWorld() != entity.getWorld()) {
            return;
        }
        double radius = configuredDouble("impact-radius", 4.0D, 1.0D, 8.0D);
        if (target.getLocation().distanceSquared(entity.getLocation()) > radius * radius) {
            return;
        }
        double damage = scaleArenaDamage(configuredDouble("impact-damage", 11.0D, 0.0D, 30.0D));
        target.damage(damage, entity);
        Vector push = target.getLocation().toVector().subtract(entity.getLocation().toVector()).setY(0.0D);
        if (push.lengthSquared() < 0.01D) {
            push = new Vector(0.1D, 0.0D, 0.1D);
        }
        target.setVelocity(push.normalize().multiply(0.78D).setY(0.35D));
        Location at = entity.getLocation().add(0.0D, 1.0D, 0.0D);
        at.getWorld().spawnParticle(Particle.EXPLOSION, at, 10, 0.45D, 0.35D, 0.45D, 0.02D);
        at.getWorld().playSound(at, Sound.ENTITY_IRON_GOLEM_ATTACK, 1.2F, 0.72F);
    }

    /** Dispatches the display model, then attaches it after Paper registers passengers. */
    private void scheduleVisual(Location location) {
        String command = readModelCommand();
        if (command == null) {
            logModelFallback("falta el modelo: ni plugins/DrakesBosses/" + MODEL_RESOURCE
                    + " ni el recurso " + MODEL_RESOURCE + " del jar contienen un '"
                    + JaxModelCommand.REQUIRED_PREFIX + "...'");
            return;
        }
        World world = location.getWorld();
        if (world == null) {
            logModelFallback("el nucleo aparecio sin mundo asociado");
            return;
        }
        String marker = "odysseia_jax_" + entity.getUniqueId().toString().replace("-", "");
        String taggedCommand = JaxModelCommand.tagged(command, marker);
        if (taggedCommand == null) {
            logModelFallback("el modelo no trae compuesto NBT '{...}' donde insertar la etiqueta de rastreo");
            return;
        }
        // Sin 'execute in <mundo>' la consola invoca en su dimension por defecto (el overworld),
        // no en la arena: el display nacia fuera del mundo del boss, attachVisual no lo encontraba
        // y quedaba un BlockDisplay huerfano por cada spawn.
        String positioned = JaxModelCommand.positioned(world.getKey().toString(),
                location.getX(), location.getY(), location.getZ(), taggedCommand);
        if (!Bukkit.dispatchCommand(Bukkit.getConsoleSender(), positioned)) {
            logModelFallback("la consola rechazo el summon del modelo");
            return;
        }
        Bukkit.getScheduler().runTaskLater(DrakesBosses.getInstance(), () -> attachVisual(world, marker), 1L);
    }

    private void attachVisual(World world, String marker) {
        BlockDisplay root = world.getEntitiesByClass(BlockDisplay.class).stream()
                .filter(display -> display.getScoreboardTags().contains(marker))
                .findFirst()
                .orElse(null);
        if (root == null || entity.isDead()) {
            if (root != null) root.remove();
            logModelFallback(root == null
                    ? "el display etiquetado no aparecio en " + world.getName() + " al tick siguiente"
                    : "el nucleo murio antes de poder vestirlo");
            return;
        }
        visualRoot = root;
        root.setTeleportDuration(2);
        root.setInterpolationDuration(2);
        entity.addPassenger(root);
        entity.setInvisible(true);
        entity.setSilent(true);
        entity.setCustomNameVisible(false);
    }

    /**
     * El aviso decia solo "no pudo cargar su modelo", sin distinguir cual de las cinco rutas fallo:
     * un recurso ausente (condicion permanente) se leia igual que la carrera de un tick con Paper.
     * Ahora cada causa se nombra y se recuerda, para que una condicion permanente avise una vez por
     * arranque en vez de una vez por spawn.
     */
    private void logModelFallback(String cause) {
        if (!WARNED_MODEL_CAUSES.add(cause)) {
            return;
        }
        DrakesBosses.getInstance().getLogger().warning(
                "[Bosses] Jax no pudo cargar su modelo (" + cause
                        + "); el núcleo sigue visible para evitar un boss invisible.");
    }

    /**
     * Lee el modelo de {@code plugins/DrakesBosses/models/jax-model.command} y, si no esta, del jar.
     * La copia en disco permite reponer o retocar la escultura sin recompilar el plugin; hoy el
     * recurso del jar no existe, asi que esa es la unica via activa.
     */
    private String readModelCommand() {
        Path override = DrakesBosses.getInstance().getDataFolder().toPath().resolve(MODEL_RESOURCE);
        if (Files.isRegularFile(override)) {
            try {
                return validModelCommand(Files.readString(override, StandardCharsets.UTF_8));
            } catch (IOException exception) {
                DrakesBosses.getInstance().getLogger()
                        .warning("[Bosses] No se pudo leer " + override + ": " + exception.getMessage());
            }
        }
        try (InputStream stream = DrakesBosses.getInstance().getResource(MODEL_RESOURCE)) {
            if (stream == null) {
                return null;
            }
            return validModelCommand(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException exception) {
            DrakesBosses.getInstance().getLogger().warning("[Bosses] No se pudo leer el modelo de Jax: " + exception.getMessage());
            return null;
        }
    }

    private static String validModelCommand(String raw) {
        String command = raw.trim();
        return command.startsWith(JaxModelCommand.REQUIRED_PREFIX) ? command : null;
    }

    private void removeVisual(Entity visual, Set<UUID> visited) {
        if (visual == null || !visited.add(visual.getUniqueId())) {
            return;
        }
        for (Entity passenger : visual.getPassengers()) {
            removeVisual(passenger, visited);
        }
        visual.remove();
    }

    private static double configuredDouble(String key, double fallback, double minimum, double maximum) {
        return Math.clamp(DrakesBosses.getInstance().getConfig().getDouble("bosses.jax." + key, fallback), minimum, maximum);
    }
}
