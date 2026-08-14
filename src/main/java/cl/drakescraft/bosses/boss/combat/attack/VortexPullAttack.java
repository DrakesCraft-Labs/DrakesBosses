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
/** Ataque ground: vortexPull. Extraido de BossCombatDirector sin cambiar su comportamiento. */
public final class VortexPullAttack implements BossAttack {

    @Override
    public String getName() {
        return "vortexPull";
    }

    @Override
    public AttackFamily getFamily() {
        return AttackFamily.GROUND;
    }

    @Override
    public void execute(BossCombatSupport soporte, OdysseyBoss boss, Player target) {

    Location center = boss.getEntity().getLocation().clone();
    soporte.telegraph(boss, "Vórtice de cadenas", center, soporte.dustTerrestre(), Sound.BLOCK_CHAIN_PLACE, () -> {
        center.getWorld().spawnParticle(Particle.WITCH, center.clone().add(0, 1, 0), 65, 4.5, 1.0, 4.5, 0.12);
        for (Player player : soporte.playersNear(center, 10.0D)) {
            Vector pull = center.toVector().subtract(player.getLocation().toVector());
            if (pull.lengthSquared() > 0.05D) {
                player.setVelocity(pull.normalize().multiply(1.2D).setY(0.18D));
            }
            player.damage(player.equals(target) ? 10.0D : 7.0D, boss.getEntity());
        }
    });
    }
}
