package ec.edu.monster.pruebas;

import ec.edu.monster.modelo.RespuestaRest;
import ec.edu.monster.servicio.ClienteRest;
import java.net.URI;

final class PruebaConsultaUnidades {
    static void ejecutar() throws Exception {
        try (SoportePruebasRest servidor = new SoportePruebasRest()) {
            servidor.responder("{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"unidades\",\"unidades\":[\"METRO\",\"CENTIMETRO\"]}");
            ClienteRest cliente = new ClienteRest(servidor.endpoint());
            RespuestaRest respuesta = cliente.obtenerUnidades("token-test", "LONGITUD");
            SoportePruebasRest.exigir(respuesta.getUnidades().size() == 2
                    && respuesta.getUnidades().contains("METRO"), "Debe deserializar las unidades");
            SoportePruebasRest.exigir(servidor.ruta.contains("LONGITUD") && servidor.autorizacion.equals("Bearer token-test"),
                    "Consulta de unidades debe incluir categoria y token");
        }
    }
}

