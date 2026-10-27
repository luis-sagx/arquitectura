package ec.edu.monster.pruebas;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

/** Servidor HTTP local compatible con Android para las pruebas REST. */
final class SoportePruebasRest implements AutoCloseable {
    private final ServerSocket servidor;
    private final Thread hilo;
    private final AtomicReference<String> respuesta = new AtomicReference<>("{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"ok\"}");
    volatile String metodo;
    volatile String ruta;
    volatile String autorizacion;
    volatile String cuerpo;

    SoportePruebasRest() throws Exception {
        servidor = new ServerSocket(0, 10, InetAddress.getByName("127.0.0.1"));
        hilo = new Thread(() -> {
            while (!servidor.isClosed()) {
                try (Socket cliente = servidor.accept()) { atender(cliente); }
                catch (Exception error) { if (!servidor.isClosed()) throw new IllegalStateException(error); }
            }
        }, "pruebas-rest-http");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void atender(Socket cliente) throws Exception {
        cliente.setSoTimeout(5000);
        InputStream entrada = cliente.getInputStream();
        ByteArrayOutputStream cabecera = new ByteArrayOutputStream();
        int estado = 0;
        while (cabecera.size() < 32768) {
            int siguiente = entrada.read();
            if (siguiente < 0) return;
            cabecera.write(siguiente);
            if ((estado == 0 && siguiente == 13) || (estado == 2 && siguiente == 13)) estado++;
            else if ((estado == 1 && siguiente == 10) || (estado == 3 && siguiente == 10)) estado++;
            else estado = siguiente == 13 ? 1 : 0;
            if (estado == 4) break;
        }
        String textoCabecera = new String(cabecera.toByteArray(), StandardCharsets.ISO_8859_1);
        String[] lineas = textoCabecera.split("\\r\\n");
        String[] inicio = lineas[0].split(" ");
        metodo = inicio[0];
        ruta = inicio.length > 1 ? inicio[1] : "/";
        int longitud = 0;
        autorizacion = null;
        for (String linea : lineas) {
            int separador = linea.indexOf(':');
            if (separador < 0) continue;
            String nombre = linea.substring(0, separador).trim();
            String valor = linea.substring(separador + 1).trim();
            if (nombre.equalsIgnoreCase("Content-Length")) longitud = Integer.parseInt(valor);
            if (nombre.equalsIgnoreCase("Authorization")) autorizacion = valor;
        }
        ByteArrayOutputStream bytesCuerpo = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        while (bytesCuerpo.size() < longitud) {
            int leidos = entrada.read(buffer, 0, Math.min(buffer.length, longitud - bytesCuerpo.size()));
            if (leidos < 0) break;
            bytesCuerpo.write(buffer, 0, leidos);
        }
        cuerpo = new String(bytesCuerpo.toByteArray(), StandardCharsets.UTF_8);
        byte[] datos = respuesta.get().getBytes(StandardCharsets.UTF_8);
        OutputStream salida = cliente.getOutputStream();
        salida.write(("HTTP/1.1 200 OK\r\nContent-Type: application/json; charset=utf-8\r\nContent-Length: "
                + datos.length + "\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.ISO_8859_1));
        salida.write(datos);
        salida.flush();
    }

    String endpoint() { return "http://127.0.0.1:" + servidor.getLocalPort() + "/api/"; }
    void responder(String json) { respuesta.set(json); }
    static void exigir(boolean condicion, String mensaje) { if (!condicion) throw new AssertionError(mensaje); }
    @Override public void close() throws Exception { servidor.close(); hilo.join(1000); }
}
