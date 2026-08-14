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
/** Ataque aerial: gravityWell. Extraido de BossCombatDirector sin cambiar su comportamiento. */
public final class GravityWellAttack implements BossAttack {

    @Override
    public String getName() {
        return "gravityWell";
    }

    @Override
    public AttackFamily getFamily() {
        return AttackFamily.AERIAL;
    }

    @Override
    public void execute(BossCombatSupport soporte, OdysseyBoss boss, Player target) {

    Location center = target.getLocation().clone();
    soporte.telegraph(boss, "Pozo gravitatorio", center, soporte.dustAereo(), Sound.ENTITY_ILLUSIONER_PREPARE_BLINDNESS, () -> {
        center.getWorld().spawnParticle(Particle.PORTAL, center.clone().add(0, 1, 0), 90, 3.0, 1.2, 3.0, 0.15);
        for (Player player : soporte.playersNear(center, 9.0D)) {
            Vector pull = center.toVector().subtract(player.getLocation().toVector());
            if (pull.lengthSquared() > 0.05D) {
                player.setVelocity(pull.normalize().multiply(1.05D).setY(0.22D));
            }
            player.damage(boss.scaleArenaDamage(9.0D), boss.getEntity());
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 50, 2));
        }
    });
    }
}
