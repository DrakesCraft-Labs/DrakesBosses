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
/** Ataque aerial: skyLance. Extraido de BossCombatDirector sin cambiar su comportamiento. */
public final class SkyLanceAttack implements BossAttack {

    @Override
    public String getName() {
        return "skyLance";
    }

    @Override
    public AttackFamily getFamily() {
        return AttackFamily.AERIAL;
    }

    @Override
    public void execute(BossCombatSupport soporte, OdysseyBoss boss, Player target) {

    Location impact = target.getLocation().clone();
    soporte.telegraph(boss, "Lanza celeste", impact, soporte.dustAereo(), Sound.BLOCK_BEACON_ACTIVATE, () -> {
        Location origin = impact.clone().add(0, 12, 0);
        soporte.drawLine(origin, impact, Particle.END_ROD, soporte.dustAereo());
        impact.getWorld().spawnParticle(Particle.EXPLOSION, impact, 3, 0.8, 0.3, 0.8, 0.0);
        impact.getWorld().playSound(impact, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.3F, 0.8F);
        soporte.damagePlayers(boss, impact, 4.5D, 17.0D,
                player -> player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 45, 1)));
    });
    }
}
