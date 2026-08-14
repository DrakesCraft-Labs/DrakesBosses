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
/** Ataque ranged: arcaneMissiles. Extraido de BossCombatDirector sin cambiar su comportamiento. */
public final class ArcaneMissilesAttack implements BossAttack {

    @Override
    public String getName() {
        return "arcaneMissiles";
    }

    @Override
    public AttackFamily getFamily() {
        return AttackFamily.RANGED;
    }

    @Override
    public void execute(BossCombatSupport soporte, OdysseyBoss boss, Player target) {

    soporte.telegraph(boss, "Misiles arcanos", target.getLocation(), soporte.dustDistancia(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, () -> {
        List<Player> victims = soporte.playersNear(boss.getEntity().getLocation(), 32.0D).stream().limit(3).toList();
        for (Player victim : victims) {
            soporte.drawLine(boss.getEntity().getEyeLocation(), victim.getEyeLocation(), Particle.END_ROD, soporte.dustDistancia());
            victim.damage(9.0D, boss.getEntity());
        }
        if (victims.isEmpty() && target.isOnline()) {
            target.damage(boss.scaleArenaDamage(9.0D), boss.getEntity());
        }
    });
    }
}
