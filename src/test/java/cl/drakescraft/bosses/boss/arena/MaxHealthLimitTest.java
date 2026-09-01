package cl.drakescraft.bosses.boss.arena;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaxHealthLimitTest {

    @Test
    @DisplayName("lee el techo del rechazo real de Paper que tumbaba la arena de zeus")
    void leeElTechoDelMensajeDePaper() {
        String paper = "Health value (20000.0) must be between 0 and 10000.0. (attribute base value: 20000.0)";
        assertEquals(10000.0D, MaxHealthLimit.ceilingFromMessage(paper));
    }

    @Test
    @DisplayName("un mensaje sin techo declarado no inventa un limite")
    void mensajeSinTechoDevuelveNegativo() {
        assertTrue(MaxHealthLimit.ceilingFromMessage("valor invalido") < 0.0D);
        assertTrue(MaxHealthLimit.ceilingFromMessage(null) < 0.0D);
    }

    @Test
    @DisplayName("sin techo en el mensaje, encuentra el mayor valor que el atributo acepta")
    void buscaElMayorValorAceptado() {
        double techo = 10000.0D;
        double encontrado = MaxHealthLimit.highestAccepted(candidate -> candidate <= techo, 20000.0D, 4000.0D);
        assertTrue(encontrado <= techo, "no puede superar el techo del servidor");
        assertTrue(encontrado > techo - 1.0D, "debe quedar pegado al techo: " + encontrado);
    }

    @Test
    @DisplayName("si el atributo no acepta nada nuevo, conserva el valor vigente")
    void conservaElValorVigenteSiTodoEsRechazado() {
        assertEquals(4000.0D, MaxHealthLimit.highestAccepted(candidate -> false, 20000.0D, 4000.0D));
    }

    @Test
    @DisplayName("la vida que no cupo se cobra como absorcion de dano")
    void compensaLaVidaRecortada() {
        assertEquals(2.0D, MaxHealthLimit.compensationDivider(20000.0D, 10000.0D));
    }

    @Test
    @DisplayName("si la vida pedida cupo entera no se toca el dano")
    void sinRecorteNoHayCompensacion() {
        assertEquals(1.0D, MaxHealthLimit.compensationDivider(4000.0D, 4000.0D));
        assertEquals(1.0D, MaxHealthLimit.compensationDivider(4000.0D, 0.0D));
    }
}
