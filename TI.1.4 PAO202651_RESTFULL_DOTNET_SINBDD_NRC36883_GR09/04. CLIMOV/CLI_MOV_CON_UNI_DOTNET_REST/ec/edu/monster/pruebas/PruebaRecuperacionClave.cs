using System;
using System.Threading.Tasks;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;

namespace ec.edu.monster.pruebas;

internal static class PruebaRecuperacionClave
{
    internal static async Task EjecutarAsync()
    {
        var captura = await SoportePruebasRest.EjecutarAsync("{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"ok\"}",
            async uri => await new ClienteRest(uri.ToString()).RecuperarClaveAsync("Monster", "nueva-clave"));
        SoportePruebasRest.Exigir(captura.Respuesta.Codigo == "OK" && captura.Ruta.Contains("recuperar-clave"), "Recuperacion debe llamar su ruta");
        SoportePruebasRest.Exigir(captura.Cuerpo.Contains("\"nuevaClave\":\"nueva-clave\""), "Debe enviar la clave nueva");
    }
}

