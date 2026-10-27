using System;
using System.Threading.Tasks;
using ec.edu.monster.modelo;
using ec.edu.monster.cliente.rest;

namespace ec.edu.monster.pruebas;

internal static class PruebaRecuperacionClave
{
    internal static async Task EjecutarAsync()
    {
        var captura = await SoportePruebasRest.EjecutarAsync("{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"ok\"}",
            uri => Task.FromResult(new ClienteRest(uri).RecuperarClave("Monster", "nueva-clave")));
        SoportePruebasRest.Exigir(captura.Respuesta.Codigo == "OK" && captura.Ruta.Contains("recuperar-clave"), "Recuperacion debe llamar su ruta");
        SoportePruebasRest.Exigir(captura.Cuerpo.Contains("\"nuevaClave\":\"nueva-clave\""), "Debe enviar la clave nueva");
    }
}

