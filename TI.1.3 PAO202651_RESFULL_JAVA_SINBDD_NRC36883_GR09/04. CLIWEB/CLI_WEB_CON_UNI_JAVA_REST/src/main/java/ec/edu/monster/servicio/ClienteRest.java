package ec.edu.monster.servicio;

import ec.edu.monster.modelo.RespuestaRest;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Cliente HTTP/JSON del servicio REST de conversión. */
public final class ClienteRest {
    private static final HttpClient HTTP = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    private final URI endpoint;

    public ClienteRest(URI endpoint) {
        if (endpoint == null || endpoint.getHost() == null
                || !List.of("http", "https").contains(endpoint.getScheme())) {
            throw new IllegalArgumentException("Configura un endpoint REST válido con HTTP o HTTPS.");
        }
        String ruta = endpoint.getPath() == null ? "" : endpoint.getPath().replaceAll("/+$", "");
        try {
            this.endpoint = new URI(endpoint.getScheme(), endpoint.getUserInfo(), endpoint.getHost(),
                endpoint.getPort(), ruta, null, null);
        } catch (java.net.URISyntaxException e) {
            throw new IllegalArgumentException("El endpoint REST configurado no es válido.", e);
        }
    }

    public RespuestaRest autenticar(String usuario, String clave) throws IOException {
        return invocar("POST", "/auth/login", null, Map.of("usuario", usuario, "clave", clave));
    }

    public RespuestaRest recuperarClave(String usuario, String nuevaClave) throws IOException {
        return invocar("POST", "/auth/recover", null,
            Map.of("usuario", usuario, "nuevaClave", nuevaClave));
    }

    public RespuestaRest obtenerCategorias(String token) throws IOException {
        return invocar("GET", "/catalog/categories", token, null);
    }

    public RespuestaRest obtenerUnidades(String token, String categoria) throws IOException {
        String segmento = URLEncoder.encode(categoria, StandardCharsets.UTF_8).replace("+", "%20");
        return invocar("GET", "/catalog/categories/" + segmento + "/units", token, null);
    }

    public RespuestaRest convertir(String token, String categoria, String origen,
            String destino, double valor) throws IOException {
        Map<String, Object> solicitud = new LinkedHashMap<>();
        solicitud.put("categoria", categoria);
        solicitud.put("unidadOrigen", origen);
        solicitud.put("unidadDestino", destino);
        solicitud.put("valor", valor);
        return invocar("POST", "/conversions", token, solicitud);
    }

    private RespuestaRest invocar(String metodo, String ruta, String token, Object cuerpo) throws IOException {
        URI uri = URI.create(endpoint.toString() + ruta);
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
            .timeout(Duration.ofSeconds(30))
            .header("Accept", "application/json");
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }

        Jsonb jsonb = JsonbBuilder.create();
        try {
            if (cuerpo == null) {
                builder.method(metodo, HttpRequest.BodyPublishers.noBody());
            } else {
                String json;
                try {
                    json = jsonb.toJson(cuerpo);
                } catch (jakarta.json.bind.JsonbException e) {
                    throw new IOException("No se pudo preparar la solicitud JSON para el servidor REST.", e);
                }
                builder.header("Content-Type", "application/json; charset=UTF-8")
                    .method(metodo, HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));
            }

            HttpResponse<String> respuesta;
            try {
                respuesta = HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("La solicitud al servidor REST fue interrumpida.", e);
            } catch (java.net.http.HttpTimeoutException e) {
                throw new IOException("El servidor REST tardó demasiado en responder.", e);
            } catch (IOException e) {
                throw new IOException("No se pudo conectar con el servidor REST. Comprueba su URL y despliegue.", e);
            }

            RespuestaRest datos;
            try {
                datos = jsonb.fromJson(respuesta.body(), RespuestaRest.class);
            } catch (RuntimeException e) {
                throw new IOException("El servidor REST devolvió una respuesta JSON inválida.", e);
            }
            if (datos == null || datos.getCodigo() == null || datos.getMensaje() == null) {
                throw new IOException("El servidor REST devolvió una respuesta incompleta.");
            }
            if (datos.getResultado() != null && !Double.isFinite(datos.getResultado())) {
                throw new IOException("El servidor REST devolvió un resultado numérico inválido.");
            }
            if ((respuesta.statusCode() < 200 || respuesta.statusCode() >= 300) && datos.esExito()) {
                throw new IOException("El servidor REST devolvió un estado HTTP inesperado.");
            }
            return datos;
        } finally {
            try {
                jsonb.close();
            } catch (Exception ignored) {
                // El cierre no debe ocultar el resultado ni el error de la solicitud.
            }
        }
    }
}
