using System;
using System.Threading.Tasks;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;

namespace ec.edu.monster.pruebas;

internal static class PruebaAutenticacion
{
    internal static async Task EjecutarAsync()
    {
        var captura = await SoportePruebasRest.EjecutarAsync("{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"ok\",\"token\":\"sesion\"}",
            async uri => await new ClienteRest(uri.ToString()).AutenticarAsync("Monster", "Monster9"));
        SoportePruebasRest.Exigir(captura.Respuesta.Codigo == "OK" && captura.Respuesta.Token == "sesion", "Autenticacion debe devolver el token recibido");
        SoportePruebasRest.Exigir(captura.Metodo == "POST" && captura.Ruta.Contains("autenticar"), "Autenticacion debe usar POST en su ruta");
        SoportePruebasRest.Exigir(captura.Cuerpo.Contains("\"usuario\":\"Monster\"") && captura.Cuerpo.Contains("\"clave\":\"Monster9\""), "Debe serializar las credenciales");
    }
}

