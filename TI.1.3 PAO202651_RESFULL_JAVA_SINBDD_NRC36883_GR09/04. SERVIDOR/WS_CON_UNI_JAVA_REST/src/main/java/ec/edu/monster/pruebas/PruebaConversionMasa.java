package ec.edu.monster.pruebas;

import ec.edu.monster.servicios.ConversorUnidadesServices;

public class PruebaConversionMasa {
    public static void main(String[] args) {
        ConversorUnidadesServices servicio = VerificacionesREST.servicio();
        String token = VerificacionesREST.token(servicio);
        VerificacionesREST.resultado(servicio.convertir(token, "MASA", "KILOGRAMO", "GRAMO", 1.25),
            1250, "kilogramos a gramos");
        VerificacionesREST.resultado(servicio.convertir(token, "MASA", "LIBRA", "KILOGRAMO", 1.0),
            0.45359237, "libras a kilogramos");
        VerificacionesREST.resultado(servicio.convertir(token, "MASA", "MILIGRAMO", "GRAMO", 1000.0),
            1, "miligramos a gramos");
        VerificacionesREST.informar("PruebaConversionMasa");
    }
}
