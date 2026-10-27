package ec.edu.monster.pruebas;

import ec.edu.monster.modelo.RespuestaRest;
import ec.edu.monster.cliente.ClienteRest;
import java.net.URI;

final class PruebaConversion {
    static void ejecutar() throws Exception {
        try (SoportePruebasRest servidor = new SoportePruebasRest()) {
            servidor.responder("{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"convertido\",\"resultado\":200.0}");
            ClienteRest cliente = new ClienteRest(URI.create(servidor.endpoint()));
            RespuestaRest respuesta = cliente.convertir("token-test", "LONGITUD", "METRO", "CENTIMETRO", 2.0);
            SoportePruebasRest.exigir("OK".equals(respuesta.getCodigo()) && respuesta.getResultado() == 200.0,
                    "Conversion debe devolver el resultado recibido");
            SoportePruebasRest.exigir("POST".equals(servidor.metodo) && servidor.ruta.contains("conversion"),
                    "Conversion debe enviar POST a la ruta correspondiente");
            SoportePruebasRest.exigir(servidor.cuerpo.contains("\"valor\":2.0")
                    && servidor.autorizacion.equals("Bearer token-test"), "Conversion debe enviar valor y token");
        }
    }
}

