package ec.edu.monster.pruebas;

import java.net.http.HttpResponse;

/** Prueba de extremo a extremo contra un WAR REST desplegado y en ejecución. */
public class PruebaIntegracionREST {
    private static final String USUARIO = "Monster";
    private static final String CLAVE_INICIAL = "Monster9";
    private static final String CLAVE_TEMPORAL = "clave-prueba-temporal";

    public static void main(String[] args) throws Exception {
        String baseUrl = baseUrl(args);
        probarAPI(baseUrl);
        System.out.println("Prueba HTTP REST: " + Verificaciones.realizadas() + " verificaciones OK en " + baseUrl);
    }

    private static void probarAPI(String baseUrl) throws Exception {
        HttpResponse<String> sinToken = Verificaciones.get(baseUrl, "catalog/categories", null);
        Verificaciones.http(401, sinToken, "catálogo sin token");
        Verificaciones.codigoJson("AUTH-002", sinToken.body(), "catálogo sin token");

        HttpResponse<String> bearerInvalido = Verificaciones.get(baseUrl, "catalog/categories", "Bearer invalido");
        Verificaciones.http(401, bearerInvalido, "catálogo con token inválido");
        Verificaciones.codigoJson("AUTH-002", bearerInvalido.body(), "catálogo con token inválido");

        HttpResponse<String> credencialesIncorrectas = Verificaciones.post(baseUrl, "auth/login",
            "{\"usuario\":\"Monster\",\"clave\":\"incorrecta\"}", null);
        Verificaciones.http(401, credencialesIncorrectas, "autenticación incorrecta");
        Verificaciones.codigoJson("AUTH-001", credencialesIncorrectas.body(), "autenticación incorrecta");

        HttpResponse<String> login = Verificaciones.post(baseUrl, "auth/login",
            "{\"usuario\":\"" + USUARIO + "\",\"clave\":\"" + CLAVE_INICIAL + "\"}", null);
        Verificaciones.http(200, login, "autenticación válida");
        Verificaciones.codigoJson("OK", login.body(), "autenticación válida");
        Verificaciones.contiene(login.headers().firstValue("Content-Type").orElse(""), "application/json",
            "tipo de contenido del login");
        String token = Verificaciones.cadenaJson(login.body(), "token");
        Verificaciones.comprobar(token != null && !token.isBlank(), "login debe devolver token");
        Verificaciones.comprobar(Verificaciones.cadenaJson(login.body(), "expiraEn") != null,
            "login debe devolver vencimiento");
        String bearer = "Bearer " + token;

        HttpResponse<String> categorias = Verificaciones.get(baseUrl, "catalog/categories", bearer);
        Verificaciones.http(200, categorias, "consulta de categorías");
        Verificaciones.codigoJson("OK", categorias.body(), "consulta de categorías");
        Verificaciones.contiene(categorias.body(), "LONGITUD", "catálogo de categorías");
        Verificaciones.contiene(categorias.body(), "MASA", "catálogo de categorías");
        Verificaciones.contiene(categorias.body(), "TEMPERATURA", "catálogo de categorías");

        HttpResponse<String> unidades = Verificaciones.get(baseUrl, "catalog/categories/LONGITUD/units", bearer);
        Verificaciones.http(200, unidades, "consulta de unidades");
        Verificaciones.codigoJson("OK", unidades.body(), "consulta de unidades");
        Verificaciones.contiene(unidades.body(), "METRO", "unidades de longitud");

        probarConversion(baseUrl, bearer, "LONGITUD", "KILOMETRO", "METRO", 2.5, 2500.0);
        probarConversion(baseUrl, bearer, "MASA", "KILOGRAMO", "GRAMO", 1.25, 1250.0);
        probarConversion(baseUrl, bearer, "TEMPERATURA", "CELSIUS", "FAHRENHEIT", 0.0, 32.0);

        HttpResponse<String> unidadInvalida = Verificaciones.post(baseUrl, "conversions",
            conversionJson("LONGITUD", "METRO", "YARDA", 1.0), bearer);
        Verificaciones.http(400, unidadInvalida, "unidad no soportada");
        Verificaciones.codigoJson("UNIT-001", unidadInvalida.body(), "unidad no soportada");

        HttpResponse<String> incompatibles = Verificaciones.post(baseUrl, "conversions",
            conversionJson("LONGITUD", "METRO", "GRAMO", 1.0), bearer);
        Verificaciones.http(400, incompatibles, "magnitudes incompatibles");
        Verificaciones.codigoJson("DATA-001", incompatibles.body(), "magnitudes incompatibles");

        HttpResponse<String> bajoCeroAbsoluto = Verificaciones.post(baseUrl, "conversions",
            conversionJson("TEMPERATURA", "KELVIN", "CELSIUS", -1.0), bearer);
        Verificaciones.http(400, bajoCeroAbsoluto, "temperatura bajo cero absoluto");
        Verificaciones.codigoJson("DATA-001", bajoCeroAbsoluto.body(), "temperatura bajo cero absoluto");

        probarRecuperacionYRestaurarClave(baseUrl, token);
    }

    private static void probarConversion(String baseUrl, String bearer, String categoria,
            String origen, String destino, double valor, double esperado) throws Exception {
        HttpResponse<String> respuesta = Verificaciones.post(baseUrl, "conversions",
            conversionJson(categoria, origen, destino, valor), bearer);
        String operacion = "conversión " + categoria + " " + origen + " a " + destino;
        Verificaciones.http(200, respuesta, operacion);
        Verificaciones.codigoJson("OK", respuesta.body(), operacion);
        Verificaciones.cerca(esperado, Verificaciones.numeroJson(respuesta.body(), "resultado"), operacion);
    }

    private static void probarRecuperacionYRestaurarClave(String baseUrl, String token) throws Exception {
        HttpResponse<String> claveVacia = Verificaciones.post(baseUrl, "auth/recover",
            "{\"usuario\":\"" + USUARIO + "\",\"nuevaClave\":\" \"}", null);
        Verificaciones.http(400, claveVacia, "recuperación con clave vacía");
        Verificaciones.codigoJson("DATA-001", claveVacia.body(), "recuperación con clave vacía");

        HttpResponse<String> usuarioInexistente = Verificaciones.post(baseUrl, "auth/recover",
            "{\"usuario\":\"inexistente\",\"nuevaClave\":\"nueva\"}", null);
        Verificaciones.http(401, usuarioInexistente, "recuperación con usuario inexistente");
        Verificaciones.codigoJson("AUTH-001", usuarioInexistente.body(), "recuperación con usuario inexistente");

        try {
            HttpResponse<String> recuperacion = Verificaciones.post(baseUrl, "auth/recover",
                "{\"usuario\":\"" + USUARIO + "\",\"nuevaClave\":\"" + CLAVE_TEMPORAL + "\"}", null);
            Verificaciones.http(200, recuperacion, "recuperación de clave");
            Verificaciones.codigoJson("OK", recuperacion.body(), "recuperación de clave");
            HttpResponse<String> sesionAnterior = Verificaciones.get(baseUrl, "catalog/categories", "Bearer " + token);
            Verificaciones.http(401, sesionAnterior, "recuperación invalida sesiones previas");
            Verificaciones.codigoJson("AUTH-002", sesionAnterior.body(), "sesión previa invalidada");

            HttpResponse<String> claveAnterior = Verificaciones.post(baseUrl, "auth/login",
                "{\"usuario\":\"" + USUARIO + "\",\"clave\":\"" + CLAVE_INICIAL + "\"}", null);
            Verificaciones.http(401, claveAnterior, "clave anterior inválida");
            Verificaciones.codigoJson("AUTH-001", claveAnterior.body(), "clave anterior inválida");

            HttpResponse<String> claveNueva = Verificaciones.post(baseUrl, "auth/login",
                "{\"usuario\":\"" + USUARIO + "\",\"clave\":\"" + CLAVE_TEMPORAL + "\"}", null);
            Verificaciones.http(200, claveNueva, "clave recuperada permite iniciar sesión");
            Verificaciones.codigoJson("OK", claveNueva.body(), "clave recuperada permite iniciar sesión");
        } finally {
            HttpResponse<String> restauracion = Verificaciones.post(baseUrl, "auth/recover",
                "{\"usuario\":\"" + USUARIO + "\",\"nuevaClave\":\"" + CLAVE_INICIAL + "\"}", null);
            if (restauracion.statusCode() != 200) {
                throw new AssertionError("No se pudo restaurar la clave Monster9; respuesta: " + restauracion.body());
            }
        }
    }

    private static String conversionJson(String categoria, String origen, String destino, double valor) {
        return "{\"categoria\":\"" + categoria + "\",\"unidadOrigen\":\"" + origen
            + "\",\"unidadDestino\":\"" + destino + "\",\"valor\":" + valor + "}";
    }

    private static String baseUrl(String[] args) {
        String baseUrl = args.length > 0 ? args[0] : System.getenv("REST_BASE_URL");
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:8080/WS_CON_UNI_JAVA_REST/api";
        }
        while (baseUrl.endsWith("/")) baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        return baseUrl;
    }
}
