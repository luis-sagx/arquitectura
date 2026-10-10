package ec.edu.monster.pruebas;
import ec.edu.monster.modelos.Respuesta;
import ec.edu.monster.servicios.ConversorUnidadesServices;
final class Verificaciones {
    private Verificaciones() {}
    static ConversorUnidadesServices servicio() {
        return new ConversorUnidadesServices("monster", "monster123", java.time.Clock.systemUTC(), java.time.Duration.ofMinutes(15), 45.359237);
    }
    static String token(ConversorUnidadesServices s) {
        Respuesta r = s.autenticar("monster", "monster123");
        codigo(r, "OK");
        comprobar(r.getToken() != null && !r.getToken().isBlank(), "Debe entregar token");
        return r.getToken();
    }
    static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
    }
    static void codigo(Respuesta r, String codigo) {
        comprobar(codigo.equals(r.getCodigo()), "Esperado " + codigo + ", recibido " + r.getCodigo());
        comprobar(r.getMensaje() != null && !r.getMensaje().isBlank(), "Debe incluir mensaje");
        comprobar(("OK".equals(codigo) ? "EXITO" : "ERROR").equals(r.getEstado()), "Estado incorrecto");
        if (!"OK".equals(codigo)) comprobar(r.getResultado() == null, "Un error no debe incluir resultado");
    }
    static void resultado(Respuesta r, double esperado) {
        codigo(r, "OK");
        comprobar(r.getResultado() != null && Math.abs(r.getResultado() - esperado) <= 1e-9 * Math.max(1, Math.abs(esperado)),
            "Esperado " + esperado + ", recibido " + r.getResultado());
    }
}
