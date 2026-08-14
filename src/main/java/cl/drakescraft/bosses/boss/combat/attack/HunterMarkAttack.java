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
/** Ataque ranged: hunterMark. Extraido de BossCombatDirector sin cambiar su comportamiento. */
public final class HunterMarkAttack implements BossAttack {

    @Override
    public String getName() {
        return "hunterMark";
    }

    @Override
    public AttackFamily getFamily() {
        return AttackFamily.RANGED;
    }

    @Override
    public void execute(BossCombatSupport soporte, OdysseyBoss boss, Player target) {

    Location snapshot = target.getLocation().clone();
    soporte.telegraph(boss, "Marca del cazador", snapshot, soporte.dustDistancia(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, () -> {
        if (!target.isOnline() || target.isDead() || target.getWorld() != boss.getEntity().getWorld()) return;
        target.getWorld().spawnParticle(Particle.OMINOUS_SPAWNING, target.getLocation().add(0, 1, 0),
                45, 0.45, 0.9, 0.45, 0.04);
        target.damage(boss.scaleArenaDamage(16.0D), boss.getEntity());
        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0));
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 1));
    });
    }
}
