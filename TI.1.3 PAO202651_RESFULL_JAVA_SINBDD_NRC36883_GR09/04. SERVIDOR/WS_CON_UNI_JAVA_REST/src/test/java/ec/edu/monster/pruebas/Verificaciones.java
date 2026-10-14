package ec.edu.monster.pruebas;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Verificaciones {
    private static int realizadas;

    private Verificaciones() {
    }

    static HttpResponse<String> get(String baseUrl, String ruta, String autorizacion) throws Exception {
        HttpRequest.Builder solicitud = HttpRequest.newBuilder(URI.create(baseUrl + "/" + ruta))
            .timeout(Duration.ofSeconds(10)).GET();
        if (autorizacion != null) solicitud.header("Authorization", autorizacion);
        return http().send(solicitud.build(), HttpResponse.BodyHandlers.ofString());
    }

    static HttpResponse<String> post(String baseUrl, String ruta, String json, String autorizacion) throws Exception {
        HttpRequest.Builder solicitud = HttpRequest.newBuilder(URI.create(baseUrl + "/" + ruta))
            .timeout(Duration.ofSeconds(10))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json));
        if (autorizacion != null) solicitud.header("Authorization", autorizacion);
        return http().send(solicitud.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static HttpClient http() {
        return HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    static void http(int esperado, HttpResponse<String> respuesta, String operacion) {
        comprobar(respuesta.statusCode() == esperado,
            operacion + ": HTTP esperado " + esperado + ", recibido " + respuesta.statusCode()
                + "; cuerpo=" + respuesta.body());
    }

    static void codigo(String esperado, String recibido, String estado, String operacion) {
        comprobar(esperado.equals(recibido), operacion + ": código esperado " + esperado + ", recibido " + recibido);
        String estadoEsperado = "OK".equals(esperado) ? "EXITO" : "ERROR";
        comprobar(estadoEsperado.equals(estado), operacion + ": estado esperado " + estadoEsperado + ", recibido " + estado);
    }

    static void codigoJson(String esperado, String cuerpo, String operacion) {
        codigo(esperado, cadenaJson(cuerpo, "codigo"), cadenaJson(cuerpo, "estado"), operacion);
        comprobar(cadenaJson(cuerpo, "mensaje") != null, operacion + ": falta mensaje en JSON");
    }

    static String cadenaJson(String json, String campo) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(campo)
            + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    static double numeroJson(String json, String campo) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(campo)
            + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?)").matcher(json);
        if (!matcher.find()) throw new AssertionError("Falta el número " + campo + " en JSON: " + json);
        return Double.parseDouble(matcher.group(1));
    }

    static void contiene(String texto, String esperado, String operacion) {
        comprobar(texto.contains(esperado), operacion + ": no aparece " + esperado + "; texto=" + texto);
    }

    static void cerca(double esperado, double recibido, String operacion) {
        comprobar(Math.abs(esperado - recibido) <= 1e-9 * Math.max(1.0, Math.abs(esperado)),
            operacion + ": esperado " + esperado + ", recibido " + recibido);
    }

    static void comprobar(boolean condicion, String mensaje) {
        realizadas++;
        if (!condicion) throw new AssertionError(mensaje);
    }

    static int realizadas() {
        return realizadas;
    }
}
