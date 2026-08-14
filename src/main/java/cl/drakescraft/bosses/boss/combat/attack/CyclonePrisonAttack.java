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
/** Ataque aerial: cyclonePrison. Extraido de BossCombatDirector sin cambiar su comportamiento. */
public final class CyclonePrisonAttack implements BossAttack {

    @Override
    public String getName() {
        return "cyclonePrison";
    }

    @Override
    public AttackFamily getFamily() {
        return AttackFamily.AERIAL;
    }

    @Override
    public void execute(BossCombatSupport soporte, OdysseyBoss boss, Player target) {

    Location center = target.getLocation().clone();
    soporte.telegraph(boss, "Prisión ciclónica", center, soporte.dustAereo(), Sound.ENTITY_BREEZE_WIND_BURST, () -> {
        soporte.drawRing(center, 7.5D, soporte.dustAereo());
        center.getWorld().spawnParticle(Particle.CLOUD, center.clone().add(0, 1, 0), 80, 3.5, 1.5, 3.5, 0.08);
        for (Player player : soporte.playersNear(center, 7.5D)) {
            player.damage(boss.scaleArenaDamage(12.0D), boss.getEntity());
            player.setVelocity(player.getVelocity().setY(0.72D));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 50, 0));
        }
    });
    }
}
