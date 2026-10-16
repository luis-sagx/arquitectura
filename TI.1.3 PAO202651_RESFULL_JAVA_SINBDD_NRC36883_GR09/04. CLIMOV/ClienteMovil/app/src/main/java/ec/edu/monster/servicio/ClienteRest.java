package ec.edu.monster.servicio;

import ec.edu.monster.modelo.RespuestaRest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Adaptador HTTP/JSON del API REST de conversión de unidades.
 * Usa HttpURLConnection y org.json, que ya vienen en Android; así las vistas y controladores
 * no conocen las rutas, los verbos HTTP ni el JSON.
 * Las operaciones protegidas envían el token en {@code Authorization: Bearer <token>}.
 */
public final class ClienteRest {
    private static final int TIEMPO_CONEXION_MS = 10_000;
    private static final int TIEMPO_RESPUESTA_MS = 30_000;

    private final String base;

    public ClienteRest(String direccion) {
        this.base = normalizarEndpoint(direccion);
    }

    /** Valida la URL base del API (la que termina en /api) y quita las barras finales. */
    public static String normalizarEndpoint(String direccion) {
        if (direccion == null || direccion.trim().isEmpty()) {
            throw new IllegalArgumentException("Escribe la URL del API REST.");
        }
        try {
            URI uri = new URI(direccion.trim());
            String esquema = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if (!("http".equals(esquema) || "https".equals(esquema)) || uri.getHost() == null
                    || uri.getRawQuery() != null || uri.getRawFragment() != null) {
                throw new IllegalArgumentException("La URL debe empezar con http:// o https://, incluir el servidor y no tener parámetros.");
            }
            String texto = uri.toString();
            while (texto.endsWith("/")) {
                texto = texto.substring(0, texto.length() - 1);
            }
            return texto;
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("La URL del API no es válida.", ex);
        }
    }

    public String endpoint() {
        return base;
    }

    public RespuestaRest autenticar(String usuario, String clave) throws IOException {
        return invocar("POST", "auth/login", objeto("usuario", usuario, "clave", clave), null);
    }

    public RespuestaRest recuperarClave(String usuario, String nuevaClave) throws IOException {
        return invocar("POST", "auth/recover", objeto("usuario", usuario, "nuevaClave", nuevaClave), null);
    }

    public RespuestaRest obtenerCategorias(String token) throws IOException {
        return invocar("GET", "catalog/categories", null, token);
    }

    public RespuestaRest obtenerUnidades(String token, String categoria) throws IOException {
        return invocar("GET", "catalog/categories/" + codificar(categoria) + "/units", null, token);
    }

    public RespuestaRest convertir(String token, String categoria, String origen,
            String destino, double valor) throws IOException {
        JSONObject cuerpo = objeto("categoria", categoria, "unidadOrigen", origen, "unidadDestino", destino);
        try {
            cuerpo.put("valor", valor);
        } catch (JSONException ex) {
            throw new IOException("El valor a convertir no es un número válido.", ex);
        }
        return invocar("POST", "conversions", cuerpo, token);
    }

    private RespuestaRest invocar(String metodo, String ruta, JSONObject cuerpo, String token) throws IOException {
        HttpURLConnection conexion = null;
        try {
            conexion = (HttpURLConnection) url(ruta).openConnection();
            conexion.setConnectTimeout(TIEMPO_CONEXION_MS);
            conexion.setReadTimeout(TIEMPO_RESPUESTA_MS);
            conexion.setRequestMethod(metodo);
            conexion.setUseCaches(false);
            conexion.setRequestProperty("Accept", "application/json");
            if (token != null && !token.trim().isEmpty()) {
                conexion.setRequestProperty("Authorization", "Bearer " + token.trim());
            }
            if (cuerpo != null) {
                byte[] bytes = cuerpo.toString().getBytes(StandardCharsets.UTF_8);
                // Sin modo streaming: con él, un HTTP 401 se trata como desafío de autenticación
                // y se pierde el JSON de error (AUTH-001, AUTH-002, AUTH-003).
                conexion.setDoOutput(true);
                conexion.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                try (OutputStream salida = conexion.getOutputStream()) {
                    salida.write(bytes);
                }
            }

            // Los errores de negocio llegan con HTTP 400/401 y el mismo JSON de respuesta.
            int estadoHttp = conexion.getResponseCode();
            InputStream entrada = estadoHttp >= 400 ? conexion.getErrorStream() : conexion.getInputStream();
            String texto = entrada == null ? "" : leer(entrada);
            if (texto.trim().isEmpty()) {
                throw new IOException(mensajeHttp(estadoHttp));
            }
            JSONObject json;
            try {
                json = new JSONObject(texto);
            } catch (JSONException ex) {
                throw new IOException(estadoHttp >= 400 ? mensajeHttp(estadoHttp)
                        : "El servidor no respondió con JSON. Revise que la URL termine en /api.", ex);
            }
            return leerRespuesta(json, estadoHttp);
        } catch (SocketTimeoutException ex) {
            throw new IOException("El servidor tardó demasiado en responder.", ex);
        } catch (ConnectException | UnknownHostException ex) {
            throw new IOException("No se pudo conectar con el servidor. Revise la URL, la red y que Payara esté encendido.", ex);
        } finally {
            if (conexion != null) {
                conexion.disconnect();
            }
        }
    }

    private static RespuestaRest leerRespuesta(JSONObject json, int estadoHttp) throws IOException {
        String estado = texto(json, "estado");
        String codigo = texto(json, "codigo");
        String mensaje = texto(json, "mensaje");
        if (estado == null || !("EXITO".equalsIgnoreCase(estado) || "ERROR".equalsIgnoreCase(estado))
                || vacio(codigo) || vacio(mensaje)) {
            throw new IOException(estadoHttp >= 400 ? mensajeHttp(estadoHttp)
                    : "El servidor devolvió una respuesta incompleta.");
        }

        Double resultado = null;
        if (!json.isNull("resultado")) {
            try {
                resultado = json.getDouble("resultado");
            } catch (JSONException ex) {
                throw new IOException("El servidor devolvió un resultado no numérico.", ex);
            }
            if (Double.isNaN(resultado) || Double.isInfinite(resultado)) {
                throw new IOException("El servidor devolvió un resultado no finito.");
            }
        }

        return new RespuestaRest(estado, codigo, mensaje, texto(json, "token"),
                texto(json, "expiraEn"), resultado, texto(json, "unidadOrigen"),
                texto(json, "unidadDestino"), lista(json, "categorias"), lista(json, "unidades"));
    }

    private static List<String> lista(JSONObject json, String nombre) throws IOException {
        List<String> valores = new ArrayList<>();
        if (json.isNull(nombre)) {
            return valores;
        }
        JSONArray arreglo = json.optJSONArray(nombre);
        if (arreglo == null) {
            throw new IOException("El servidor devolvió una lista con formato inválido.");
        }
        for (int i = 0; i < arreglo.length(); i++) {
            String valor = arreglo.isNull(i) ? null : arreglo.optString(i, null);
            if (vacio(valor)) {
                throw new IOException("El servidor devolvió una lista incompleta.");
            }
            valores.add(valor);
        }
        return valores;
    }

    /** optString devuelve "null" para JSON null en algunas versiones de Android; se evita aquí. */
    private static String texto(JSONObject json, String nombre) {
        return json.isNull(nombre) ? null : json.optString(nombre, null);
    }

    private static JSONObject objeto(String... pares) throws IOException {
        JSONObject json = new JSONObject();
        try {
            for (int i = 0; i < pares.length; i += 2) {
                if (pares[i + 1] != null) {
                    json.put(pares[i], pares[i + 1]);
                }
            }
        } catch (JSONException ex) {
            throw new IOException("No se pudo preparar la petición.", ex);
        }
        return json;
    }

    private URL url(String ruta) throws IOException {
        try {
            return new URL(base + "/" + ruta);
        } catch (MalformedURLException ex) {
            throw new IOException("La URL del API no es válida.", ex);
        }
    }

    private static String codificar(String valor) throws IOException {
        if (valor == null) {
            throw new IOException("Selecciona una categoría.");
        }
        try {
            return URLEncoder.encode(valor, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException ex) {
            throw new IOException("No se pudo codificar la categoría.", ex);
        }
    }

    private static String leer(InputStream entrada) throws IOException {
        try (InputStream contenido = entrada; ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            byte[] bloque = new byte[4096];
            int leidos;
            while ((leidos = contenido.read(bloque)) != -1) {
                bytes.write(bloque, 0, leidos);
            }
            return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private static String mensajeHttp(int estadoHttp) {
        return estadoHttp == 404
                ? "No se encontró el API en esa URL (HTTP 404). Revise el contexto del WAR y que termine en /api."
                : "El servidor respondió con el estado HTTP " + estadoHttp + ".";
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
