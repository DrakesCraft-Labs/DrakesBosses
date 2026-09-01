package cl.drakescraft.bosses.boss.arena;

import java.util.function.DoublePredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * El techo de MAX_HEALTH que impone el servidor, y como repartir lo que no cabe.
 *
 * Una arena de grupo multiplica la vida del jefe por cinco. Paper rechaza con
 * IllegalArgumentException cualquier valor por encima del limite del atributo
 * (10000 en DrakesCraft), y esa excepcion tumbaba la arena entera antes de que
 * nadie llegara a pelear. Lo que el atributo no acepta se cobra como absorcion
 * de dano, asi que la dificultad efectiva no depende del limite del servidor.
 *
 * Esta clase no toca Bukkit, asi que se puede comprobar sin servidor.
 */
public final class MaxHealthLimit {

    /** Techo declarado por Paper al rechazar un valor: "must be between 0 and 10000.0". */
    private static final Pattern CEILING = Pattern.compile("between\\s+-?[0-9.]+\\s+and\\s+([0-9]+(?:\\.[0-9]+)?)");

    private MaxHealthLimit() {
    }

    /**
     * @return el techo que anuncia el mensaje de rechazo, o -1 si no lo declara
     */
    public static double ceilingFromMessage(String message) {
        if (message == null) return -1.0D;
        Matcher matcher = CEILING.matcher(message);
        if (!matcher.find()) return -1.0D;
        try {
            return Double.parseDouble(matcher.group(1));
        } catch (NumberFormatException unparsable) {
            return -1.0D;
        }
    }

    /**
     * Ultimo recurso si el mensaje no revela el techo: el mayor valor que el atributo acepta.
     *
     * @param accepts  intenta asignar el valor y responde si el atributo lo acepto
     * @param desired  valor pedido, ya rechazado
     * @param fallback valor actual del atributo, siempre valido
     */
    public static double highestAccepted(DoublePredicate accepts, double desired, double fallback) {
        double low = Math.max(1.0D, fallback);
        double high = desired;
        double best = fallback;
        for (int attempt = 0; attempt < 24 && high - low > 1.0D; attempt++) {
            double candidate = low + (high - low) / 2.0D;
            if (accepts.test(candidate)) {
                best = candidate;
                low = candidate;
            } else {
                high = candidate;
            }
        }
        return best;
    }

    /**
     * Cuanto hay que dividir el dano entrante para que la vida aplicada rinda como la pedida.
     *
     * @return 1.0 si la vida pedida cupo entera
     */
    public static double compensationDivider(double desired, double applied) {
        if (applied <= 0.0D || desired <= applied) return 1.0D;
        return desired / applied;
    }
}
