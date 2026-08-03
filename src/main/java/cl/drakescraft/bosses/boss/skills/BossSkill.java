package cl.drakescraft.bosses.boss.skills;

import org.bukkit.entity.Player;
import cl.drakescraft.bosses.boss.OdysseyBoss;

public interface BossSkill {
    void execute(OdysseyBoss boss, Player target);
}
