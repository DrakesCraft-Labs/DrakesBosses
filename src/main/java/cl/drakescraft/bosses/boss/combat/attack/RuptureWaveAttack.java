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
/** Ataque ground: ruptureWave. Extraido de BossCombatDirector sin cambiar su comportamiento. */
public final class RuptureWaveAttack implements BossAttack {

    @Override
    public String getName() {
        return "ruptureWave";
    }

    @Override
    public AttackFamily getFamily() {
        return AttackFamily.GROUND;
    }

    @Override
        // Este ataque no apunta a nadie en concreto: golpea alrededor del jefe.
    public void execute(BossCombatSupport soporte, OdysseyBoss boss, Player target) {

    Location center = boss.getEntity().getLocation().clone();
    soporte.telegraph(boss, "Onda de ruptura", center, soporte.dustTerrestre(), Sound.ENTITY_WARDEN_ATTACK_IMPACT, () -> {
        soporte.drawRing(center, 11.0D, soporte.dustTerrestre());
        center.getWorld().spawnParticle(Particle.LAVA, center.clone().add(0, 0.2, 0), 42, 4.5, 0.15, 4.5, 0.03);
        soporte.damagePlayers(boss, center, 11.0D, 14.0D, player -> {
            Vector away = player.getLocation().toVector().subtract(center.toVector());
            if (away.lengthSquared() < 0.01D) away = new Vector(0.1D, 0, 0.1D);
            player.setVelocity(away.normalize().multiply(1.1D).setY(0.65D));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 50, 1));
        });
    });
    }
}
