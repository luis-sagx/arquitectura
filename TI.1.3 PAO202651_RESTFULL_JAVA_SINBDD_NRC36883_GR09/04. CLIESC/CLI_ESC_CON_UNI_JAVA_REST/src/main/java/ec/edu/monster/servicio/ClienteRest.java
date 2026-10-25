package ec.edu.monster.servicio;

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

/** Transporte HTTP/JSON del cliente de escritorio REST. */
public final class ClienteRest {
    private static final Gson GSON = new Gson();
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private final HttpClient http;
    private final URI baseUri;

    public ClienteRest(URI baseUri) {
        this.baseUri = normalizarBase(baseUri);
        http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public RespuestaRest autenticar(String usuario, String clave) throws IOException {
        return post("auth/login", new LoginRequest(usuario, clave), null);
    }

    public RespuestaRest recuperarClave(String usuario, String nuevaClave) throws IOException {
        return post("auth/recover", new RecoverRequest(usuario, nuevaClave), null);
    }

    public RespuestaRest obtenerCategorias(String token) throws IOException { return get("catalog/categories", token); }

    public RespuestaRest obtenerUnidades(String token, String categoria) throws IOException {
        String valor = URLEncoder.encode(Objects.requireNonNull(categoria), StandardCharsets.UTF_8).replace("+", "%20");
        return get("catalog/categories/" + valor + "/units", token);
    }

    public RespuestaRest convertir(String token, String categoria, String unidadOrigen,
            String unidadDestino, double valor) throws IOException {
        return post("conversions", new ConversionRequest(categoria, unidadOrigen, unidadDestino, valor), token);
    }

    private RespuestaRest get(String path, String token) throws IOException {
        HttpRequest.Builder request = HttpRequest.newBuilder(baseUri.resolve(path)).timeout(TIMEOUT)
                .header("Accept", "application/json");
        agregarToken(request, token);
        return enviar(request.GET().build());
    }

    private RespuestaRest post(String path, Object body, String token) throws IOException {
        HttpRequest.Builder request = HttpRequest.newBuilder(baseUri.resolve(path)).timeout(TIMEOUT)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body), StandardCharsets.UTF_8));
        agregarToken(request, token);
        return enviar(request.build());
    }

    private RespuestaRest enviar(HttpRequest request) throws IOException {
        final HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("La solicitud al servidor fue interrumpida.", ex);
        } catch (IOException ex) {
            throw new IOException("No se pudo comunicar con el servidor REST: " + ex.getMessage(), ex);
        }
        String body = response.body();
        if (body == null || body.isBlank()) {
            throw new IOException("El servidor REST devolvió HTTP " + response.statusCode() + " sin contenido JSON.");
        }
        try {
            RespuestaRest parsed = GSON.fromJson(body, RespuestaRest.class);
            if (parsed == null) throw new IOException("El servidor REST devolvió una respuesta JSON vacía.");
            parsed.setEstadoHttp(response.statusCode());
            return parsed;
        } catch (JsonParseException | IllegalStateException ex) {
            throw new IOException("El servidor REST devolvió HTTP " + response.statusCode()
                    + " con una respuesta JSON no válida.", ex);
        }
    }

    private static URI normalizarBase(URI uri) {
        Objects.requireNonNull(uri, "baseUri");
        String scheme = uri.getScheme();
        if (!uri.isAbsolute() || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                || uri.getHost() == null || uri.getRawQuery() != null || uri.getRawFragment() != null
                || uri.getUserInfo() != null) {
            throw new IllegalArgumentException("La URL base debe ser absoluta HTTP/HTTPS y no incluir usuario, query ni fragmento.");
        }
        String value = uri.toString();
        while (value.endsWith("/")) value = value.substring(0, value.length() - 1);
        return URI.create(value + "/");
    }

    private static void agregarToken(HttpRequest.Builder request, String token) {
        if (token != null && !token.isBlank()) {
            request.header("Authorization", "Bearer " + token.trim());
        }
    }
}
