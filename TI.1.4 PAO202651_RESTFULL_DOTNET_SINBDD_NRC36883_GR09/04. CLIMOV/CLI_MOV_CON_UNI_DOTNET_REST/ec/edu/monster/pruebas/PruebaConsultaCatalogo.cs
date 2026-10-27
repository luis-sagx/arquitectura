using System;
using System.Threading.Tasks;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;

namespace ec.edu.monster.pruebas;

internal static class PruebaConsultaCatalogo
{
    internal static async Task EjecutarAsync()
    {
        var captura = await SoportePruebasRest.EjecutarAsync("{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"ok\",\"categorias\":[\"LONGITUD\",\"MASA\"]}",
            async uri => await new ClienteRest(uri.ToString()).ObtenerCategoriasAsync("token-test"));
        SoportePruebasRest.Exigir(captura.Respuesta.Categorias.Count == 2 && captura.Respuesta.Categorias.Contains("MASA"), "Debe leer las categorias");
        SoportePruebasRest.Exigir(captura.Metodo == "GET" && captura.Autorizacion == "Bearer token-test", "Consulta de catalogo debe usar GET con Bearer");
    }
}

