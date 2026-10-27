using System;
using System.Threading.Tasks;
using ec.edu.monster.modelo;
using ec.edu.monster.cliente.rest;

namespace ec.edu.monster.pruebas;

internal static class PruebaConversion
{
    internal static async Task EjecutarAsync()
    {
        var captura = await SoportePruebasRest.EjecutarAsync("{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"ok\",\"resultado\":200}",
            uri => Task.FromResult(new ClienteRest(uri).Convertir("token-test", "LONGITUD", "METRO", "CENTIMETRO", 2)));
        SoportePruebasRest.Exigir(captura.Respuesta.Resultado == 200 && captura.Cuerpo.Contains("\"valor\""), "Conversion debe devolver resultado y enviar el valor");
        SoportePruebasRest.Exigir(captura.Metodo == "POST" && captura.Autorizacion == "Bearer token-test", "Conversion debe usar POST con token");
    }
}

