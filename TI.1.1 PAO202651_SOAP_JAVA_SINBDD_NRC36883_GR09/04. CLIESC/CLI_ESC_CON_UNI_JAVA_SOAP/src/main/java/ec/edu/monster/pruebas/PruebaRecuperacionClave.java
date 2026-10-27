package ec.edu.monster.pruebas;

import ec.edu.monster.servicio.ClienteSoap;
import ec.edu.monster.modelo.RespuestaSoap;

final class PruebaRecuperacionClave {
    static void ejecutar() throws Exception {
        try (SoportePruebasSoap servidor = new SoportePruebasSoap(xml -> SoportePruebasSoap.exito(""))) {
            ClienteSoap cliente = new ClienteSoap(servidor.uri());
            RespuestaSoap respuesta = cliente.recuperarClave("Monster", "nueva-clave");
            SoportePruebasSoap.exigir(respuesta.isExito(), "Recuperacion debe conservar la respuesta exitosa");
            SoportePruebasSoap.exigir(servidor.solicitud.contains("recuperarClave")
                    && servidor.solicitud.contains("nueva-clave"), "Debe enviar la nueva clave");
        }
    }
}

