package cl.drakescraft.bosses.boss.combat;

import org.bukkit.entity.Player;

import cl.drakescraft.bosses.boss.OdysseyBoss;
import cl.drakescraft.bosses.boss.combat.BossCombatProfile.AttackFamily;

/**
 * Un ataque de jefe.
 *
 * POR QUE UNA INTERFAZ
 *
 * Los 18 ataques vivian como metodos privados dentro de BossCombatDirector, con el reparto
 * cableado en cadenas de "if variant == 0 ... else if variant == 1". Anadir uno obligaba a tocar
 * el switch, renumerar las variantes y confiar en no descuadrar ninguna familia.
 *
 * Con cada ataque en su clase, anadir uno es escribir un fichero y registrarlo. Y el director
 * baja de 450 lineas a coordinar.
 *
 * Es a proposito la misma forma que usa MultiverseCreatures, el plugin de Chagui: alli los
 * ataques ya estan asi. Si algun dia los dos quieren compartir catalogo, hablan el mismo idioma
 * sin que ninguno dependa del otro en tiempo de ejecucion.
 */
public interface BossAttack {

    /** Nombre corto, el que se registra y se anuncia. */
    String getName();

    /** A que distancia tiene sentido: aereo, cuerpo a cuerpo o a distancia. */
    AttackFamily getFamily();

    /** Ejecuta el ataque. El apoyo comun -- telegrafiar, dañar, buscar jugadores -- va en soporte. */
    void execute(BossCombatSupport soporte, OdysseyBoss boss, Player target);
}
