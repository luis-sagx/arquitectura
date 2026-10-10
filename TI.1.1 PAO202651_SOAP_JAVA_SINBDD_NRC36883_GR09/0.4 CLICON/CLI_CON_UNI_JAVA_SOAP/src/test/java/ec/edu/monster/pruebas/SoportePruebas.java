package ec.edu.monster.pruebas;
import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
final class SoportePruebas implements AutoCloseable {
    final HttpServer servidor;
    String ultimaSolicitud;
    int estadoHttp = 200;
    Function<String, String> respuesta;
    SoportePruebas(Function<String, String> respuesta) throws Exception {
        this.respuesta = respuesta;
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/WSConUni", intercambio -> {
            ultimaSolicitud = new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String operacion = operacion(ultimaSolicitud);
            String respuestaXml = this.respuesta.apply(ultimaSolicitud)
                .replace("m:respuesta", "m:" + operacion + "Response");
            byte[] datos = respuestaXml.getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().set("Content-Type", "text/xml; charset=UTF-8");
            intercambio.sendResponseHeaders(estadoHttp, datos.length);
            intercambio.getResponseBody().write(datos);
            intercambio.close();
        });
        servidor.start();
    }
    URI uri() { return URI.create("http://127.0.0.1:" + servidor.getAddress().getPort() + "/WSConUni"); }
    private static String operacion(String solicitud) {
        var patron = java.util.regex.Pattern.compile("<(?:[\\w]+:)?(autenticar|obtenerCategorias|obtenerUnidades|convertir)(?:[\\s>])");
        var coincidencia = patron.matcher(solicitud);
        if (!coincidencia.find()) throw new AssertionError("Solicitud SOAP sin operacion conocida");
        return coincidencia.group(1);
    }
    static String xml(String contenido) {
        return "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body>"
            + "<m:respuesta xmlns:m=\"http://ws.controladores.monster.edu.ec/\"><return>"
            + contenido + "</return></m:respuesta></s:Body></s:Envelope>";
    }
    static String exito(String contenido) {
        return xml("<estado>EXITO</estado><codigo>OK</codigo><mensaje>Operacion correcta</mensaje>" + contenido);
    }
    static void exigir(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
    }
    public void close() { servidor.stop(0); }
}
