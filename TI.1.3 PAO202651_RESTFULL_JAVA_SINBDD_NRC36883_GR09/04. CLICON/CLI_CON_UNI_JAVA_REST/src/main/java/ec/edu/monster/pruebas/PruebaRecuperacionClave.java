package ec.edu.monster.pruebas;

import ec.edu.monster.modelo.RespuestaRest;
import ec.edu.monster.cliente.ClienteRest;
import java.net.URI;

final class PruebaRecuperacionClave {
    static void ejecutar() throws Exception {
        try (SoportePruebasRest servidor = new SoportePruebasRest()) {
            ClienteRest cliente = new ClienteRest(URI.create(servidor.endpoint()));
            RespuestaRest respuesta = cliente.recuperarClave("Monster", "nueva-clave");
            SoportePruebasRest.exigir("OK".equals(respuesta.getCodigo()), "Recuperacion debe devolver codigo OK");
            SoportePruebasRest.exigir("POST".equals(servidor.metodo) && servidor.ruta.contains("recover"),
                    "Recuperacion debe enviar POST a la ruta correspondiente");
            SoportePruebasRest.exigir(servidor.cuerpo.contains("\"nuevaClave\":\"nueva-clave\""),
                    "Debe enviar la nueva clave en el cuerpo JSON");
            SoportePruebasRest.exigir(servidor.autorizacion == null, "Recuperacion no debe requerir token previo");
        }
    }
}

