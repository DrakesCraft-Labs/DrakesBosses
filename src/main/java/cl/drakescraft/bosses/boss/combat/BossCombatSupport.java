package cl.drakescraft.bosses.boss.combat;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import cl.drakescraft.bosses.boss.OdysseyBoss;

/**
 * Lo que un ataque necesita del director.
 *
 * Telegrafiar el golpe, hacer daño en un area y buscar jugadores cerca lo usan casi todos los
 * ataques. Estaba como metodos privados del director, asi que sacar un ataque a su propia clase
 * obligaba a arrastrar tambien esos ayudantes o a duplicarlos.
 *
 * Pasandolos por esta interfaz, cada ataque queda con lo suyo y nada mas.
 */
public interface BossCombatSupport {

    /**
     * Avisa antes de golpear y programa el impacto.
     *
     * Los jefes de DrakesCraft telegrafian siempre: el jugador ve donde va a caer el golpe y tiene
     * ocasion de apartarse. Un ataque que no avisa se siente injusto por muy bien hecho que este.
     */
    void telegraph(OdysseyBoss boss, String nombre, Location donde, Particle.DustOptions polvo,
                   Sound sonido, Runnable impacto);

    /** Daña a los jugadores en un radio, con un extra opcional sobre cada uno. */
    void damagePlayers(OdysseyBoss boss, Location centro, double radio, double daño,
                       java.util.function.Consumer<Player> extra);

    /** Los jugadores validos dentro del radio, ordenados del mas cercano al mas lejano. */
    List<Player> playersNear(Location centro, double radio);

    /** Dibuja un anillo de particulas. Lo usan los ataques para marcar la zona de impacto. */
    void drawRing(Location centro, double radio, Particle.DustOptions polvo);

    /** Dibuja una linea de particulas entre dos puntos. */
    void drawLine(Location desde, Location hasta, Particle particula, Particle.DustOptions polvo);

    /**
     * Las paletas de particulas de cada familia.
     *
     * Van aqui y no como constantes sueltas en cada ataque para que un ataque aereo nuevo se vea
     * igual que los demas aereos sin que su autor tenga que ir a copiar el color.
     */
    Particle.DustOptions dustAereo();

    Particle.DustOptions dustTerrestre();

    Particle.DustOptions dustDistancia();
}
