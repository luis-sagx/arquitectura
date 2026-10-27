package ec.edu.monster.pruebas;

import ec.edu.monster.modelo.RespuestaRest;
import ec.edu.monster.servicio.ClienteRest;
import java.net.URI;

final class PruebaConsultaCatalogo {
    static void ejecutar() throws Exception {
        try (SoportePruebasRest servidor = new SoportePruebasRest()) {
            servidor.responder("{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"categorias\",\"categorias\":[\"LONGITUD\",\"MASA\",\"TEMPERATURA\"]}");
            ClienteRest cliente = new ClienteRest(servidor.endpoint());
            RespuestaRest respuesta = cliente.obtenerCategorias("token-test");
            SoportePruebasRest.exigir(respuesta.getCategorias().size() == 3
                    && respuesta.getCategorias().contains("MASA"), "Debe deserializar las categorias");
            SoportePruebasRest.exigir("GET".equals(servidor.metodo) && servidor.autorizacion.equals("Bearer token-test"),
                    "Catalogo debe consultar con GET y enviar Bearer");
        }
    }
}

