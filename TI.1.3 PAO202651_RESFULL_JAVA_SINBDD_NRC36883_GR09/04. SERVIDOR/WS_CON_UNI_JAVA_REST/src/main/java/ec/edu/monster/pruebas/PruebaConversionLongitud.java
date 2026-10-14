package ec.edu.monster.pruebas;

import ec.edu.monster.servicios.ConversorUnidadesServices;

public class PruebaConversionLongitud {
    public static void main(String[] args) {
        ConversorUnidadesServices servicio = VerificacionesREST.servicio();
        String token = VerificacionesREST.token(servicio);
        VerificacionesREST.resultado(servicio.convertir(token, "LONGITUD", "KILOMETRO", "METRO", 2.5),
            2500, "kilómetros a metros");
        VerificacionesREST.resultado(servicio.convertir(token, "LONGITUD", "MILLA", "PIE", 1.0),
            5280, "millas a pies");
        VerificacionesREST.resultado(servicio.convertir(token, "LONGITUD", "METRO", "CENTIMETRO", 0.0),
            0, "cero metros a centímetros");
        VerificacionesREST.informar("PruebaConversionLongitud");
    }
}
