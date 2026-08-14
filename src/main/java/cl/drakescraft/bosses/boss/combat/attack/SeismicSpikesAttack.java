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
/** Ataque ground: seismicSpikes. Extraido de BossCombatDirector sin cambiar su comportamiento. */
public final class SeismicSpikesAttack implements BossAttack {

    @Override
    public String getName() {
        return "seismicSpikes";
    }

    @Override
    public AttackFamily getFamily() {
        return AttackFamily.GROUND;
    }

    @Override
        // Este ataque no apunta a nadie en concreto: golpea alrededor del jefe.
    public void execute(BossCombatSupport soporte, OdysseyBoss boss, Player target) {

    Location center = boss.getEntity().getLocation().clone();
    soporte.telegraph(boss, "Púas sísmicas", center, soporte.dustTerrestre(), Sound.BLOCK_ANVIL_LAND, () -> {
        for (int point = 0; point < 16; point++) {
            double angle = Math.PI * 2.0D * point / 16.0D;
            Location spike = center.clone().add(Math.cos(angle) * 7.0D, 0.15D, Math.sin(angle) * 7.0D);
            spike.getWorld().spawnParticle(Particle.BLOCK, spike, 18, 0.35, 1.2, 0.35, 0.06,
                    spike.clone().add(0, -1, 0).getBlock().getBlockData());
        }
        soporte.damagePlayers(boss, center, 9.0D, 15.0D, player ->
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 70, 1)));
    });
    }
}
