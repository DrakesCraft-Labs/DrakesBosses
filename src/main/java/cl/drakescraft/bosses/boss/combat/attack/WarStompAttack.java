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
/** Ataque ground: warStomp. Extraido de BossCombatDirector sin cambiar su comportamiento. */
public final class WarStompAttack implements BossAttack {

    @Override
    public String getName() {
        return "warStomp";
    }

    @Override
    public AttackFamily getFamily() {
        return AttackFamily.GROUND;
    }

    @Override
        // Este ataque no apunta a nadie en concreto: golpea alrededor del jefe.
    public void execute(BossCombatSupport soporte, OdysseyBoss boss, Player target) {

    Location center = boss.getEntity().getLocation().clone();
    soporte.telegraph(boss, "Pisotón de guerra", center, soporte.dustTerrestre(), Sound.ENTITY_RAVAGER_ROAR, () -> {
        center.getWorld().spawnParticle(Particle.BLOCK, center, 80, 4.0, 0.25, 4.0, 0.1,
                center.clone().add(0, -1, 0).getBlock().getBlockData());
        center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.4F, 0.6F);
        soporte.damagePlayers(boss, center, 8.0D, 12.0D, player -> player.setVelocity(
                player.getVelocity().add(new Vector(0, 0.8D, 0))));
    });
    }
}
