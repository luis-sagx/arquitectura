package ec.edu.monster.pruebas;

import ec.edu.monster.servicio.ClienteSoap;
import ec.edu.monster.modelo.RespuestaSoap;

final class PruebaAutenticacion {
    static void ejecutar() throws Exception {
        try (SoportePruebasSoap servidor = new SoportePruebasSoap(xml -> SoportePruebasSoap.exito("<token>sesion-test</token>"))) {
            ClienteSoap cliente = new ClienteSoap(servidor.uri().toString());
            RespuestaSoap respuesta = cliente.autenticar("Monster", "Monster9");
            SoportePruebasSoap.exigir(respuesta.isExito() && "sesion-test".equals(respuesta.token()),
                    "Autenticacion debe devolver una sesion");
            SoportePruebasSoap.exigir(servidor.solicitud.contains("autenticar") && servidor.solicitud.contains("Monster9"),
                    "Debe enviar las credenciales en la operacion autenticar");
        }
    }
}

