package cl.drakescraft.bosses.boss.instances;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JaxModelCommandTest {

    private static final String MODEL =
            "summon block_display ~ ~ ~ {block_state:{Name:\"minecraft:amethyst_block\"},Passengers:[]}";

    @Test
    @DisplayName("la etiqueta entra en el compuesto NBT sin romper el resto del modelo")
    void tagsTheRootCompound() {
        String tagged = JaxModelCommand.tagged(MODEL, "odysseia_jax_abc");

        assertEquals("summon block_display ~ ~ ~ {Tags:[\"odysseia_jax_abc\"],"
                + "block_state:{Name:\"minecraft:amethyst_block\"},Passengers:[]}", tagged);
    }

    @Test
    @DisplayName("un modelo sin compuesto NBT se rechaza en vez de invocar un display irrastreable")
    void refusesCommandsWithoutNbt() {
        // Sin '{' no hay donde poner la etiqueta: el display nace sin marcador, attachVisual no lo
        // encuentra y queda huerfano. Antes replaceFirst devolvia el comando intacto y se invocaba igual.
        assertNull(JaxModelCommand.tagged("summon block_display ~ ~ ~", "odysseia_jax_abc"));
        assertNull(JaxModelCommand.tagged(null, "odysseia_jax_abc"));
        assertNull(JaxModelCommand.tagged(MODEL, ""));
    }

    @Test
    @DisplayName("el summon se ancla al mundo del boss, no a la dimension por defecto de la consola")
    void anchorsTheSummonToTheBossWorld() {
        String tagged = JaxModelCommand.tagged(MODEL, "odysseia_jax_abc");

        String positioned = JaxModelCommand.positioned("minecraft:drakes_bosses", 12.5D, 64.0D, -7.25D, tagged);

        assertTrue(positioned.startsWith("execute in minecraft:drakes_bosses positioned 12.500 64.000 -7.250 run "),
                positioned);
        assertTrue(positioned.endsWith(tagged), positioned);
    }

    @Test
    @DisplayName("sin clave de mundo no se invoca nada")
    void refusesToDispatchWithoutAWorldKey() {
        String tagged = JaxModelCommand.tagged(MODEL, "odysseia_jax_abc");

        assertNull(JaxModelCommand.positioned(null, 0D, 0D, 0D, tagged));
        assertNull(JaxModelCommand.positioned("", 0D, 0D, 0D, tagged));
        assertNull(JaxModelCommand.positioned("minecraft:drakes_bosses", 0D, 0D, 0D, null));
    }

    @Test
    @DisplayName("las coordenadas usan punto decimal aunque la JVM corra con locale es_CL")
    void formatsCoordinatesWithARootLocale() {
        assertTrue(JaxModelCommand.positioned("minecraft:world", -3989859.5D, 91.0D, 1230901.25D, MODEL)
                .contains("positioned -3989859.500 91.000 1230901.250"));
    }
}
