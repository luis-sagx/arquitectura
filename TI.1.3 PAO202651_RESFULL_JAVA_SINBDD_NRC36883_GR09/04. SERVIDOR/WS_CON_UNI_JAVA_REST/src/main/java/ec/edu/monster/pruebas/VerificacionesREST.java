package ec.edu.monster.pruebas;

import ec.edu.monster.modelos.Respuesta;
import ec.edu.monster.servicios.ConversorUnidadesServices;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/** Aserciones compartidas para ejecutar pruebas desde Source Packages en NetBeans. */
final class VerificacionesREST {
    private static int realizadas;

    private VerificacionesREST() {
    }

    static ConversorUnidadesServices servicio() {
        return new ConversorUnidadesServices(
            "monster", "monster123", Clock.systemUTC(), Duration.ofMinutes(15));
    }

    static String token(ConversorUnidadesServices servicio) {
        Respuesta respuesta = servicio.autenticar("monster", "monster123");
        codigo(respuesta, "OK", "inicio de sesión para la prueba");
        comprobar(respuesta.getToken() != null && !respuesta.getToken().isBlank(), "debe emitir un token");
        return respuesta.getToken();
    }

    static void codigo(Respuesta respuesta, String esperado, String operacion) {
        comprobar(respuesta != null, operacion + ": respuesta nula");
        comprobar(esperado.equals(respuesta.getCodigo()), operacion + ": se esperaba " + esperado
            + " y se recibió " + respuesta.getCodigo());
        String estadoEsperado = "OK".equals(esperado) ? "EXITO" : "ERROR";
        comprobar(estadoEsperado.equals(respuesta.getEstado()), operacion + ": estado esperado "
            + estadoEsperado + " y recibido " + respuesta.getEstado());
        comprobar(respuesta.getMensaje() != null && !respuesta.getMensaje().isBlank(),
            operacion + ": falta mensaje");
        if (!"OK".equals(esperado)) {
            comprobar(respuesta.getResultado() == null, operacion + ": un error no debe incluir resultado");
        }
    }

    static void resultado(Respuesta respuesta, double esperado, String operacion) {
        codigo(respuesta, "OK", operacion);
        comprobar(respuesta.getResultado() != null, operacion + ": falta resultado");
        double recibido = respuesta.getResultado();
        comprobar(Math.abs(esperado - recibido) <= 1e-9 * Math.max(1.0, Math.abs(esperado)),
            operacion + ": esperado " + esperado + ", recibido " + recibido);
    }

    static void comprobar(boolean condicion, String mensaje) {
        realizadas++;
        if (!condicion) throw new AssertionError(mensaje);
    }

    static void informar(String prueba) {
        System.out.println(prueba + ": OK (" + realizadas + " verificaciones acumuladas)");
    }

    static int realizadas() {
        return realizadas;
    }

    static final class RelojMutable extends Clock {
        private Instant instante;

        RelojMutable(Instant instante) {
            this.instante = instante;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instante;
        }

        void avanzar(Duration duracion) {
            instante = instante.plus(duracion);
        }
    }
}
