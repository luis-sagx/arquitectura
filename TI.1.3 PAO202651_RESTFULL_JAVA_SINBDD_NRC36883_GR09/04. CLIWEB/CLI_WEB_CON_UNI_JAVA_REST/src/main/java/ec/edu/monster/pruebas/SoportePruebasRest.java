package ec.edu.monster.pruebas;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

final class SoportePruebasRest implements AutoCloseable {
    private final HttpServer servidor;
    private final AtomicReference<String> respuesta = new AtomicReference<>("{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"ok\"}");
    volatile String metodo;
    volatile String ruta;
    volatile String autorizacion;
    volatile String cuerpo;

    SoportePruebasRest() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", intercambio -> {
            metodo = intercambio.getRequestMethod();
            ruta = intercambio.getRequestURI().toString();
            autorizacion = intercambio.getRequestHeaders().getFirst("Authorization");
            cuerpo = new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            byte[] bytes = respuesta.get().getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            intercambio.sendResponseHeaders(200, bytes.length);
            intercambio.getResponseBody().write(bytes);
            intercambio.close();
        });
        servidor.start();
    }

    String endpoint() {
        return "http://127.0.0.1:" + servidor.getAddress().getPort() + "/api/";
    }

    void responder(String json) {
        respuesta.set(json);
    }

    static void exigir(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
    }

    @Override
    public void close() {
        servidor.stop(0);
    }
}

