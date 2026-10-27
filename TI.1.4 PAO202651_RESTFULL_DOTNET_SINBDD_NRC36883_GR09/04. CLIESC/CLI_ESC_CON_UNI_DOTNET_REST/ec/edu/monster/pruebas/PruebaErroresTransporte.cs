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
        private static async Task VerificarErroresTransporteAsync()
        {
            var handler = new Handler();
            using (var cliente = new ClienteRest(new Uri("http://localhost:5111/api/ws-con-uni/"), handler))
            {
handler.Configurar(HttpStatusCode.Unauthorized, "{\"estado\":\"ERROR\",\"codigo\":\"AUTH-001\",\"mensaje\":\"Credenciales invalidas\"}");
                RespuestaRest credenciales = await cliente.AutenticarAsync("Monster", "mal");
                Check(!credenciales.IsExito && credenciales.Codigo == "AUTH-001", "HTTP 401 debe conservar el error de negocio JSON");

                handler.Configurar(HttpStatusCode.BadRequest, "{\"estado\":\"ERROR\",\"codigo\":\"DATA-001\",\"mensaje\":\"Categoria inexistente\"}");
                RespuestaRest categoriaInvalida = await cliente.ObtenerUnidadesAsync("token-prueba", "X");
                Check(!categoriaInvalida.IsExito && categoriaInvalida.Codigo == "DATA-001", "HTTP 400 debe conservar el error de negocio JSON");

                handler.Configurar(HttpStatusCode.InternalServerError, "<html>fallo</html>");
                await DebeLanzarIOException(() => cliente.ObtenerCategoriasAsync("token-prueba"), "JSON inválido debe convertirse en IOException legible");
                handler.Error = new HttpRequestException("conexión cerrada");
                await DebeLanzarIOException(() => cliente.ObtenerCategoriasAsync("token-prueba"), "Fallo de transporte debe convertirse en IOException");
            }

            bool rechazoEndpoint = false;
            try { using (var clienteInvalido = new ClienteRest(new Uri("ftp://localhost/api/", UriKind.Absolute))) { } }
            catch (ArgumentException) { rechazoEndpoint = true; }
            Check(rechazoEndpoint, "El cliente debe rechazar endpoint que no use HTTP o HTTPS");

    }
}
