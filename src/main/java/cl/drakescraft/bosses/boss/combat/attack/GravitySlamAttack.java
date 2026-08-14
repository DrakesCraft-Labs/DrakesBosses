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
/** Ataque ground: gravitySlam. Extraido de BossCombatDirector sin cambiar su comportamiento. */
public final class GravitySlamAttack implements BossAttack {

    @Override
    public String getName() {
        return "gravitySlam";
    }

    @Override
    public AttackFamily getFamily() {
        return AttackFamily.GROUND;
    }

    @Override
    public void execute(BossCombatSupport soporte, OdysseyBoss boss, Player target) {

    Location impact = target.getLocation().clone();
    soporte.telegraph(boss, "Martillo gravitatorio", impact, soporte.dustTerrestre(), Sound.ENTITY_IRON_GOLEM_ATTACK, () -> {
        impact.getWorld().spawnParticle(Particle.CRIT, impact.clone().add(0, 1, 0), 70, 1.2, 1.0, 1.2, 0.15);
        impact.getWorld().playSound(impact, Sound.ENTITY_GENERIC_EXPLODE, 1.25F, 0.55F);
        soporte.damagePlayers(boss, impact, 5.0D, 19.0D, player -> {
            Vector away = player.getLocation().toVector().subtract(impact.toVector());
            if (away.lengthSquared() < 0.01D) away = new Vector(0.1D, 0, 0.1D);
            player.setVelocity(away.normalize().multiply(1.25D).setY(0.8D));
        });
    });
    }
}
