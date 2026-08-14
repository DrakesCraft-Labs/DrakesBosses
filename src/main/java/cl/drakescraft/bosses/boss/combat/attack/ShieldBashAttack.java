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
/** Ataque ground: shieldBash. Extraido de BossCombatDirector sin cambiar su comportamiento. */
public final class ShieldBashAttack implements BossAttack {

    @Override
    public String getName() {
        return "shieldBash";
    }

    @Override
    public AttackFamily getFamily() {
        return AttackFamily.GROUND;
    }

    @Override
    public void execute(BossCombatSupport soporte, OdysseyBoss boss, Player target) {

    Location targetLocation = target.getLocation().clone();
    soporte.telegraph(boss, "Embestida de escudo", targetLocation, soporte.dustTerrestre(), Sound.ITEM_SHIELD_BLOCK, () -> {
        if (!target.isOnline() || target.getWorld() != boss.getEntity().getWorld()) return;
        Vector away = target.getLocation().toVector().subtract(boss.getEntity().getLocation().toVector());
        if (away.lengthSquared() < 0.01D) away = target.getLocation().getDirection();
        target.setVelocity(away.normalize().multiply(1.6D).setY(0.55D));
        target.damage(boss.scaleArenaDamage(15.0D), boss.getEntity());
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1));
        target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 30, 0.4, 0.7, 0.4, 0.15);
    });
    }
}
