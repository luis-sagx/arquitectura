using System;
using System.Collections.Generic;
using System.Reflection;
using System.ServiceModel;
using System.ServiceModel.Description;
using System.Web.Services.Description;
using System.Xml.Schema;
using ec.edu.monster.controladores.ws;
using ec.edu.monster.modelos;
using ec.edu.monster.servicios;

namespace ec.edu.monster.pruebas
{
internal static partial class PruebaServicioUnidades
{
    private static void ConversionesYValidaciones()
    {
        var servicio = new ConversorUnidadesServices();
        string token = Token(servicio);
        Respuesta longitud = servicio.Convertir(token, " longitud ", " metro ", "centimetro", 2.0);
        Resultado(longitud, 200.0);
        Exigir(longitud.Categoria == "LONGITUD" && longitud.UnidadOrigen == "METRO" && longitud.UnidadDestino == "CENTIMETRO",
            "La respuesta debe identificar magnitud y unidades normalizadas");
        string[] unidadesLongitud = { "KILOMETRO", "METRO", "CENTIMETRO", "MILIMETRO", "MILLA", "PIE" };
        double[] metros = { 1000.0, 1.0, 0.01, 0.001, 1609.344, 0.3048 };
        for (int i = 0; i < unidadesLongitud.Length; i++)
        {
            Resultado(servicio.Convertir(token, "LONGITUD", unidadesLongitud[i], "METRO", 1.0), metros[i]);
            Resultado(servicio.Convertir(token, "LONGITUD", "METRO", unidadesLongitud[i], metros[i]), 1.0);
            Resultado(servicio.Convertir(token, "LONGITUD", unidadesLongitud[i], unidadesLongitud[i], 7.5), 7.5);
        }
        Resultado(servicio.Convertir(token, "LONGITUD", "METRO", "PIE", 0.0), 0.0);
        Resultado(servicio.Convertir(token, "MASA", "KILOGRAMO", "GRAMO", 0.0), 0.0);
        Resultado(servicio.Convertir(token, "MASA", "KILOGRAMO", "LIBRA", 0.45359237), 1.0);
        Resultado(servicio.Convertir(token, "MASA", "KILOGRAMO", "GRAMO", 1.0), 1000.0);
        Resultado(servicio.Convertir(token, "MASA", "KILOGRAMO", "MILIGRAMO", 1.0), 1000000.0);
        Resultado(servicio.Convertir(token, "TEMPERATURA", "CELSIUS", "FAHRENHEIT", -40.0), -40.0);
        Resultado(servicio.Convertir(token, "TEMPERATURA", "CELSIUS", "FAHRENHEIT", 100.0), 212.0);
        Resultado(servicio.Convertir(token, "TEMPERATURA", "CELSIUS", "KELVIN", 100.0), 373.15);

        Resultado(servicio.Convertir(token, "TEMPERATURA", "CELSIUS", "KELVIN", -273.15), 0.0);
        Resultado(servicio.Convertir(token, "TEMPERATURA", "FAHRENHEIT", "KELVIN", -459.67), 0.0);
        Resultado(servicio.Convertir(token, "TEMPERATURA", "KELVIN", "CELSIUS", 0.0), -273.15);
        Codigo(servicio.Convertir(token, "TEMPERATURA", "CELSIUS", "KELVIN", -273.150001), "DATA-001");
        Codigo(servicio.Convertir(token, "TEMPERATURA", "FAHRENHEIT", "KELVIN", -459.670001), "DATA-001");

        Resultado(servicio.Convertir(token, "TEMPERATURA", "FAHRENHEIT", "CELSIUS", 1e308), 5.555555555555556e307);
        Resultado(servicio.Convertir(token, "TEMPERATURA", "FAHRENHEIT", "FAHRENHEIT", 1e308), 1e308);
        Resultado(servicio.Convertir(token, "TEMPERATURA", "CELSIUS", "KELVIN", 1e308), 1e308);

        Codigo(servicio.Convertir(token, "LONGITUD", "METRO", "PIE", null), "DATA-001");
        Codigo(servicio.Convertir(token, "LONGITUD", "METRO", "PIE", Double.NaN), "DATA-001");
        Codigo(servicio.Convertir(token, "LONGITUD", "METRO", "PIE", Double.PositiveInfinity), "DATA-001");
        Codigo(servicio.Convertir(token, "LONGITUD", "METRO", "PIE", Double.NegativeInfinity), "DATA-001");
        Codigo(servicio.Convertir(token, "LONGITUD", "METRO", "PIE", -1.0), "DATA-001");
        Codigo(servicio.Convertir(token, "MASA", "KILOGRAMO", "GRAMO", -1.0), "DATA-001");
        Codigo(servicio.Convertir(token, null, "METRO", "PIE", 1.0), "DATA-001");
        Codigo(servicio.Convertir(token, "LONGITUD", "", "PIE", 1.0), "DATA-001");
        Codigo(servicio.Convertir(token, "LONGITUD", "METRO", "LIBRA", 1.0), "DATA-001");
        Codigo(servicio.Convertir(token, "LONGITUD", "DESCONOCIDA", "METRO", 1.0), "UNIT-001");
        Codigo(servicio.Convertir(token, "LONGITUD", "MILLA", "CENTIMETRO", Double.MaxValue), "DATA-001");
        Codigo(servicio.Convertir("inventado", null, null, null, null), "AUTH-002");
    }
}
}