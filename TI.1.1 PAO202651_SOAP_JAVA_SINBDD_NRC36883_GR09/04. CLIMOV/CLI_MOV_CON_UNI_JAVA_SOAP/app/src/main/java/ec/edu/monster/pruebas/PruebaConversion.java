package ec.edu.monster.pruebas;

import ec.edu.monster.servicio.ClienteSoap;
import ec.edu.monster.modelo.RespuestaSoap;

final class PruebaConversion {
    static void ejecutar() throws Exception {
        try (SoportePruebasSoap servidor = new SoportePruebasSoap(xml -> SoportePruebasSoap.exito("<resultado>200.0</resultado><unidadOrigen>METRO</unidadOrigen><unidadDestino>CENTIMETRO</unidadDestino>"))) {
            ClienteSoap cliente = new ClienteSoap(servidor.uri().toString());
            RespuestaSoap respuesta = cliente.convertir("token-test", "LONGITUD", "METRO", "CENTIMETRO", 2.0);
            SoportePruebasSoap.exigir(respuesta.isExito() && respuesta.resultado() != null
                    && respuesta.resultado() == 200.0, "Conversion debe leer el resultado");
            SoportePruebasSoap.exigir(servidor.solicitud.contains("convertir")
                    && servidor.solicitud.contains("token-test") && servidor.solicitud.contains("<valor>2.0</valor>"),
                    "Debe enviar operacion, token y valor");
        }
    }
}

