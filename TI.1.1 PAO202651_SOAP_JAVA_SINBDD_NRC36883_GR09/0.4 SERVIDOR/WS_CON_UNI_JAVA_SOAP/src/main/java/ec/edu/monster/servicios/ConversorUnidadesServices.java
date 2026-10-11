package ec.edu.monster.servicios;

import ec.edu.monster.modelos.Respuesta;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Logica de negocio independiente del transporte SOAP, sin base de datos. */
public class ConversorUnidadesServices {
    private final String usuario;
    private final String clave;
    private final Clock reloj;
    private final Duration duracion;
    private final Map<String, Instant> sesiones = new ConcurrentHashMap<>();
    private final Map<String, List<String>> catalogo = new LinkedHashMap<>();
    private final Map<String, Double> longitud = Map.of(
        "KILOMETRO", 1000.0, "METRO", 1.0, "CENTIMETRO", 0.01,
        "MILIMETRO", 0.001, "MILLA", 1609.344, "PIE", 0.3048);
    private final Map<String, Double> masa;

    public ConversorUnidadesServices() {
        this(System.getProperty("conversor.usuario", "monster"),
            System.getProperty("conversor.clave", "monster123"), Clock.systemUTC(),
            Duration.ofMinutes(Long.parseLong(System.getProperty("conversor.sesion.minutos", "15"))));
    }

    public ConversorUnidadesServices(String usuario, String clave, Clock reloj, Duration duracion) {
        if (usuario == null || usuario.isBlank() || clave == null || clave.isBlank())
            throw new IllegalArgumentException("Las credenciales deben estar configuradas");
        this.usuario = usuario; this.clave = clave;
        this.reloj = Objects.requireNonNull(reloj);
        this.duracion = Objects.requireNonNull(duracion);
        if (duracion.isZero() || duracion.isNegative())
            throw new IllegalArgumentException("La duracion de la sesion debe ser positiva");
        masa = Map.of("KILOGRAMO", 1.0, "GRAMO", 0.001, "MILIGRAMO", 0.000001,
            "LIBRA", 0.45359237);
        catalogo.put("LONGITUD", List.of("KILOMETRO", "METRO", "CENTIMETRO", "MILIMETRO", "MILLA", "PIE"));
        catalogo.put("MASA", List.of("KILOGRAMO", "GRAMO", "MILIGRAMO", "LIBRA"));
        catalogo.put("TEMPERATURA", List.of("CELSIUS", "FAHRENHEIT", "KELVIN"));
    }

    public Respuesta autenticar(String usuario, String clave) {
        if (!this.usuario.equals(usuario) || clave == null || !MessageDigest.isEqual(
                this.clave.getBytes(StandardCharsets.UTF_8), clave.getBytes(StandardCharsets.UTF_8)))
            return Respuesta.error("AUTH-001", "Credenciales invalidas");
        Instant ahora = reloj.instant();
        // Retener tokens vencidos un dia para distinguir expiracion de token desconocido.
        sesiones.entrySet().removeIf(e -> e.getValue().isBefore(ahora.minus(Duration.ofDays(1))));
        Instant vencimiento = ahora.plus(duracion);
        String token = UUID.randomUUID().toString();
        sesiones.put(token, vencimiento);
        Respuesta r = Respuesta.exito("Sesion iniciada");
        r.setToken(token); r.setExpiraEn(vencimiento.toString());
        return r;
    }

    private Respuesta validarToken(String token) {
        Instant vencimiento = token == null ? null : sesiones.get(token);
        if (vencimiento == null) return Respuesta.error("AUTH-002", "Token ausente o invalido");
        if (!reloj.instant().isBefore(vencimiento))
            return Respuesta.error("AUTH-003", "Sesion expirada; inicie sesion nuevamente");
        return null;
    }

    public Respuesta obtenerCategorias(String token) {
        Respuesta error = validarToken(token);
        if (error != null) return error;
        Respuesta r = Respuesta.exito("Categorias disponibles; conversion habilitada para longitud, masa y temperatura");
        r.setCategorias(new ArrayList<>(catalogo.keySet()));
        return r;
    }

    public Respuesta obtenerUnidades(String token, String categoria) {
        Respuesta error = validarToken(token);
        if (error != null) return error;
        String c = normalizar(categoria);
        if (!catalogo.containsKey(c)) return Respuesta.error("DATA-001", "Categoria inexistente o vacia");
        Respuesta r = Respuesta.exito("Unidades disponibles");
        r.setCategoria(c); r.setUnidades(new ArrayList<>(catalogo.get(c)));
        return r;
    }

    public Respuesta convertir(String token, String categoria, String unidadOrigen, String unidadDestino, Double valor) {
        Respuesta error = validarToken(token);
        if (error != null) return error;
        String c = normalizar(categoria), origen = normalizar(unidadOrigen), destino = normalizar(unidadDestino);
        if (valor == null || !Double.isFinite(valor) || !catalogo.containsKey(c) || origen.isEmpty() || destino.isEmpty())
            return Respuesta.error("DATA-001", "Indique categoria, unidades y un valor numerico finito");
        for (String u : List.of(origen, destino)) {
            if (!catalogo.get(c).contains(u)) {
                boolean otraMagnitud = catalogo.values().stream().anyMatch(unidades -> unidades.contains(u));
                return Respuesta.error(otraMagnitud ? "DATA-001" : "UNIT-001",
                    otraMagnitud ? "Las unidades pertenecen a magnitudes incompatibles" : "Unidad no soportada: " + u);
            }
        }
        double resultado;
        if ("TEMPERATURA".equals(c)) {
            if (valor < ceroAbsoluto(origen))
                return Respuesta.error("DATA-001", "Temperatura inferior al cero absoluto");
            // Validar en la escala original evita rechazar el limite por redondeo.
            double celsius = Math.max(-273.15, aCelsius(origen, valor));
            resultado = origen.equals(destino) ? valor : desdeCelsius(destino, celsius);
        } else {
            Map<String, Double> factores = "LONGITUD".equals(c) ? longitud : masa;
            resultado = valor * (factores.get(origen) / factores.get(destino));
        }
        if (!Double.isFinite(resultado)) return Respuesta.error("DATA-001", "Resultado fuera del rango numerico");
        Respuesta r = Respuesta.exito("Conversion realizada");
        r.setResultado(resultado); r.setCategoria(c);
        r.setUnidadOrigen(origen); r.setUnidadDestino(destino);
        return r;
    }

    private static String normalizar(String texto) {
        return texto == null ? "" : texto.trim().toUpperCase(Locale.ROOT);
    }
    private static double ceroAbsoluto(String unidad) {
        return switch (unidad) {
            case "CELSIUS" -> -273.15;
            case "FAHRENHEIT" -> -459.67;
            case "KELVIN" -> 0;
            default -> throw new IllegalArgumentException("Escala no soportada");
        };
    }
    private static double aCelsius(String unidad, double valor) {
        return switch (unidad) {
            case "CELSIUS" -> valor;
            case "FAHRENHEIT" -> (valor - 32) * (5.0 / 9.0);
            case "KELVIN" -> valor - 273.15;
            default -> throw new IllegalArgumentException("Escala no soportada");
        };
    }
    private static double desdeCelsius(String unidad, double valor) {
        return switch (unidad) {
            case "CELSIUS" -> valor;
            case "FAHRENHEIT" -> valor * 1.8 + 32;
            case "KELVIN" -> valor + 273.15;
            default -> throw new IllegalArgumentException("Escala no soportada");
        };
    }
}
