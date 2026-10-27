package ec.edu.monster.pruebas;

import ec.edu.monster.servicio.ClienteSoap;
import ec.edu.monster.modelo.RespuestaSoap;

final class PruebaConsultaUnidades {
    static void ejecutar() throws Exception {
        try (SoportePruebasSoap servidor = new SoportePruebasSoap(xml -> SoportePruebasSoap.exito("<unidades>METRO</unidades><unidades>CENTIMETRO</unidades>"))) {
            ClienteSoap cliente = new ClienteSoap(servidor.uri());
            RespuestaSoap respuesta = cliente.obtenerUnidades("token-test", "LONGITUD");
            SoportePruebasSoap.exigir(respuesta.unidades().size() == 2
                    && respuesta.unidades().contains("METRO"), "Debe deserializar unidades SOAP");
            SoportePruebasSoap.exigir(servidor.solicitud.contains("obtenerUnidades")
                    && servidor.solicitud.contains("LONGITUD"), "Debe enviar la categoria consultada");
        }
    }
}

