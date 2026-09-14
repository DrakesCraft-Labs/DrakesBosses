package cl.drakescraft.bosses.boss.arena;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Deja pasar el primer aviso de cada clave y silencia las repeticiones mientras la clave siga viva.
 * La contencion de un jefe se dispara una vez por segundo mientras el jefe empuja el borde de su
 * celda, asi que registrarla en cada pasada llenaba la consola con la misma linea; el aviso util es
 * el primero. {@link #retain(Collection)} poda las claves de sesiones ya cerradas para que la
 * estructura no crezca sin limite y para que una arena nueva vuelva a avisar.
 */
final class SessionLogGate<K> {
    private final Set<K> reported = ConcurrentHashMap.newKeySet();

    /** true solo la primera vez que se ve la clave desde que entro en la ventana vigente. */
    boolean firstTime(K key) {
        return key != null && reported.add(key);
    }

    /** Olvida toda clave que ya no este viva, devolviendo el derecho a avisar si reaparece. */
    void retain(Collection<K> alive) {
        reported.retainAll(alive);
    }

    int size() {
        return reported.size();
    }
}
