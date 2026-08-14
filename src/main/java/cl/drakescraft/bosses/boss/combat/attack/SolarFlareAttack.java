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
/** Ataque ranged: solarFlare. Extraido de BossCombatDirector sin cambiar su comportamiento. */
public final class SolarFlareAttack implements BossAttack {

    @Override
    public String getName() {
        return "solarFlare";
    }

    @Override
    public AttackFamily getFamily() {
        return AttackFamily.RANGED;
    }

    @Override
    public void execute(BossCombatSupport soporte, OdysseyBoss boss, Player target) {

    Location center = target.getLocation().clone();
    soporte.telegraph(boss, "Llamarada solar", center, soporte.dustDistancia(), Sound.ITEM_TOTEM_USE, () -> {
        center.getWorld().spawnParticle(Particle.FLASH, center.clone().add(0, 1, 0), 1, 0, 0, 0, 0);
        center.getWorld().spawnParticle(Particle.FIREWORK, center.clone().add(0, 1, 0), 80, 3.5, 1.5, 3.5, 0.15);
        for (Player player : soporte.playersNear(center, 8.0D)) {
            player.damage(boss.scaleArenaDamage(11.0D), boss.getEntity());
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 80, 0));
        }
    });
    }
}
