package ec.edu.monster.pruebas;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

final class SoportePruebasSoap implements AutoCloseable {
    private final HttpServer servidor;
    volatile String solicitud;
    volatile int estadoHttp = 200;
    Function<String, String> respuesta;

    SoportePruebasSoap(Function<String, String> respuesta) throws Exception {
        this.respuesta = respuesta;
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/WSConUni", intercambio -> {
            solicitud = new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String operacion = operacion(solicitud);
            byte[] datos = respuesta.apply(solicitud).replace("m:respuesta", "m:" + operacion + "Response")
                    .getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().set("Content-Type", "text/xml; charset=UTF-8");
            intercambio.sendResponseHeaders(estadoHttp, datos.length);
            intercambio.getResponseBody().write(datos);
            intercambio.close();
        });
        servidor.start();
    }

    URI uri() {
        return URI.create("http://127.0.0.1:" + servidor.getAddress().getPort() + "/WSConUni");
    }

    private static String operacion(String xml) {
        var patron = java.util.regex.Pattern.compile("<(?:[\\w]+:)?(autenticar|recuperarClave|obtenerCategorias|obtenerUnidades|convertir)(?:[\\s>])");
        var coincidencia = patron.matcher(xml);
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

    @Override
    public void close() {
        servidor.stop(0);
    }
}

