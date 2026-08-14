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
/** Ataque ranged: spiritBeam. Extraido de BossCombatDirector sin cambiar su comportamiento. */
public final class SpiritBeamAttack implements BossAttack {

    @Override
    public String getName() {
        return "spiritBeam";
    }

    @Override
    public AttackFamily getFamily() {
        return AttackFamily.RANGED;
    }

    @Override
    public void execute(BossCombatSupport soporte, OdysseyBoss boss, Player target) {

    Location snapshot = target.getEyeLocation().clone();
    soporte.telegraph(boss, "Haz espiritual", snapshot, soporte.dustDistancia(), Sound.ENTITY_EVOKER_PREPARE_ATTACK, () -> {
        Location origin = boss.getEntity().getEyeLocation();
        soporte.drawLine(origin, snapshot, Particle.SOUL_FIRE_FLAME, soporte.dustDistancia());
        soporte.damagePlayers(boss, snapshot, 2.4D, 14.0D,
                player -> player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1)));
        snapshot.getWorld().playSound(snapshot, Sound.ENTITY_WITHER_SHOOT, 1.1F, 1.35F);
    });
    }
}
