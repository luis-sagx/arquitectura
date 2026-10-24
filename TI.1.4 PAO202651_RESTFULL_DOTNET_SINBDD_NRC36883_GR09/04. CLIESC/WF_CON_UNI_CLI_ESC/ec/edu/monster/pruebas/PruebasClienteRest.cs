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
    internal static class PruebasClienteRest
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
            var handler = new Handler();
            using (var cliente = new ClienteRest(new Uri("http://localhost:5111/api/ws-con-uni/"), handler))
            {
                handler.Configurar(HttpStatusCode.OK, "{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"Sesion iniciada\",\"token\":\"token-prueba\",\"expiraEn\":\"2026-10-21T12:00:00Z\"}");
                RespuestaRest login = await cliente.AutenticarAsync("Monster", "Monster9");
                Check(login.IsExito && login.Token == "token-prueba", "Autenticación debe leer token y estado de éxito");
                Check(handler.Ultima.Metodo == HttpMethod.Post && handler.Ultima.Uri.AbsolutePath == "/api/ws-con-uni/autenticar", "Autenticación debe enviar POST a la ruta correcta");
                Check(handler.Ultima.Cuerpo.Contains("\"usuario\":\"Monster\"") && handler.Ultima.Cuerpo.Contains("\"clave\":\"Monster9\""), "Autenticación debe enviar los nombres JSON requeridos");
                Check(handler.Ultima.Authorization == null, "Autenticación no debe enviar Bearer");

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

                handler.Configurar(HttpStatusCode.OK, "{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"Conversion realizada\",\"resultado\":200,\"categoria\":\"LONGITUD\",\"unidadOrigen\":\"METRO\",\"unidadDestino\":\"CENTIMETRO\"}");
                RespuestaRest conversion = await cliente.ConvertirAsync("token-prueba", "LONGITUD", "METRO", "CENTIMETRO", 2);
                Check(conversion.Resultado == 200 && conversion.UnidadOrigen == "METRO", "Conversión debe deserializar resultado y unidades");
                Check(handler.Ultima.Metodo == HttpMethod.Post && handler.Ultima.Uri.AbsolutePath.EndsWith("/convertir"), "Conversión debe usar POST");
                Check(handler.Ultima.Cuerpo.Contains("\"categoria\":\"LONGITUD\"") && handler.Ultima.Cuerpo.Contains("\"valor\":2"), "Conversión debe enviar los campos JSON y el valor numérico");
                Check(handler.Ultima.Authorization == "Bearer token-prueba", "Conversión debe enviar Bearer");

                handler.Configurar(HttpStatusCode.OK, "{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"Clave actualizada\"}");
                RespuestaRest recuperacion = await cliente.RecuperarClaveAsync("Monster", "nueva-clave");
                Check(recuperacion.IsExito && handler.Ultima.Uri.AbsolutePath.EndsWith("/recuperar-clave"), "Recuperación debe invocar la ruta REST correspondiente");
                Check(handler.Ultima.Cuerpo.Contains("\"nuevaClave\":\"nueva-clave\"") && handler.Ultima.Authorization == null, "Recuperación debe enviar JSON sin token");

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
