package ec.edu.monster.pruebas;

import ec.edu.monster.cliente.ws.ClienteSoap;
import java.util.List;

public final class PruebaConsultaCatalogo {
    public static void main(String[] args) throws Exception {
        try (var servidor = new SoportePruebas(xml -> SoportePruebas.exito("<categorias>LONGITUD</categorias><categorias>MASA</categorias>"))) {
            var respuesta = new ClienteSoap(servidor.uri()).obtenerCategorias("sesion");
            SoportePruebas.exigir(respuesta.getCategorias().equals(List.of("LONGITUD", "MASA")), "Debe leer categorias del catalogo");
            SoportePruebas.exigir(servidor.ultimaSolicitud.contains("<token>sesion</token>"), "Debe enviar el token de sesion");
        }
        System.out.println("PruebaConsultaCatalogo: OK");
    }
}
