package ec.edu.monster.pruebas;

import ec.edu.monster.modelos.Respuesta;
import ec.edu.monster.servicios.ConversorUnidadesServices;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

/** Pruebas locales de reglas de negocio, sin servidor de aplicaciones. */
public class PruebaServicioUnidades {
    public static void main(String[] args) {
        probarAutenticacionYCatalogo();
        probarConversionesYValidaciones();
        probarRecuperacionDeClave();
        probarExpiracion();
        System.out.println("Pruebas locales del servicio: " + Verificaciones.realizadas() + " verificaciones OK");
    }

    private static void probarAutenticacionYCatalogo() {
        ConversorUnidadesServices servicio = servicio();
        Respuesta incorrecta = servicio.autenticar("monster", "incorrecta");
        Verificaciones.codigo("AUTH-001", incorrecta.getCodigo(), incorrecta.getEstado(), "credenciales inválidas");

        Respuesta sesion = servicio.autenticar("monster", "monster123");
        Verificaciones.codigo("OK", sesion.getCodigo(), sesion.getEstado(), "autenticación válida");
        Verificaciones.comprobar(sesion.getToken() != null && !sesion.getToken().isBlank(), "debe emitir token");
        Verificaciones.comprobar(sesion.getExpiraEn() != null, "debe incluir vencimiento");

        Respuesta categorias = servicio.obtenerCategorias(sesion.getToken());
        Verificaciones.codigo("OK", categorias.getCodigo(), categorias.getEstado(), "consulta de categorías");
        Verificaciones.comprobar(categorias.getCategorias().containsAll(
            List.of("LONGITUD", "MASA", "TEMPERATURA")), "debe publicar las tres categorías");

        Respuesta unidades = servicio.obtenerUnidades(sesion.getToken(), " longitud ");
        Verificaciones.codigo("OK", unidades.getCodigo(), unidades.getEstado(), "consulta normalizada de unidades");
        Verificaciones.comprobar(unidades.getUnidades().contains("METRO"), "LONGITUD debe incluir METRO");

        Respuesta tokenInvalido = servicio.obtenerCategorias("token-invalido");
        Verificaciones.codigo("AUTH-002", tokenInvalido.getCodigo(), tokenInvalido.getEstado(), "token inválido");
    }

    private static void probarConversionesYValidaciones() {
        ConversorUnidadesServices servicio = servicio();
        String token = servicio.autenticar("monster", "monster123").getToken();

        Respuesta longitud = servicio.convertir(token, "longitud", "kilometro", "metro", 2.5);
        Verificaciones.codigo("OK", longitud.getCodigo(), longitud.getEstado(), "conversión de longitud");
        Verificaciones.cerca(2500, longitud.getResultado(), "kilómetros a metros");

        Respuesta masa = servicio.convertir(token, "MASA", "KILOGRAMO", "GRAMO", 1.25);
        Verificaciones.codigo("OK", masa.getCodigo(), masa.getEstado(), "conversión de masa");
        Verificaciones.cerca(1250, masa.getResultado(), "kilogramos a gramos");

        Respuesta temperatura = servicio.convertir(token, "TEMPERATURA", "CELSIUS", "FAHRENHEIT", 0.0);
        Verificaciones.codigo("OK", temperatura.getCodigo(), temperatura.getEstado(), "conversión de temperatura");
        Verificaciones.cerca(32, temperatura.getResultado(), "cero Celsius a Fahrenheit");

        Respuesta unidadInvalida = servicio.convertir(token, "LONGITUD", "METRO", "YARDA", 1.0);
        Verificaciones.codigo("UNIT-001", unidadInvalida.getCodigo(), unidadInvalida.getEstado(), "unidad desconocida");
        Verificaciones.comprobar(unidadInvalida.getResultado() == null, "error de unidad no debe incluir resultado");

        Respuesta incompatibles = servicio.convertir(token, "LONGITUD", "METRO", "GRAMO", 1.0);
        Verificaciones.codigo("DATA-001", incompatibles.getCodigo(), incompatibles.getEstado(), "magnitudes incompatibles");

        Respuesta bajoCero = servicio.convertir(token, "TEMPERATURA", "KELVIN", "CELSIUS", -1.0);
        Verificaciones.codigo("DATA-001", bajoCero.getCodigo(), bajoCero.getEstado(), "temperatura bajo cero absoluto");

        Respuesta infinito = servicio.convertir(token, "LONGITUD", "METRO", "KILOMETRO", Double.POSITIVE_INFINITY);
        Verificaciones.codigo("DATA-001", infinito.getCodigo(), infinito.getEstado(), "valor infinito");
    }

    private static void probarRecuperacionDeClave() {
        ConversorUnidadesServices servicio = servicio();
        String token = servicio.autenticar("monster", "monster123").getToken();

        Respuesta claveVacia = servicio.recuperarClave("monster", " ");
        Verificaciones.codigo("DATA-001", claveVacia.getCodigo(), claveVacia.getEstado(), "clave vacía");

        Respuesta recuperacion = servicio.recuperarClave("monster", "temporal");
        Verificaciones.codigo("OK", recuperacion.getCodigo(), recuperacion.getEstado(), "recuperación de clave");

        Respuesta sesionAnterior = servicio.obtenerCategorias(token);
        Verificaciones.codigo("AUTH-002", sesionAnterior.getCodigo(), sesionAnterior.getEstado(), "sesión invalidada");

        Respuesta claveAnterior = servicio.autenticar("monster", "monster123");
        Verificaciones.codigo("AUTH-001", claveAnterior.getCodigo(), claveAnterior.getEstado(), "clave anterior invalidada");

        Respuesta claveNueva = servicio.autenticar("monster", "temporal");
        Verificaciones.codigo("OK", claveNueva.getCodigo(), claveNueva.getEstado(), "clave nueva activa");
    }

    private static void probarExpiracion() {
        RelojMutable reloj = new RelojMutable(Instant.parse("2026-01-01T00:00:00Z"));
        ConversorUnidadesServices servicio = new ConversorUnidadesServices(
            "monster", "monster123", reloj, Duration.ofSeconds(5));
        String token = servicio.autenticar("monster", "monster123").getToken();
        reloj.avanzar(Duration.ofSeconds(5));
        Respuesta expirada = servicio.obtenerCategorias(token);
        Verificaciones.codigo("AUTH-003", expirada.getCodigo(), expirada.getEstado(), "sesión expirada");
    }

    private static ConversorUnidadesServices servicio() {
        return new ConversorUnidadesServices("monster", "monster123", Clock.systemUTC(), Duration.ofMinutes(15));
    }

    private static final class RelojMutable extends Clock {
        private Instant instante;

        private RelojMutable(Instant instante) {
            this.instante = instante;
        }

        @Override
        public ZoneId getZone() { return ZoneId.of("UTC"); }

        @Override
        public Clock withZone(ZoneId zone) { return this; }

        @Override
        public Instant instant() { return instante; }

        private void avanzar(Duration duracion) { instante = instante.plus(duracion); }
    }
}
