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
/** Ataque aerial: starfall. Extraido de BossCombatDirector sin cambiar su comportamiento. */
public final class StarfallAttack implements BossAttack {

    @Override
    public String getName() {
        return "starfall";
    }

    @Override
    public AttackFamily getFamily() {
        return AttackFamily.AERIAL;
    }

    @Override
    public void execute(BossCombatSupport soporte, OdysseyBoss boss, Player target) {

    Location impact = target.getLocation().clone();
    soporte.telegraph(boss, "Lluvia estelar", impact, soporte.dustAereo(), Sound.BLOCK_BEACON_POWER_SELECT, () -> {
        World world = impact.getWorld();
        world.spawnParticle(Particle.END_ROD, impact.clone().add(0, 8, 0), 70, 3.5, 5.5, 3.5, 0.08);
        world.spawnParticle(Particle.EXPLOSION, impact, 4, 2.0, 0.5, 2.0, 0.0);
        world.playSound(impact, Sound.ENTITY_GENERIC_EXPLODE, 1.2F, 1.4F);
        soporte.damagePlayers(boss, impact, 5.5D, 13.0D, player -> player.setVelocity(player.getVelocity().setY(0.75D)));
    });
    }
}
