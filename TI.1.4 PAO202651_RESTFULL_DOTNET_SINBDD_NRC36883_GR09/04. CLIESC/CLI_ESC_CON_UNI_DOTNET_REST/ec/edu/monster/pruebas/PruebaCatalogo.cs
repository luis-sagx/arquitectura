using System;
using System.Net;
using System.Net.Http;
using System.Threading.Tasks;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;

namespace ec.edu.monster.pruebas
{
    internal static partial class PruebasClienteRest
    {
        private static async Task VerificarCatalogoAsync()
        {
            var handler = new Handler();
            using (var cliente = new ClienteRest(new Uri("http://localhost:5111/api/ws-con-uni/"), handler))
            {
handler.Configurar(HttpStatusCode.OK, "{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"Categorias\",\"categorias\":[\"LONGITUD\",\"MASA\"]}");
                RespuestaRest categorias = await cliente.ObtenerCategoriasAsync("token-prueba");
                Check(categorias.Categorias.Count == 2 && categorias.Categorias[0] == "LONGITUD", "Categorías deben deserializarse");
                Check(handler.Ultima.Metodo == HttpMethod.Get && handler.Ultima.Uri.AbsolutePath.EndsWith("/categorias"), "Categorías debe usar GET");
                Check(handler.Ultima.Authorization == "Bearer token-prueba", "Categorías debe enviar Bearer");

                handler.Configurar(HttpStatusCode.OK, "{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"Unidades\",\"unidades\":[\"METRO\"]}");
                RespuestaRest unidades = await cliente.ObtenerUnidadesAsync("token-prueba", "MASA CON ESPACIO");
                Check(unidades.Unidades.Count == 1 && unidades.Unidades[0] == "METRO", "Unidades deben deserializarse");
                Check(handler.Ultima.Uri.Query.Contains("categoria=MASA%20CON%20ESPACIO"), "Categoría debe codificarse en la consulta URI");
                Check(handler.Ultima.Authorization == "Bearer token-prueba", "Unidades debe enviar Bearer");
            }
        }

    }
}