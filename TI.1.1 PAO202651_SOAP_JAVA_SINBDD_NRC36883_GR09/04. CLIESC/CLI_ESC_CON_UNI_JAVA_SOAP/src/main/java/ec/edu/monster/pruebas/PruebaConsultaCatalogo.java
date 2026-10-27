package ec.edu.monster.pruebas;

import ec.edu.monster.servicio.ClienteSoap;
import ec.edu.monster.modelo.RespuestaSoap;

final class PruebaConsultaCatalogo {
    static void ejecutar() throws Exception {
        try (SoportePruebasSoap servidor = new SoportePruebasSoap(xml -> SoportePruebasSoap.exito("<categorias>LONGITUD</categorias><categorias>MASA</categorias>"))) {
            ClienteSoap cliente = new ClienteSoap(servidor.uri());
            RespuestaSoap respuesta = cliente.obtenerCategorias("token-test");
            SoportePruebasSoap.exigir(respuesta.categorias().size() == 2
                    && respuesta.categorias().contains("MASA"), "Debe deserializar categorias SOAP");
            SoportePruebasSoap.exigir(servidor.solicitud.contains("obtenerCategorias")
                    && servidor.solicitud.contains("token-test"), "Debe enviar el token para consultar el catalogo");
        }
    }
}

