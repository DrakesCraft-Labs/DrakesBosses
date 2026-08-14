package cl.drakescraft.bosses.boss.combat.attack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import cl.drakescraft.bosses.boss.OdysseyBoss;
import cl.drakescraft.bosses.boss.combat.BossAttack;
import cl.drakescraft.bosses.boss.combat.BossCombatProfile.AttackFamily;
import cl.drakescraft.bosses.boss.combat.BossCombatSupport;
/** Ataque aerial: tempestCage. Extraido de BossCombatDirector sin cambiar su comportamiento. */
public final class TempestCageAttack implements BossAttack {

    @Override
    public String getName() {
        return "tempestCage";
    }

    @Override
    public AttackFamily getFamily() {
        return AttackFamily.AERIAL;
    }

    @Override
    public void execute(BossCombatSupport soporte, OdysseyBoss boss, Player target) {

    Location center = target.getLocation().clone();
    soporte.telegraph(boss, "Prisión de tormenta", center, soporte.dustAereo(), Sound.ENTITY_WARDEN_SONIC_BOOM, () -> {
        soporte.drawRing(center, 6.0D, soporte.dustAereo());
        center.getWorld().spawnParticle(Particle.GUST, center.clone().add(0, 1, 0), 18, 2.5, 1.2, 2.5, 0.08);
        for (Player player : soporte.playersNear(center, 6.0D)) {
            Vector away = player.getLocation().toVector().subtract(center.toVector());
            if (away.lengthSquared() < 0.01D) away = new Vector(0.1D, 0, 0.1D);
            player.setVelocity(away.normalize().multiply(0.95D).setY(0.45D));
            player.damage(10.0D, boss.getEntity());
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 70, 0));
        }
    });
    }
}
