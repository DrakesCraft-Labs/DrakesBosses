package cl.drakescraft.bosses.boss.arena;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionLogGateTest {

    @Test
    @DisplayName("la contencion de una misma sesion solo se registra la primera vez")
    void soloAvisaUnaVezPorSesion() {
        SessionLogGate<UUID> gate = new SessionLogGate<>();
        UUID sesion = UUID.randomUUID();
        assertTrue(gate.firstTime(sesion));
        for (int pasada = 0; pasada < 25; pasada++) {
            assertFalse(gate.firstTime(sesion), "la repeticion de la pasada " + pasada + " no debe volver a la consola");
        }
    }

    @Test
    @DisplayName("cerrada la arena, la siguiente sesion recupera su aviso y no queda basura")
    void alPodarOlvidaLasSesionesCerradas() {
        SessionLogGate<UUID> gate = new SessionLogGate<>();
        UUID cerrada = UUID.randomUUID();
        UUID viva = UUID.randomUUID();
        gate.firstTime(cerrada);
        gate.firstTime(viva);
        assertEquals(2, gate.size());

        gate.retain(Set.of(viva));
        assertEquals(1, gate.size(), "la sesion cerrada debe desaparecer para que el mapa no crezca sin limite");
        assertFalse(gate.firstTime(viva), "la sesion que sigue viva conserva su silencio");
        assertTrue(gate.firstTime(cerrada), "una arena nueva con ese jefe vuelve a tener derecho a avisar");
    }

    @Test
    @DisplayName("sin sesiones vivas la poda deja la estructura vacia")
    void podaTotalCuandoNoQuedaNingunaSesion() {
        SessionLogGate<UUID> gate = new SessionLogGate<>();
        gate.firstTime(UUID.randomUUID());
        gate.firstTime(UUID.randomUUID());
        gate.retain(List.of());
        assertEquals(0, gate.size());
    }

    @Test
    @DisplayName("el mundo no plano avisa una sola vez por arranque aunque se entre muchas veces")
    void elMundoNoPlanoAvisaUnaVez() {
        SessionLogGate<String> gate = new SessionLogGate<>();
        assertTrue(gate.firstTime("drakes_bosses"));
        assertFalse(gate.firstTime("drakes_bosses"));
        assertTrue(gate.firstTime("otro_mundo"), "un mundo distinto es un aviso distinto");
    }

    @Test
    @DisplayName("una clave nula no ocupa sitio ni dispara aviso")
    void claveNulaNoAvisa() {
        SessionLogGate<UUID> gate = new SessionLogGate<>();
        assertFalse(gate.firstTime(null));
        assertEquals(0, gate.size());
    }
}
