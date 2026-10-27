package ec.edu.monster.pruebas;

import ec.edu.monster.modelo.RespuestaRest;
import ec.edu.monster.servicio.ClienteRest;
import java.net.URI;

final class PruebaAutenticacion {
    static void ejecutar() throws Exception {
        try (SoportePruebasRest servidor = new SoportePruebasRest()) {
            servidor.responder("{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"sesion\",\"token\":\"token-test\"}");
            ClienteRest cliente = new ClienteRest(servidor.endpoint());
            RespuestaRest respuesta = cliente.autenticar("Monster", "Monster9");
            SoportePruebasRest.exigir("OK".equals(respuesta.getCodigo()) && "token-test".equals(respuesta.getToken()),
                    "Autenticacion debe devolver codigo OK y token");
            SoportePruebasRest.exigir("POST".equals(servidor.metodo) && servidor.ruta.contains("login"),
                    "Autenticacion debe enviar POST a la ruta de inicio de sesion");
            SoportePruebasRest.exigir(servidor.cuerpo.contains("\"usuario\":\"Monster\"")
                    && servidor.cuerpo.contains("\"clave\":\"Monster9\""), "Debe enviar las credenciales como JSON");
        }
    }
}

