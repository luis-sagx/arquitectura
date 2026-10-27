package ec.edu.monster.pruebas;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/** Servidor HTTP local compatible con Android para las pruebas SOAP. */
final class SoportePruebasSoap implements AutoCloseable {
    private final ServerSocket servidor;
    private final Thread hilo;
    volatile String solicitud;
    volatile int estadoHttp = 200;
    final Function<String, String> respuesta;

    SoportePruebasSoap(Function<String, String> respuesta) throws Exception {
        this.respuesta = respuesta;
        servidor = new ServerSocket(0, 10, InetAddress.getByName("127.0.0.1"));
        hilo = new Thread(() -> {
            while (!servidor.isClosed()) {
                try (Socket cliente = servidor.accept()) {
                    atender(cliente);
                } catch (Exception error) {
                    if (!servidor.isClosed()) throw new IllegalStateException(error);
                }
            }
        }, "pruebas-soap-http");
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
        int longitud = 0;
        for (String linea : lineas) {
            if (linea.toLowerCase().startsWith("content-length:")) longitud = Integer.parseInt(linea.substring(linea.indexOf(':') + 1).trim());
        }
        ByteArrayOutputStream cuerpo = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        while (cuerpo.size() < longitud) {
            int leidos = entrada.read(buffer, 0, Math.min(buffer.length, longitud - cuerpo.size()));
            if (leidos < 0) break;
            cuerpo.write(buffer, 0, leidos);
        }
        solicitud = new String(cuerpo.toByteArray(), StandardCharsets.UTF_8);
        String operacion = operacion(solicitud);
        byte[] datos = respuesta.apply(solicitud).replace("m:respuesta", "m:" + operacion + "Response")
                .getBytes(StandardCharsets.UTF_8);
        OutputStream salida = cliente.getOutputStream();
        String estadoTexto = estadoHttp == 200 ? "OK" : "Internal Server Error";
        salida.write(("HTTP/1.1 " + estadoHttp + " " + estadoTexto + "\r\nContent-Type: text/xml; charset=UTF-8\r\nContent-Length: "
                + datos.length + "\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.ISO_8859_1));
        salida.write(datos);
        salida.flush();
    }

    URI uri() { return URI.create("http://127.0.0.1:" + servidor.getLocalPort() + "/WSConUni"); }

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

    @Override public void close() throws Exception { servidor.close(); hilo.join(1000); }
}
