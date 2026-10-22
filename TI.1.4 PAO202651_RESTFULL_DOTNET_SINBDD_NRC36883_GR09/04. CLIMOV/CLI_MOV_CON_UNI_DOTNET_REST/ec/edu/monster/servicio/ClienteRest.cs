using System;
using System.Collections.Generic;
using System.IO;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;
using ec.edu.monster.modelo;

namespace ec.edu.monster.servicio
{
    /// <summary>
    /// Adaptador de la API REST (JSON sobre HTTP con token Bearer) para Android e iOS.
    /// Así las vistas y controladores no conocen el JSON ni las rutas HTTP.
    /// </summary>
    public sealed class ClienteRest
    {
        private static readonly HttpClient Http = new HttpClient { Timeout = TimeSpan.FromSeconds(30) };

        public ClienteRest(string direccion)
        {
            Endpoint = NormalizarEndpoint(direccion);
        }

        public Uri Endpoint { get; }

        /// <summary>Valida la URL base de la API (por ejemplo http://10.0.2.2:5111/api/ws-con-uni).</summary>
        public static Uri NormalizarEndpoint(string? direccion)
        {
            if (String.IsNullOrWhiteSpace(direccion))
                throw new ArgumentException("Escribe la URL de la API REST.");
            Uri? uri;
            if (!Uri.TryCreate(direccion.Trim(), UriKind.Absolute, out uri)
                || (uri.Scheme != Uri.UriSchemeHttp && uri.Scheme != Uri.UriSchemeHttps)
                || String.IsNullOrEmpty(uri.Host) || !String.IsNullOrEmpty(uri.Fragment) || !String.IsNullOrEmpty(uri.Query))
                throw new ArgumentException("La URL debe empezar con http:// o https://, incluir el servidor y terminar en /api/ws-con-uni.");
            return new Uri(uri.AbsoluteUri.TrimEnd('/') + "/");
        }

        public Task<RespuestaRest> AutenticarAsync(string usuario, string clave)
        {
            return EnviarAsync(HttpMethod.Post, "autenticar", new { usuario, clave }, null);
        }

        public Task<RespuestaRest> RecuperarClaveAsync(string usuario, string nuevaClave)
        {
            return EnviarAsync(HttpMethod.Post, "recuperar-clave", new { usuario, nuevaClave }, null);
        }

        public Task<RespuestaRest> ObtenerCategoriasAsync(string token)
        {
            return EnviarAsync(HttpMethod.Get, "categorias", null, token);
        }

        public Task<RespuestaRest> ObtenerUnidadesAsync(string token, string categoria)
        {
            return EnviarAsync(HttpMethod.Get, "unidades?categoria=" + Uri.EscapeDataString(categoria), null, token);
        }

        public Task<RespuestaRest> ConvertirAsync(string token, string categoria, string origen,
            string destino, double valor)
        {
            return EnviarAsync(HttpMethod.Post, "convertir",
                new { categoria, unidadOrigen = origen, unidadDestino = destino, valor }, token);
        }

        private async Task<RespuestaRest> EnviarAsync(HttpMethod metodo, string ruta, object? cuerpo, string? token)
        {
            using var peticion = new HttpRequestMessage(metodo, new Uri(Endpoint, ruta));
            peticion.Headers.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
            if (token != null) peticion.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);
            if (cuerpo != null)
                peticion.Content = new StringContent(JsonSerializer.Serialize(cuerpo), Encoding.UTF8, "application/json");

            int estadoHttp;
            string texto;
            try
            {
                using HttpResponseMessage respuestaHttp = await Http.SendAsync(peticion).ConfigureAwait(false);
                estadoHttp = (int)respuestaHttp.StatusCode;
                texto = await respuestaHttp.Content.ReadAsStringAsync().ConfigureAwait(false);
            }
            catch (TaskCanceledException ex)
            {
                throw new IOException("El servidor tardó demasiado en responder.", ex);
            }
            catch (HttpRequestException ex)
            {
                throw new IOException("No se pudo conectar con el servidor. Revise la URL, la red y que la API REST esté iniciada.", ex);
            }

            try
            {
                using JsonDocument documento = JsonDocument.Parse(texto);
                return Normalizar(documento.RootElement);
            }
            catch (JsonException ex)
            {
                throw new IOException(MensajeHttp(estadoHttp), ex);
            }
        }

        private static RespuestaRest Normalizar(JsonElement json)
        {
            if (json.ValueKind != JsonValueKind.Object)
                throw new IOException("El servidor devolvió una respuesta incompleta.");
            string? estado = Texto(json, "estado");
            string? codigo = Texto(json, "codigo");
            string? mensaje = Texto(json, "mensaje");
            if ((estado != "EXITO" && estado != "ERROR")
                || String.IsNullOrWhiteSpace(codigo) || String.IsNullOrWhiteSpace(mensaje))
                throw new IOException("El servidor devolvió una respuesta incompleta.");

            double? resultado = null;
            if (json.TryGetProperty("resultado", out JsonElement valor) && valor.ValueKind == JsonValueKind.Number)
            {
                resultado = valor.GetDouble();
                if (Double.IsNaN(resultado.Value) || Double.IsInfinity(resultado.Value))
                    throw new IOException("El servidor devolvió un resultado numérico no válido.");
            }

            return new RespuestaRest(estado, codigo, mensaje, Texto(json, "token"), Texto(json, "expiraEn"),
                resultado, Texto(json, "unidadOrigen"), Texto(json, "unidadDestino"),
                Lista(json, "categorias"), Lista(json, "unidades"));
        }

        private static string? Texto(JsonElement json, string nombre)
        {
            return json.TryGetProperty(nombre, out JsonElement valor) && valor.ValueKind == JsonValueKind.String
                ? valor.GetString() : null;
        }

        private static List<string> Lista(JsonElement json, string nombre)
        {
            var valores = new List<string>();
            if (!json.TryGetProperty(nombre, out JsonElement lista) || lista.ValueKind != JsonValueKind.Array)
                return valores;
            foreach (JsonElement elemento in lista.EnumerateArray())
            {
                string? texto = elemento.ValueKind == JsonValueKind.String ? elemento.GetString() : null;
                if (String.IsNullOrWhiteSpace(texto))
                    throw new IOException("El servidor devolvió una lista incompleta.");
                valores.Add(texto);
            }
            return valores;
        }

        private static string MensajeHttp(int estadoHttp)
        {
            if (estadoHttp == 404)
                return "No se encontró la API en esa URL (HTTP 404). Revise que termine en /api/ws-con-uni.";
            if (estadoHttp >= 400)
                return "El servidor respondió con el estado HTTP " + estadoHttp + " sin una respuesta JSON válida.";
            return "El servidor no respondió con JSON. Revise que la URL apunte a la API REST.";
        }
    }
}
