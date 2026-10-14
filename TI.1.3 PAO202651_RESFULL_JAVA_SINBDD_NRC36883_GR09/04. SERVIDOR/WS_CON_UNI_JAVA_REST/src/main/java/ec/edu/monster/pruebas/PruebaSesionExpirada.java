package ec.edu.monster.pruebas;

import ec.edu.monster.modelos.Respuesta;
import ec.edu.monster.servicios.ConversorUnidadesServices;
import java.time.Duration;
import java.time.Instant;

public class PruebaSesionExpirada {
    public static void main(String[] args) {
        VerificacionesREST.RelojMutable reloj = new VerificacionesREST.RelojMutable(
            Instant.parse("2026-01-01T00:00:00Z"));
        ConversorUnidadesServices servicio = new ConversorUnidadesServices(
            "monster", "monster123", reloj, Duration.ofSeconds(5));
        String token = servicio.autenticar("monster", "monster123").getToken();

        VerificacionesREST.codigo(servicio.obtenerCategorias(token), "OK", "sesión antes de vencer");
        reloj.avanzar(Duration.ofSeconds(5));
        Respuesta expirada = servicio.obtenerCategorias(token);
        VerificacionesREST.codigo(expirada, "AUTH-003", "sesión vencida");

        VerificacionesREST.informar("PruebaSesionExpirada");
    }
}
