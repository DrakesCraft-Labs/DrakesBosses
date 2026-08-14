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
/** Ataque ranged: voidRift. Extraido de BossCombatDirector sin cambiar su comportamiento. */
public final class VoidRiftAttack implements BossAttack {

    @Override
    public String getName() {
        return "voidRift";
    }

    @Override
    public AttackFamily getFamily() {
        return AttackFamily.RANGED;
    }

    @Override
    public void execute(BossCombatSupport soporte, OdysseyBoss boss, Player target) {

    Location center = target.getLocation().clone();
    soporte.telegraph(boss, "Grieta del vacío", center, soporte.dustDistancia(), Sound.ENTITY_ENDERMAN_STARE, () -> {
        soporte.drawRing(center, 5.0D, soporte.dustDistancia());
        center.getWorld().spawnParticle(Particle.PORTAL, center.clone().add(0, 0.8, 0), 100, 2.0, 0.8, 2.0, 0.2);
        for (Player player : soporte.playersNear(center, 5.5D)) {
            player.damage(boss.scaleArenaDamage(13.0D), boss.getEntity());
            player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 70, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 35, 0));
        }
    });
    }
}
