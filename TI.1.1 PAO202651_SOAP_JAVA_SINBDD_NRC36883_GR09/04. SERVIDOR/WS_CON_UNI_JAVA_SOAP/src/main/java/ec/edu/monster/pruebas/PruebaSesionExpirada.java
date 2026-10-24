package ec.edu.monster.pruebas;
import java.time.*;
public class PruebaSesionExpirada {
    private static final class Reloj extends Clock {
        private Instant instante = Instant.parse("2026-10-09T00:00:00Z");
        public ZoneId getZone() { return ZoneOffset.UTC; }
        public Clock withZone(ZoneId zone) { return Clock.fixed(instante, zone); }
        public Instant instant() { return instante; }
    }
    public static void main(String[] args) {
        Reloj reloj = new Reloj();
        var s = new ec.edu.monster.servicios.ConversorUnidadesServices("Monster", "Monster9", reloj, Duration.ofMinutes(15));
        String t = Verificaciones.token(s);
        reloj.instante = reloj.instante.plusSeconds(899);
        Verificaciones.resultado(s.convertir(t, "LONGITUD", "METRO", "CENTIMETRO", 1.0), 100);
        reloj.instante = reloj.instante.plusSeconds(1);
        Verificaciones.codigo(s.convertir(t, "LONGITUD", "METRO", "CENTIMETRO", 1.0), "AUTH-003");
        Verificaciones.codigo(s.obtenerCategorias(t), "AUTH-003");
        Verificaciones.codigo(s.obtenerUnidades(t, "MASA"), "AUTH-003");
        Verificaciones.resultado(s.convertir(Verificaciones.token(s), "LONGITUD", "METRO", "CENTIMETRO", 1.0), 100);
        Verificaciones.codigo(Verificaciones.servicio().convertir(t, "LONGITUD", "METRO", "CENTIMETRO", 1.0), "AUTH-002");
        System.out.println("PruebaSesionExpirada: OK");
    }
}
