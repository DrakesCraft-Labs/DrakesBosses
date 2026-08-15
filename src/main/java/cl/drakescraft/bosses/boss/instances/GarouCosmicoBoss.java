package cl.drakescraft.bosses.boss.instances;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import cl.drakescraft.bosses.boss.OdysseyBoss;

import java.util.List;
import java.util.Random;

/**
 * Garou Cosmico no depende de comandos ni destruye terreno: lee la distancia del
 * objetivo, alterna posturas y aumenta su presion en cada fase.
 */
public final class GarouCosmicoBoss extends OdysseyBoss {

    private static final Particle.DustOptions COSMIC_DUST = new Particle.DustOptions(Color.fromRGB(122, 76, 255), 1.8F);
    private final Random random = new Random();
    private int adaptationsUsed;

    public GarouCosmicoBoss(LivingEntity entity) {
        super(entity, "garou_cosmico", "§5§lGarou Cósmico §7§l- §dCazador de Héroes", 4200.0D, BarColor.PURPLE, BarStyle.SEGMENTED_20);

        setAttribute(Attribute.ATTACK_DAMAGE, 42.0D);
        setAttribute(Attribute.ARMOR, 18.0D);
        setAttribute(Attribute.KNOCKBACK_RESISTANCE, 1.0D);
        setAttribute(Attribute.FOLLOW_RANGE, 72.0D);
        setAttribute(Attribute.MOVEMENT_SPEED, 0.42D);
        setAttribute(Attribute.SCALE, 1.18D);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 60 * 20, 0, false, false, false));
    }

    @Override
    public void executeSkillsRotation() {
        if (entity.isDead()) return;
        Player target = findNearestPlayer(52.0D);
        if (target == null) return;

        double distance = entity.getLocation().distanceSquared(target.getLocation());
        if (currentPhase == 1) {
            if (distance > 144.0D) predictiveRush(target);
            else martialCounter(target);
            return;
        }
        if (currentPhase == 2) {
            switch (random.nextInt(3)) {
                case 0 -> predictiveRush(target);
                case 1 -> cosmicPressure(target.getLocation());
                default -> stellarMirror(target);
            }
            return;
        }
        switch (random.nextInt(4)) {
            case 0 -> cosmicPressure(target.getLocation());
            case 1 -> stellarMirror(target);
            case 2 -> martialCounter(target);
            default -> predictiveRush(target);
        }
    }

    @Override
    protected void onPhaseChange(int phase) {
        super.onPhaseChange(phase);
        adaptToCombat(phase);
    }

    @Override
    public void tickAura() {
        super.tickAura();
        if (currentPhase < 2 || entity.isDead()) return;
        Location center = entity.getLocation().add(0, 1.15D, 0);
        entity.getWorld().spawnParticle(Particle.DUST, center, currentPhase * 6, 0.7D, 1.0D, 0.7D, 0.01D, COSMIC_DUST);
        if (currentPhase == 3) {
            entity.getWorld().spawnParticle(Particle.REVERSE_PORTAL, center, 8, 0.65D, 1.0D, 0.65D, 0.03D);
        }
    }

    /** Pone presion al jugador lejano con un salto fisico, no con teletransporte inseguro. */
    private void predictiveRush(Player target) {
        Location origin = entity.getLocation();
        Vector direction = target.getVelocity().clone().multiply(7.0D)
                .add(target.getLocation().toVector().subtract(origin.toVector()));
        if (direction.lengthSquared() < 0.01D) return;
        entity.setVelocity(direction.normalize().multiply(1.35D + currentPhase * 0.15D).setY(0.32D));
        entity.getWorld().spawnParticle(Particle.DUST, origin.add(0, 1, 0), 28, 0.45D, 0.6D, 0.45D, 0.08D, COSMIC_DUST);
        entity.getWorld().playSound(origin, Sound.ENTITY_ENDERMAN_SCREAM, 0.8F, 0.55F);
        announceAttack("Caza predicativa");
    }

    /** Castiga el cuerpo a cuerpo sin bloquear al jugador ni alterar bloques. */
    private void martialCounter(Player target) {
        Location center = entity.getLocation().add(0, 1.0D, 0);
        announceAttack("Arte marcial cósmico");
        entity.getWorld().spawnParticle(Particle.SWEEP_ATTACK, center, 8, 1.7D, 0.4D, 1.7D, 0.0D);
        entity.getWorld().playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.2F, 0.55F);
        for (Player player : findPlayersInRange(6.0D)) {
            Vector knockback = player.getLocation().toVector().subtract(entity.getLocation().toVector());
            if (knockback.lengthSquared() < 0.01D) knockback = new Vector(0.2D, 0.0D, 0.2D);
            player.setVelocity(knockback.normalize().multiply(1.15D).setY(0.38D));
            player.damage(scaleArenaDamage(14.0D + currentPhase * 4.0D), entity);
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 45, Math.max(0, currentPhase - 2), true, true, true));
        }
    }

    /** Deja una zona de daño breve y legible, sin fuego, lava ni cambios del mundo. */
    private void cosmicPressure(Location target) {
        Location center = target.clone().add(0, 0.15D, 0);
        announceAttack("Colapso de energía cósmica");
        for (int point = 0; point < 30; point++) {
            double angle = Math.PI * 2.0D * point / 30.0D;
            center.getWorld().spawnParticle(Particle.DUST, center.clone().add(Math.cos(angle) * 5.5D, 0.1D, Math.sin(angle) * 5.5D),
                    1, 0, 0, 0, 0, COSMIC_DUST);
        }
        center.getWorld().playSound(center, Sound.BLOCK_BEACON_POWER_SELECT, 1.2F, 0.45F);
        for (Player player : findPlayersInRange(10.0D)) {
            if (player.getLocation().distanceSquared(center) > 30.25D) continue;
            player.damage(scaleArenaDamage(18.0D + currentPhase * 3.0D), entity);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 50, 1, true, true, true));
        }
    }

    /** Replica una linea de ataque visible para impedir que el rango sea una zona segura. */
    private void stellarMirror(Player target) {
        Location start = entity.getLocation().add(0, 1.2D, 0);
        Vector path = target.getLocation().add(0, 1.0D, 0).toVector().subtract(start.toVector());
        if (path.lengthSquared() < 0.01D) return;
        Vector step = path.normalize().multiply(0.65D);
        announceAttack("Puño que imita las estrellas");
        for (double traveled = 0.0D; traveled < Math.min(34.0D, path.length()); traveled += 0.65D) {
            Location point = start.clone().add(step.clone().multiply(traveled / 0.65D));
            point.getWorld().spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, COSMIC_DUST);
            for (Player player : point.getWorld().getNearbyPlayers(point, 1.25D)) {
                player.damage(scaleArenaDamage(17.0D + currentPhase * 3.0D), entity);
                player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 50, 0, true, true, true));
            }
        }
        start.getWorld().playSound(start, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.75F, 1.45F);
    }

    /** Una adaptación por fase: resistente y visual, no inmunidad repetitiva. */
    private void adaptToCombat(int phase) {
        if (adaptationsUsed >= phase) return;
        adaptationsUsed = phase;
        Location center = entity.getLocation().add(0, 1, 0);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, Math.min(4, phase + 1), false, false, false));
        entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60 * 10, phase, false, false, false));
        center.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, center, 80, 1.2D, 1.7D, 1.2D, 0.08D);
        center.getWorld().spawnParticle(Particle.REVERSE_PORTAL, center, 90, 1.0D, 1.4D, 1.0D, 0.05D);
        center.getWorld().playSound(center, Sound.ENTITY_WARDEN_EMERGE, 1.4F, 0.65F);
        speak("He observado suficiente. Ahora conozco vuestro ritmo.");
    }

    private void setAttribute(Attribute attribute, double value) {
        var instance = entity.getAttribute(attribute);
        if (instance != null) instance.setBaseValue(value);
    }
}
