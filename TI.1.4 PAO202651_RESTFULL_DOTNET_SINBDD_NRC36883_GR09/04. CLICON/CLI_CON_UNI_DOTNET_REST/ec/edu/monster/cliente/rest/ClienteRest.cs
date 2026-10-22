using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Runtime.Serialization;
using System.Runtime.Serialization.Json;
using System.Xml;
using ec.edu.monster.modelo;

namespace ec.edu.monster.cliente.rest
{
    /// <summary>Adaptador entre la aplicación de consola y la API REST (JSON sobre HTTP).</summary>
    internal sealed class ClienteRest : IDisposable
    {
        private readonly Uri api;

        public ClienteRest(Uri api)
        {
            if (api == null) throw new ArgumentNullException("api");
            this.api = api;
        }

        /// <summary>Valida la URL base de la API (por ejemplo http://localhost:5111/api/ws-con-uni).</summary>
        public static Uri ValidarEndpoint(string valor)
        {
            Uri uri;
            if (!Uri.TryCreate(valor == null ? null : valor.Trim(), UriKind.Absolute, out uri)
                || (uri.Scheme != Uri.UriSchemeHttp && uri.Scheme != Uri.UriSchemeHttps)
                || string.IsNullOrEmpty(uri.Host) || !string.IsNullOrEmpty(uri.Fragment) || !string.IsNullOrEmpty(uri.Query))
                throw new ArgumentException("Indique la URL absoluta HTTP o HTTPS de la API REST, terminada en /api/ws-con-uni.");
            return new Uri(uri.AbsoluteUri.TrimEnd('/') + "/");
        }

        public RespuestaRest Autenticar(string usuario, string clave)
        {
            return Enviar("POST", "autenticar", new LoginRequest { Usuario = usuario, Clave = clave }, null);
        }

        public RespuestaRest RecuperarClave(string usuario, string nuevaClave)
        {
            return Enviar("POST", "recuperar-clave", new RecoverRequest { Usuario = usuario, NuevaClave = nuevaClave }, null);
        }

        public RespuestaRest ObtenerCategorias(string token)
        {
            return Enviar("GET", "categorias", null, token);
        }

        public RespuestaRest ObtenerUnidades(string token, string categoria)
        {
            return Enviar("GET", "unidades?categoria=" + Uri.EscapeDataString(categoria ?? string.Empty), null, token);
        }

        public RespuestaRest Convertir(string token, string categoria, string origen, string destino, double valor)
        {
            return Enviar("POST", "convertir", new ConversionRequest
            {
                Categoria = categoria, UnidadOrigen = origen, UnidadDestino = destino, Valor = valor
            }, token);
        }

        private RespuestaRest Enviar(string metodo, string ruta, object cuerpo, string token)
        {
            var peticion = (HttpWebRequest)WebRequest.Create(new Uri(api, ruta));
            peticion.Method = metodo;
            peticion.Accept = "application/json";
            peticion.Timeout = 30000;
            peticion.ReadWriteTimeout = 30000;
            if (token != null) peticion.Headers[HttpRequestHeader.Authorization] = "Bearer " + token;

            HttpWebResponse respuestaHttp;
            try
            {
                if (cuerpo != null)
                {
                    peticion.ContentType = "application/json; charset=utf-8";
                    using (Stream salida = peticion.GetRequestStream())
                        new DataContractJsonSerializer(cuerpo.GetType()).WriteObject(salida, cuerpo);
                }
                respuestaHttp = (HttpWebResponse)peticion.GetResponse();
            }
            catch (WebException ex) when (ex.Response is HttpWebResponse)
            {
                // 400 y 401 también traen la respuesta de negocio en JSON.
                respuestaHttp = (HttpWebResponse)ex.Response;
            }
            catch (WebException ex)
            {
                if (ex.Status == WebExceptionStatus.Timeout)
                    throw new IOException("Se agotó el tiempo de espera al comunicarse con la API REST.", ex);
                throw new IOException("No se pudo comunicar con el servidor. Revise la URL de la API y que el servicio REST esté iniciado.", ex);
            }

            using (respuestaHttp)
            {
                int estadoHttp = (int)respuestaHttp.StatusCode;
                RespuestaJson json;
                try
                {
                    using (Stream entrada = respuestaHttp.GetResponseStream())
                        json = (RespuestaJson)new DataContractJsonSerializer(typeof(RespuestaJson)).ReadObject(entrada);
                }
                catch (Exception ex) when (ex is SerializationException || ex is XmlException || ex is InvalidCastException)
                {
                    throw new IOException(MensajeHttp(estadoHttp), ex);
                }
                return Normalizar(json);
            }
        }

        private static RespuestaRest Normalizar(RespuestaJson respuesta)
        {
            if (respuesta == null || (respuesta.Estado != "EXITO" && respuesta.Estado != "ERROR")
                || string.IsNullOrWhiteSpace(respuesta.Codigo)
                || string.IsNullOrWhiteSpace(respuesta.Mensaje))
                throw new IOException("El servidor devolvió una respuesta incompleta.");

            if (respuesta.Resultado.HasValue
                && (double.IsNaN(respuesta.Resultado.Value) || double.IsInfinity(respuesta.Resultado.Value)))
                throw new IOException("El servidor devolvió un resultado numérico no válido.");
            if (ContieneNulos(respuesta.Categorias) || ContieneNulos(respuesta.Unidades))
                throw new IOException("El servidor devolvió una lista incompleta.");

            return new RespuestaRest(respuesta.Estado, respuesta.Codigo, respuesta.Mensaje,
                respuesta.Token, respuesta.ExpiraEn, respuesta.Resultado,
                respuesta.UnidadOrigen, respuesta.UnidadDestino,
                respuesta.Categorias ?? new List<string>(), respuesta.Unidades ?? new List<string>());
        }

        private static string MensajeHttp(int estadoHttp)
        {
            if (estadoHttp == 404)
                return "No se encontró la API en esa URL (HTTP 404). Revise que termine en /api/ws-con-uni.";
            if (estadoHttp >= 400)
                return "El servidor respondió con el estado HTTP " + estadoHttp + " sin una respuesta JSON válida.";
            return "El servidor no respondió con JSON. Revise que la URL apunte a la API REST.";
        }

        private static bool ContieneNulos(IList<string> valores)
        {
            return valores != null && valores.Contains(null);
        }

        public void Dispose()
        {
            // HttpWebRequest no mantiene recursos abiertos entre llamadas.
        }
    }
}
