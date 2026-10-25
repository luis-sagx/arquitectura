package ec.edu.monster.pruebas;

import ec.edu.monster.servicios.ConversorUnidadesServices;

public class PruebaTemperaturaValoresGrandes {
    public static void main(String[] args) {
        ConversorUnidadesServices servicio = VerificacionesREST.servicio();
        String token = VerificacionesREST.token(servicio);

        VerificacionesREST.resultado(servicio.convertir(token, "TEMPERATURA", "CELSIUS", "CELSIUS", 1e300),
            1e300, "valor grande en la misma escala");
        VerificacionesREST.codigo(servicio.convertir(token, "TEMPERATURA", "CELSIUS", "FAHRENHEIT",
            Double.MAX_VALUE), "DATA-001", "resultado de temperatura fuera de rango");

        VerificacionesREST.informar("PruebaTemperaturaValoresGrandes");
    }
}
