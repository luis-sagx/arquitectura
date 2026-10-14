package ec.edu.monster.pruebas;

import ec.edu.monster.servicios.ConversorUnidadesServices;

public class PruebaCeroAbsoluto {
    public static void main(String[] args) {
        ConversorUnidadesServices servicio = VerificacionesREST.servicio();
        String token = VerificacionesREST.token(servicio);

        VerificacionesREST.resultado(servicio.convertir(token, "TEMPERATURA", "CELSIUS", "KELVIN", -273.15),
            0, "cero absoluto Celsius");
        VerificacionesREST.resultado(servicio.convertir(token, "TEMPERATURA", "FAHRENHEIT", "CELSIUS", -459.67),
            -273.15, "cero absoluto Fahrenheit");
        VerificacionesREST.resultado(servicio.convertir(token, "TEMPERATURA", "KELVIN", "CELSIUS", 0.0),
            -273.15, "cero absoluto Kelvin");
        VerificacionesREST.codigo(servicio.convertir(token, "TEMPERATURA", "CELSIUS", "KELVIN", -273.1501),
            "DATA-001", "temperatura Celsius bajo cero absoluto");
        VerificacionesREST.codigo(servicio.convertir(token, "TEMPERATURA", "FAHRENHEIT", "KELVIN", -459.6701),
            "DATA-001", "temperatura Fahrenheit bajo cero absoluto");
        VerificacionesREST.codigo(servicio.convertir(token, "TEMPERATURA", "KELVIN", "CELSIUS", -0.0001),
            "DATA-001", "temperatura Kelvin bajo cero absoluto");

        VerificacionesREST.informar("PruebaCeroAbsoluto");
    }
}
