package ec.edu.monster.pruebas;

import ec.edu.monster.servicios.ConversorUnidadesServices;

public class PruebaCeroAbsoluto {
    public static void main(String[] args) {
        ConversorUnidadesServices servicio = VerificacionesREST.servicio();
        String token = VerificacionesREST.token(servicio);

        VerificacionesREST.codigo(servicio.convertir(token, "TEMPERATURA", "CELSIUS", "KELVIN", -273.15),
            "DATA-001", "cero absoluto Celsius rechazado");
        VerificacionesREST.codigo(servicio.convertir(token, "TEMPERATURA", "FAHRENHEIT", "CELSIUS", -459.67),
            "DATA-001", "cero absoluto Fahrenheit rechazado");
        VerificacionesREST.codigo(servicio.convertir(token, "TEMPERATURA", "KELVIN", "CELSIUS", 0.0),
            "DATA-001", "cero absoluto Kelvin rechazado");
        VerificacionesREST.resultado(servicio.convertir(token, "TEMPERATURA", "CELSIUS", "KELVIN", -273.149),
            0.001, "temperatura Celsius sobre cero absoluto");
        VerificacionesREST.resultado(servicio.convertir(token, "TEMPERATURA", "FAHRENHEIT", "KELVIN", -459.669),
            0.0005555555555555556, "temperatura Fahrenheit sobre cero absoluto");
        VerificacionesREST.resultado(servicio.convertir(token, "TEMPERATURA", "KELVIN", "CELSIUS", 0.001),
            -273.149, "temperatura Kelvin sobre cero absoluto");
        VerificacionesREST.codigo(servicio.convertir(token, "TEMPERATURA", "CELSIUS", "KELVIN", -273.1501),
            "DATA-001", "temperatura Celsius bajo cero absoluto");
        VerificacionesREST.codigo(servicio.convertir(token, "TEMPERATURA", "FAHRENHEIT", "KELVIN", -459.6701),
            "DATA-001", "temperatura Fahrenheit bajo cero absoluto");
        VerificacionesREST.codigo(servicio.convertir(token, "TEMPERATURA", "KELVIN", "CELSIUS", -0.0001),
            "DATA-001", "temperatura Kelvin bajo cero absoluto");

        VerificacionesREST.informar("PruebaCeroAbsoluto");
    }
}
