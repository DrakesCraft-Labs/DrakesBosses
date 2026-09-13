package cl.drakescraft.bosses.boss.instances;

import java.util.Locale;

/**
 * Construccion del comando que invoca la escultura de Jax, aislada de Bukkit para poder probarla.
 *
 * <p>El modelo se resume en un {@code summon block_display} con pasajeros anidados: para poder
 * reencontrarlo un tick despues hay que etiquetarlo, y para que nazca en la arena hay que anclarlo
 * al mundo del boss. La consola ejecuta en la dimension por defecto (el overworld), no en el mundo
 * del boss, asi que sin {@code execute in} la escultura acaba en {@code world} mientras el combate
 * ocurre en {@code drakes_bosses}.</p>
 */
final class JaxModelCommand {

    private JaxModelCommand() {
    }

    /** Prefijo exigido al recurso del modelo: el resto del codigo asume un BlockDisplay raiz. */
    static final String REQUIRED_PREFIX = "summon block_display ";

    /**
     * Inserta {@code Tags:[marker]} en el compuesto NBT del summon.
     *
     * @return el comando etiquetado, o {@code null} si el comando no trae compuesto NBT donde
     *         insertar la etiqueta (sin ella el display es irrecuperable y quedaria huerfano).
     */
    static String tagged(String command, String marker) {
        if (command == null || marker == null || marker.isEmpty()) {
            return null;
        }
        int brace = command.indexOf('{');
        if (brace < 0) {
            return null;
        }
        return command.substring(0, brace + 1) + "Tags:[\"" + marker + "\"]," + command.substring(brace + 1);
    }

    /**
     * Ancla el comando al mundo y a la posicion del boss.
     *
     * @param worldKey clave de dimension del mundo del boss (p.ej. {@code minecraft:drakes_bosses}).
     */
    static String positioned(String worldKey, double x, double y, double z, String taggedCommand) {
        if (worldKey == null || worldKey.isEmpty() || taggedCommand == null) {
            return null;
        }
        return String.format(Locale.ROOT, "execute in %s positioned %.3f %.3f %.3f run %s",
                worldKey, x, y, z, taggedCommand);
    }
}
