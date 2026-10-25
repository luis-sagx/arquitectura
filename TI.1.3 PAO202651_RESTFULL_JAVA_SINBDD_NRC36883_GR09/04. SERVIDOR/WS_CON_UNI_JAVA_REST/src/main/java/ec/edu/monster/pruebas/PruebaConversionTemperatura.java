package ec.edu.monster.pruebas;

import ec.edu.monster.servicios.ConversorUnidadesServices;

public class PruebaConversionTemperatura {
    public static void main(String[] args) {
        ConversorUnidadesServices servicio = VerificacionesREST.servicio();
        String token = VerificacionesREST.token(servicio);
        VerificacionesREST.resultado(servicio.convertir(token, "TEMPERATURA", "CELSIUS", "FAHRENHEIT", 0.0),
            32, "cero Celsius a Fahrenheit");
        VerificacionesREST.resultado(servicio.convertir(token, "TEMPERATURA", "FAHRENHEIT", "CELSIUS", 212.0),
            100, "ebullición Fahrenheit a Celsius");
        VerificacionesREST.resultado(servicio.convertir(token, "TEMPERATURA", "KELVIN", "CELSIUS", 273.15),
            0, "Kelvin a Celsius");
        VerificacionesREST.informar("PruebaConversionTemperatura");
    }
}
