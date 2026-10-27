using System;
using System.Threading.Tasks;
using ec.edu.monster.modelo;
using ec.edu.monster.cliente.rest;

namespace ec.edu.monster.pruebas;

internal static class PruebaConsultaUnidades
{
    internal static async Task EjecutarAsync()
    {
        var captura = await SoportePruebasRest.EjecutarAsync("{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"ok\",\"unidades\":[\"METRO\"]}",
            uri => Task.FromResult(new ClienteRest(uri).ObtenerUnidades("token-test", "LONGITUD")));
        SoportePruebasRest.Exigir(captura.Respuesta.Unidades.Contains("METRO") && captura.Ruta.Contains("LONGITUD"), "Debe devolver las unidades de la categoria");
        SoportePruebasRest.Exigir(captura.Autorizacion == "Bearer token-test", "Consulta de unidades debe enviar Bearer");
    }
}

