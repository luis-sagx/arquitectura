using System;
using System.IO;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Runtime.Serialization.Json;
using System.Text;
using System.Threading.Tasks;
using System.Configuration;
using ec.edu.monster.modelo;

namespace ec.edu.monster.servicio
{
    public sealed class ClienteRest : IClienteRest, IDisposable
    {
        private const string EndpointPredeterminado = "http://localhost:5111/api/ws-con-uni/";
        private readonly HttpClient http;
        private readonly Uri endpoint;
        private readonly string errorConfiguracion;

        public ClienteRest(Uri endpoint) : this(endpoint, new HttpClientHandler())
        {
        }

        public ClienteRest(Uri endpoint, HttpMessageHandler handler)
        {
            this.endpoint = ValidarEndpoint(endpoint);
            if (handler == null) throw new ArgumentNullException("handler");
            http = new HttpClient(handler, true);
            http.Timeout = TimeSpan.FromSeconds(20);
        }

        private ClienteRest(string errorConfiguracion)
        {
            this.errorConfiguracion = errorConfiguracion;
            endpoint = new Uri(EndpointPredeterminado, UriKind.Absolute);
            http = new HttpClient { Timeout = TimeSpan.FromSeconds(20) };
        }

        public string ErrorConfiguracion { get { return errorConfiguracion; } }

        public static ClienteRest DesdeConfiguracion()
        {
            string valor = ConfigurationManager.AppSettings["conversor.endpoint"];
            if (String.IsNullOrWhiteSpace(valor)) valor = EndpointPredeterminado;
            Uri uri;
            if (!Uri.TryCreate(valor.Trim(), UriKind.Absolute, out uri) ||
                (uri.Scheme != Uri.UriSchemeHttp && uri.Scheme != Uri.UriSchemeHttps))
                return new ClienteRest("La dirección del servicio REST no es válida. Revisa la opción conversor.endpoint en App.config.");

            try { return new ClienteRest(uri); }
            catch (ArgumentException error)
            {
                return new ClienteRest("La dirección del servicio REST no es válida: " + error.Message);
            }
        }

        public Task<RespuestaRest> AutenticarAsync(string usuario, string clave)
        {
            return EnviarJsonAsync("autenticar", new SolicitudAutenticacionRest { Usuario = usuario, Clave = clave }, null);
        }

        public Task<RespuestaRest> RecuperarClaveAsync(string usuario, string nuevaClave)
        {
            return EnviarJsonAsync("recuperar-clave", new SolicitudRecuperacionRest { Usuario = usuario, NuevaClave = nuevaClave }, null);
        }

        public Task<RespuestaRest> ObtenerCategoriasAsync(string token)
        {
            return EnviarAsync(new HttpRequestMessage(HttpMethod.Get, CrearUri("categorias")), token);
        }

        public Task<RespuestaRest> ObtenerUnidadesAsync(string token, string categoria)
        {
            string consulta = "unidades?categoria=" + Uri.EscapeDataString(categoria ?? String.Empty);
            return EnviarAsync(new HttpRequestMessage(HttpMethod.Get, CrearUri(consulta)), token);
        }

        public Task<RespuestaRest> ConvertirAsync(string token, string categoria, string unidadOrigen, string unidadDestino, double valor)
        {
            if (Double.IsNaN(valor) || Double.IsInfinity(valor))
                throw new ArgumentOutOfRangeException("valor", "El valor debe ser un número finito.");
            return EnviarJsonAsync("convertir", new SolicitudConversionRest
            {
                Categoria = categoria,
                UnidadOrigen = unidadOrigen,
                UnidadDestino = unidadDestino,
                Valor = valor
            }, token);
        }

        private Task<RespuestaRest> EnviarJsonAsync(string ruta, object solicitud, string token)
        {
            var peticion = new HttpRequestMessage(HttpMethod.Post, CrearUri(ruta));
            byte[] contenido;
            try { contenido = Serializar(solicitud); }
            catch (Exception error)
            {
                peticion.Dispose();
                throw new IOException("No se pudo preparar la solicitud para el servicio REST.", error);
            }
            peticion.Content = new ByteArrayContent(contenido);
            peticion.Content.Headers.ContentType = new MediaTypeHeaderValue("application/json") { CharSet = "utf-8" };
            return EnviarAsync(peticion, token);
        }

        private async Task<RespuestaRest> EnviarAsync(HttpRequestMessage peticion, string token)
        {
            if (!String.IsNullOrEmpty(errorConfiguracion))
            {
                peticion.Dispose();
                throw new IOException(errorConfiguracion);
            }
            if (!String.IsNullOrWhiteSpace(token))
                peticion.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);

            try
            {
                using (peticion)
                using (HttpResponseMessage respuestaHttp = await http.SendAsync(peticion).ConfigureAwait(false))
                {
                    string cuerpo = await respuestaHttp.Content.ReadAsStringAsync().ConfigureAwait(false);
                    RespuestaRest respuesta = Deserializar(cuerpo, respuestaHttp.StatusCode);
                    if (respuesta.Resultado.HasValue &&
                        (Double.IsNaN(respuesta.Resultado.Value) || Double.IsInfinity(respuesta.Resultado.Value)))
                        throw new IOException("El servicio devolvió un resultado numérico no finito.");
                    return respuesta;
                }
            }
            catch (IOException) { throw; }
            catch (HttpRequestException error)
            {
                throw new IOException("No se pudo conectar con el servicio REST. Verifica que el servidor esté iniciado y revisa la dirección configurada.", error);
            }
            catch (TaskCanceledException error)
            {
                throw new IOException("La solicitud al servicio REST tardó demasiado y se canceló.", error);
            }
            catch (Exception error)
            {
                throw new IOException("No se pudo procesar la respuesta del servicio REST.", error);
            }
        }

        private static RespuestaRest Deserializar(string cuerpo, System.Net.HttpStatusCode estadoHttp)
        {
            if (String.IsNullOrWhiteSpace(cuerpo))
                throw new IOException("El servicio REST devolvió una respuesta vacía (HTTP " + (int)estadoHttp + ").");
            try
            {
                var serializer = new DataContractJsonSerializer(typeof(RespuestaRest));
                using (var stream = new MemoryStream(Encoding.UTF8.GetBytes(cuerpo)))
                {
                    var resultado = serializer.ReadObject(stream) as RespuestaRest;
                    if (resultado == null || String.IsNullOrWhiteSpace(resultado.Codigo) ||
                        String.IsNullOrWhiteSpace(resultado.Estado) || String.IsNullOrWhiteSpace(resultado.Mensaje))
                        throw new IOException("El servicio REST devolvió una respuesta incompleta.");
                    return resultado;
                }
            }
            catch (IOException) { throw; }
            catch (Exception error)
            {
                throw new IOException("El servicio REST devolvió una respuesta JSON inválida (HTTP " + (int)estadoHttp + ").", error);
            }
        }

        private static byte[] Serializar(object solicitud)
        {
            var serializer = new DataContractJsonSerializer(solicitud.GetType());
            using (var stream = new MemoryStream())
            {
                serializer.WriteObject(stream, solicitud);
                return stream.ToArray();
            }
        }

        private Uri CrearUri(string ruta)
        {
            return new Uri(endpoint, ruta);
        }

        private static Uri ValidarEndpoint(Uri valor)
        {
            if (valor == null || !valor.IsAbsoluteUri ||
                (valor.Scheme != Uri.UriSchemeHttp && valor.Scheme != Uri.UriSchemeHttps))
                throw new ArgumentException("El endpoint debe ser una dirección absoluta HTTP o HTTPS.", "endpoint");
            string normalizado = valor.AbsoluteUri.EndsWith("/", StringComparison.Ordinal)
                ? valor.AbsoluteUri : valor.AbsoluteUri + "/";
            return new Uri(normalizado, UriKind.Absolute);
        }

        public void Dispose()
        {
            http.Dispose();
        }
    }
}
