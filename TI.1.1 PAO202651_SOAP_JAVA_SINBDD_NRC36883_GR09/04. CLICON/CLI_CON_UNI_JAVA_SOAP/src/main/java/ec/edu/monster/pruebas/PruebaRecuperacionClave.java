package ec.edu.monster.pruebas;

import ec.edu.monster.cliente.ws.ClienteSoap;

public final class PruebaRecuperacionClave {
    public static void main(String[] args) throws Exception {
        try (var servidor = new SoportePruebas(xml -> SoportePruebas.exito(""))) {
            var respuesta = new ClienteSoap(servidor.uri()).recuperarClave("Monster", "nueva123");
            SoportePruebas.exigir(respuesta.isExito(), "La recuperacion debe aceptar la operacion correcta");
            SoportePruebas.exigir(servidor.ultimaSolicitud.contains("<usuario>Monster</usuario>")
                && servidor.ultimaSolicitud.contains("<nuevaClave>nueva123</nuevaClave>"), "Debe enviar usuario y nueva clave");
        }
        System.out.println("PruebaRecuperacionClave: OK");
    }
}
