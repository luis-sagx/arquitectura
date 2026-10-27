package ec.edu.monster.pruebas;

import ec.edu.monster.cliente.ws.ClienteSoap;

public final class PruebaAutenticacion {
    public static void main(String[] args) throws Exception {
        try (var servidor = new SoportePruebas(xml -> SoportePruebas.exito("<token>sesion</token>"))) {
            var respuesta = new ClienteSoap(servidor.uri()).autenticar("Monster", "Monster9");
            SoportePruebas.exigir(respuesta.isExito() && "sesion".equals(respuesta.getToken()), "Autenticacion debe entregar el token");
            SoportePruebas.exigir(servidor.ultimaSolicitud.contains("<usuario>Monster</usuario>")
                && servidor.ultimaSolicitud.contains("<clave>Monster9</clave>"), "Debe enviar las credenciales SOAP");
        }
        System.out.println("PruebaAutenticacion: OK");
    }
}
