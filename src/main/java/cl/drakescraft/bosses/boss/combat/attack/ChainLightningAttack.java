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
/** Ataque ranged: chainLightning. Extraido de BossCombatDirector sin cambiar su comportamiento. */
public final class ChainLightningAttack implements BossAttack {

    @Override
    public String getName() {
        return "chainLightning";
    }

    @Override
    public AttackFamily getFamily() {
        return AttackFamily.RANGED;
    }

    @Override
    public void execute(BossCombatSupport soporte, OdysseyBoss boss, Player target) {

    Location impact = target.getLocation().clone();
    soporte.telegraph(boss, "Relámpago encadenado", impact, soporte.dustDistancia(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, () -> {
        List<Player> victims = soporte.playersNear(impact, 12.0D).stream().limit(4).toList();
        Location previous = boss.getEntity().getEyeLocation();
        for (Player victim : victims) {
            soporte.drawLine(previous, victim.getEyeLocation(), Particle.ELECTRIC_SPARK, null);
            victim.damage(boss.scaleArenaDamage(10.0D), boss.getEntity());
            previous = victim.getEyeLocation();
        }
        impact.getWorld().playSound(impact, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.3F, 1.1F);
    });
    }
}
