package ec.edu.monster.pruebas;

import ec.edu.monster.cliente.ws.ClienteSoap;
import java.util.List;

public final class PruebaConsultaUnidades {
    public static void main(String[] args) throws Exception {
        try (var servidor = new SoportePruebas(xml -> SoportePruebas.exito("<unidades>METRO</unidades><unidades>CENTIMETRO</unidades>"))) {
            var respuesta = new ClienteSoap(servidor.uri()).obtenerUnidades("sesion", "LONGITUD");
            SoportePruebas.exigir(respuesta.getUnidades().equals(List.of("METRO", "CENTIMETRO")), "Debe leer las unidades de la categoria");
            SoportePruebas.exigir(servidor.ultimaSolicitud.contains("<token>sesion</token>")
                && servidor.ultimaSolicitud.contains("<categoria>LONGITUD</categoria>"), "Debe enviar token y categoria");
        }
        System.out.println("PruebaConsultaUnidades: OK");
    }
}
