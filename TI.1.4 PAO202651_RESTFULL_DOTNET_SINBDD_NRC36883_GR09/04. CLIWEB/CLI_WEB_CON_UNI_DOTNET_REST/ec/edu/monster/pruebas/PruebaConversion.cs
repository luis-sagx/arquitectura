using System;
using System.Threading.Tasks;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;

namespace ec.edu.monster.pruebas;

internal static class PruebaConversion
{
    internal static async Task EjecutarAsync()
    {
        var captura = await SoportePruebasRest.EjecutarAsync("{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"ok\",\"resultado\":200}",
            async uri => await new ClienteRest(uri.ToString()).ConvertirAsync("token-test", "LONGITUD", "METRO", "CENTIMETRO", 2));
        SoportePruebasRest.Exigir(captura.Respuesta.Resultado == 200 && captura.Cuerpo.Contains("\"valor\""), "Conversion debe devolver resultado y enviar el valor");
        SoportePruebasRest.Exigir(captura.Metodo == "POST" && captura.Autorizacion == "Bearer token-test", "Conversion debe usar POST con token");
    }
}

