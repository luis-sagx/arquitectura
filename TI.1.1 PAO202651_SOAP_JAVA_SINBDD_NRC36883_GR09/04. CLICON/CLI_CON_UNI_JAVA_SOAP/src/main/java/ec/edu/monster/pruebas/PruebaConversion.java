package ec.edu.monster.pruebas;

import ec.edu.monster.cliente.ws.ClienteSoap;

public final class PruebaConversion {
    public static void main(String[] args) throws Exception {
        try (var servidor = new SoportePruebas(xml -> SoportePruebas.exito("<resultado>200.0</resultado><unidadOrigen>METRO</unidadOrigen><unidadDestino>CENTIMETRO</unidadDestino>"))) {
            var respuesta = new ClienteSoap(servidor.uri()).convertir("sesion", "LONGITUD", "METRO", "CENTIMETRO", 2);
            SoportePruebas.exigir(respuesta.getResultado() == 200.0, "Debe devolver el resultado de la conversion");
            SoportePruebas.exigir(servidor.ultimaSolicitud.contains("<unidadOrigen>METRO</unidadOrigen>")
                && servidor.ultimaSolicitud.contains("<unidadDestino>CENTIMETRO</unidadDestino>")
                && servidor.ultimaSolicitud.contains("<valor>2.0</valor>"), "Debe enviar los datos de conversion");
        }
        System.out.println("PruebaConversion: OK");
    }
}
