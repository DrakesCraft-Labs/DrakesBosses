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
/** Ataque aerial: airSlam. Extraido de BossCombatDirector sin cambiar su comportamiento. */
public final class AirSlamAttack implements BossAttack {

    @Override
    public String getName() {
        return "airSlam";
    }

    @Override
    public AttackFamily getFamily() {
        return AttackFamily.AERIAL;
    }

    @Override
    public void execute(BossCombatSupport soporte, OdysseyBoss boss, Player target) {

    Location impact = target.getLocation().clone();
    soporte.telegraph(boss, "Impacto celeste", impact, soporte.dustAereo(), Sound.ENTITY_BREEZE_CHARGE, () -> {
        impact.getWorld().spawnParticle(Particle.GUST_EMITTER_LARGE, impact, 3, 1.5, 0.2, 1.5, 0.0);
        impact.getWorld().playSound(impact, Sound.ENTITY_BREEZE_WIND_BURST, 1.5F, 0.65F);
        soporte.damagePlayers(boss, impact, 6.0D, 11.0D, player -> {
            Vector away = player.getLocation().toVector().subtract(impact.toVector());
            if (away.lengthSquared() < 0.01D) away = new Vector(0.1D, 0, 0.1D);
            player.setVelocity(away.normalize().multiply(0.9D).setY(1.0D));
        });
    });
    }
}
