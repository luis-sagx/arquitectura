package ec.edu.monster.cliente;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import ec.edu.monster.modelo.ConversionRequest;
import ec.edu.monster.modelo.LoginRequest;
import ec.edu.monster.modelo.RecoverRequest;
import ec.edu.monster.modelo.RespuestaRest;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

/** Adaptador HTTP/JSON para el API REST de conversión de unidades. */
public final class ClienteRest {
    private static final Gson GSON = new Gson();
    private static final Duration TIEMPO_RESPUESTA = Duration.ofSeconds(30);

    private final HttpClient http;
    private final URI baseUri;

    public ClienteRest(URI baseUri) {
        this.baseUri = normalizarBase(baseUri);
        this.http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    public RespuestaRest autenticar(String usuario, String clave) throws IOException {
        return post("auth/login", new LoginRequest(usuario, clave), null);
    }

    public RespuestaRest recuperarClave(String usuario, String nuevaClave) throws IOException {
        return post("auth/recover", new RecoverRequest(usuario, nuevaClave), null);
    }

    public RespuestaRest obtenerCategorias(String token) throws IOException {
        return get("catalog/categories", token);
    }

    public RespuestaRest obtenerUnidades(String token, String categoria) throws IOException {
        String categoriaCodificada = URLEncoder.encode(
            Objects.requireNonNull(categoria, "categoria"), StandardCharsets.UTF_8).replace("+", "%20");
        return get("catalog/categories/" + categoriaCodificada + "/units", token);
    }

    public RespuestaRest convertir(String token, String categoria, String unidadOrigen,
            String unidadDestino, double valor) throws IOException {
        return post("conversions", new ConversionRequest(categoria, unidadOrigen, unidadDestino, valor), token);
    }

    private RespuestaRest get(String ruta, String token) throws IOException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(endpoint(ruta))
            .timeout(TIEMPO_RESPUESTA)
            .header("Accept", "application/json");
        agregarToken(builder, token);
        return enviar(builder.GET().build());
    }

    private RespuestaRest post(String ruta, Object body, String token) throws IOException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(endpoint(ruta))
            .timeout(TIEMPO_RESPUESTA)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json; charset=UTF-8")
            .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body), StandardCharsets.UTF_8));
        agregarToken(builder, token);
        return enviar(builder.build());
    }

    private RespuestaRest enviar(HttpRequest request) throws IOException {
        final HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("La solicitud al servidor fue interrumpida.", e);
        } catch (IOException e) {
            throw new IOException("No se pudo comunicar con el servidor REST: " + e.getMessage(), e);
        }

        String body = response.body();
        if (body == null || body.isBlank()) {
            throw new IOException("El servidor REST devolvió HTTP " + response.statusCode() + " sin contenido JSON.");
        }
        try {
            RespuestaRest respuesta = GSON.fromJson(body, RespuestaRest.class);
            if (respuesta == null) {
                throw new IOException("El servidor REST devolvió un cuerpo JSON vacío.");
            }
            respuesta.setEstadoHttp(response.statusCode());
            return respuesta;
        } catch (JsonParseException | IllegalStateException e) {
            throw new IOException("El servidor REST devolvió HTTP " + response.statusCode()
                + " con un cuerpo que no es una respuesta JSON válida.", e);
        }
    }

    private URI endpoint(String ruta) {
        return baseUri.resolve(ruta);
    }

    private static URI normalizarBase(URI uri) {
        Objects.requireNonNull(uri, "baseUri");
        String scheme = uri.getScheme();
        if (!uri.isAbsolute() || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                || uri.getHost() == null || uri.getRawQuery() != null || uri.getRawFragment() != null) {
            throw new IllegalArgumentException("La URL base debe ser absoluta HTTP/HTTPS y no incluir query ni fragmento.");
        }
        String valor = uri.toString();
        while (valor.endsWith("/")) valor = valor.substring(0, valor.length() - 1);
        return URI.create(valor + "/");
    }

    private static void agregarToken(HttpRequest.Builder request, String token) {
        if (token != null && !token.isBlank()) {
            request.header("Authorization", "Bearer " + token.trim());
        }
    }
}
