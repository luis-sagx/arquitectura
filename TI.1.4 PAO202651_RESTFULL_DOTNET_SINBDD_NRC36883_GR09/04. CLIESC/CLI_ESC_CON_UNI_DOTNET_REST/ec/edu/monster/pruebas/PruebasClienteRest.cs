using System;
using System.Net;
using System.Net.Http;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;

namespace ec.edu.monster.pruebas
{
    internal static partial class PruebasClienteRest
    {
        private static int comprobaciones;

        private static int Main(string[] args)
        {
            try
            {
                EjecutarAsync(args).GetAwaiter().GetResult();
                Console.WriteLine("Pruebas del cliente REST correctas: " + comprobaciones + " comprobaciones.");
                return 0;
            }
            catch (Exception error)
            {
                Console.Error.WriteLine("PRUEBA FALLIDA: " + error.Message);
                return 1;
            }
        }

        private static async Task EjecutarAsync(string[] args)
        {
            await VerificarTransporteAsync();
            if (Array.Exists(args, argumento => String.Equals(argumento, "--integracion", StringComparison.OrdinalIgnoreCase)))
                await VerificarIntegracionAsync();
        }

        private static async Task VerificarTransporteAsync()
        {
            await VerificarAutenticacionAsync();
            await VerificarCatalogoAsync();
            await VerificarConversionesAsync();
            await VerificarRecuperacionClaveAsync();
            await VerificarErroresTransporteAsync();
        }

        private static async Task VerificarIntegracionAsync()
        {
            string baseConfigurada = Environment.GetEnvironmentVariable("REST_API_BASE");
            Uri endpoint = new Uri(String.IsNullOrWhiteSpace(baseConfigurada)
                ? "http://localhost:5111/api/ws-con-uni/" : baseConfigurada.TrimEnd('/') + "/");
            string usuario = Environment.GetEnvironmentVariable("REST_TEST_USER") ?? "Monster";
            string claveOriginal = Environment.GetEnvironmentVariable("REST_TEST_PASSWORD") ?? "Monster9";
            string claveTemporal = "Prueba-" + Guid.NewGuid().ToString("N");
            using (var cliente = new ClienteRest(endpoint))
            {
                RespuestaRest login = await cliente.AutenticarAsync(usuario, claveOriginal);
                Check(login.IsExito && !String.IsNullOrWhiteSpace(login.Token), "Integración: autenticación HTTP");
                RespuestaRest categorias = await cliente.ObtenerCategoriasAsync(login.Token);
                Check(categorias.IsExito && categorias.Categorias.Count >= 3, "Integración: carga de categorías");
                RespuestaRest unidades = await cliente.ObtenerUnidadesAsync(login.Token, "LONGITUD");
                Check(unidades.IsExito && unidades.Unidades.Contains("METRO"), "Integración: carga de unidades");
                RespuestaRest conversion = await cliente.ConvertirAsync(login.Token, "LONGITUD", "METRO", "CENTIMETRO", 2);
                Check(conversion.IsExito && conversion.Resultado == 200, "Integración: conversión de unidades");

                try
                {
                    RespuestaRest cambioClave = await cliente.RecuperarClaveAsync(usuario, claveTemporal);
                    Check(cambioClave.IsExito, "Integración: recuperación de clave");
                    RespuestaRest sesionRevocada = await cliente.ObtenerCategoriasAsync(login.Token);
                    Check(sesionRevocada.Codigo == "AUTH-002", "Integración: recuperación debe invalidar el token anterior");
                    RespuestaRest nuevoLogin = await cliente.AutenticarAsync(usuario, claveTemporal);
                    Check(nuevoLogin.IsExito, "Integración: inicio con nueva clave");
                }
                finally
                {
                    await cliente.RecuperarClaveAsync(usuario, claveOriginal);
                }
            }
        }

        private static async Task DebeLanzarIOException(Func<Task<RespuestaRest>> accion, string mensaje)
        {
            bool lanzada = false;
            try { await accion(); }
            catch (System.IO.IOException error) { lanzada = !String.IsNullOrWhiteSpace(error.Message); }
            Check(lanzada, mensaje);
        }

        private static void Check(bool condicion, string mensaje)
        {
            if (!condicion) throw new InvalidOperationException(mensaje);
            comprobaciones++;
        }

        private sealed class Handler : HttpMessageHandler
        {
            internal SolicitudCapturada Ultima;
            internal HttpStatusCode Estado;
            internal string CuerpoRespuesta;
            internal Exception Error;

            internal void Configurar(HttpStatusCode estado, string cuerpo)
            {
                Estado = estado;
                CuerpoRespuesta = cuerpo;
                Error = null;
            }

            protected override async Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
            {
                if (Error != null) throw Error;
                Ultima = new SolicitudCapturada
                {
                    Metodo = request.Method,
                    Uri = request.RequestUri,
                    Authorization = request.Headers.Authorization == null ? null : request.Headers.Authorization.ToString(),
                    Cuerpo = request.Content == null ? String.Empty : await request.Content.ReadAsStringAsync()
                };
                return new HttpResponseMessage(Estado)
                {
                    Content = new StringContent(CuerpoRespuesta ?? String.Empty, Encoding.UTF8, "application/json")
                };
            }
        }

        private sealed class SolicitudCapturada
        {
            internal HttpMethod Metodo;
            internal Uri Uri;
            internal string Authorization;
            internal string Cuerpo;
        }
    }
}
