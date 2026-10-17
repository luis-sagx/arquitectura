using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using System.Xml;
using System.Xml.Linq;
using ec.edu.monster.modelo;

namespace ec.edu.monster.servicio
{
    /// <summary>
    /// Adaptador SOAP 1.1 del servicio WCF WSConUni.svc (basicHttpBinding).
    /// Arma y lee el sobre con HttpClient y LINQ to XML para funcionar igual en Android e iOS,
    /// donde los proxys dinámicos de WCF no están disponibles; así las vistas y controladores no conocen el XML.
    /// WCF elige la operación por el encabezado SOAPAction, espera los parámetros calificados con el
    /// namespace del contrato y devuelve el resultado en el elemento &lt;operacion&gt;Result.
    /// </summary>
    public sealed class ClienteSoap
    {
        private const string Contrato = "http://ws.controladores.monster.edu.ec/";
        private static readonly XNamespace Ns = Contrato;
        private static readonly XNamespace SoapEnv = "http://schemas.xmlsoap.org/soap/envelope/";
        private static readonly HttpClient Http = new HttpClient { Timeout = TimeSpan.FromSeconds(30) };

        public ClienteSoap(string direccion)
        {
            Endpoint = NormalizarEndpoint(direccion);
        }

        public Uri Endpoint { get; }

        /// <summary>Valida la dirección y acepta también la URL terminada en ?wsdl.</summary>
        public static Uri NormalizarEndpoint(string? direccion)
        {
            if (String.IsNullOrWhiteSpace(direccion))
                throw new ArgumentException("Escribe la URL del servicio WCF.");
            Uri? uri;
            if (!Uri.TryCreate(direccion.Trim(), UriKind.Absolute, out uri)
                || (uri.Scheme != Uri.UriSchemeHttp && uri.Scheme != Uri.UriSchemeHttps)
                || String.IsNullOrEmpty(uri.Host) || !String.IsNullOrEmpty(uri.Fragment))
                throw new ArgumentException("La URL debe empezar con http:// o https:// e incluir el servidor.");
            if (String.Equals(uri.Query, "?wsdl", StringComparison.OrdinalIgnoreCase))
                uri = new UriBuilder(uri) { Query = String.Empty }.Uri;
            return uri;
        }

        public Task<RespuestaSoap> AutenticarAsync(string usuario, string clave)
        {
            return InvocarAsync("autenticar", ("usuario", usuario), ("clave", clave));
        }

        public Task<RespuestaSoap> RecuperarClaveAsync(string usuario, string nuevaClave)
        {
            return InvocarAsync("recuperarClave", ("usuario", usuario), ("nuevaClave", nuevaClave));
        }

        public Task<RespuestaSoap> ObtenerCategoriasAsync(string token)
        {
            return InvocarAsync("obtenerCategorias", ("token", token));
        }

        public Task<RespuestaSoap> ObtenerUnidadesAsync(string token, string categoria)
        {
            return InvocarAsync("obtenerUnidades", ("token", token), ("categoria", categoria));
        }

        public Task<RespuestaSoap> ConvertirAsync(string token, string categoria, string origen,
            string destino, double valor)
        {
            return InvocarAsync("convertir", ("token", token), ("categoria", categoria),
                ("unidadOrigen", origen), ("unidadDestino", destino), ("valor", XmlConvert.ToString(valor)));
        }

        private async Task<RespuestaSoap> InvocarAsync(string operacion, params (string Nombre, string? Valor)[] parametros)
        {
            var sobre = new XDocument(new XDeclaration("1.0", "utf-8", null),
                new XElement(SoapEnv + "Envelope", new XAttribute(XNamespace.Xmlns + "s", SoapEnv),
                    new XElement(SoapEnv + "Body",
                        new XElement(Ns + operacion, new XAttribute(XNamespace.Xmlns + "ns", Ns),
                            parametros.Where(p => p.Valor != null)
                                .Select(p => new XElement(Ns + p.Nombre, p.Valor))))));

            using var peticion = new HttpRequestMessage(HttpMethod.Post, Endpoint)
            {
                Content = new StringContent(sobre.Declaration + sobre.ToString(SaveOptions.DisableFormatting),
                    Encoding.UTF8, "text/xml")
            };
            peticion.Headers.TryAddWithoutValidation("SOAPAction", "\"" + Contrato + "WSConUni/" + operacion + "\"");
            peticion.Headers.TryAddWithoutValidation("Accept", "text/xml");

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
                throw new IOException("No se pudo conectar con el servidor. Revise la URL, la red y que el servicio WCF esté iniciado.", ex);
            }

            XDocument documento;
            try
            {
                documento = Parsear(texto);
            }
            catch (XmlException ex)
            {
                throw new IOException(estadoHttp >= 400 ? MensajeHttp(estadoHttp)
                    : "El servidor no respondió con un mensaje SOAP. Revise la URL.", ex);
            }

            XElement? fallo = documento.Descendants(SoapEnv + "Fault").FirstOrDefault();
            if (fallo != null)
            {
                string? detalle = fallo.Element("faultstring")?.Value;
                throw new IOException("El servidor devolvió un fallo SOAP: " + (detalle ?? "sin detalle"));
            }
            if (estadoHttp >= 400) throw new IOException(MensajeHttp(estadoHttp));

            XElement? retorno = documento.Descendants(Ns + (operacion + "Response")).FirstOrDefault()
                ?.Element(Ns + (operacion + "Result"));
            if (retorno == null)
                throw new IOException("El servidor devolvió una respuesta que no corresponde a " + operacion + ".");
            return LeerRespuesta(retorno);
        }

        private static RespuestaSoap LeerRespuesta(XElement retorno)
        {
            string? estado = Texto(retorno, "estado");
            string? codigo = Texto(retorno, "codigo");
            string? mensaje = Texto(retorno, "mensaje");
            if (!(String.Equals(estado, "EXITO", StringComparison.OrdinalIgnoreCase)
                    || String.Equals(estado, "ERROR", StringComparison.OrdinalIgnoreCase))
                || String.IsNullOrWhiteSpace(codigo) || String.IsNullOrWhiteSpace(mensaje))
                throw new IOException("El servidor devolvió una respuesta incompleta.");

            // WCF envía <resultado xsi:nil="true"/> cuando no hay valor.
            double? resultado = null;
            string? textoResultado = Texto(retorno, "resultado");
            if (!String.IsNullOrWhiteSpace(textoResultado))
            {
                try
                {
                    resultado = XmlConvert.ToDouble(textoResultado.Trim());
                }
                catch (FormatException ex)
                {
                    throw new IOException("El servidor devolvió un resultado no numérico.", ex);
                }
                if (Double.IsNaN(resultado.Value) || Double.IsInfinity(resultado.Value))
                    throw new IOException("El servidor devolvió un resultado no finito.");
            }

            return new RespuestaSoap(estado, codigo, mensaje, Texto(retorno, "token"),
                Texto(retorno, "expiraEn"), resultado, Texto(retorno, "unidadOrigen"),
                Texto(retorno, "unidadDestino"), Lista(retorno, "categorias"), Lista(retorno, "unidades"));
        }

        private static List<string> Lista(XElement padre, string nombre)
        {
            var valores = new List<string>();
            foreach (XElement elemento in padre.Elements(Ns + nombre))
            {
                if (String.IsNullOrWhiteSpace(elemento.Value))
                    throw new IOException("El servidor devolvió una lista incompleta.");
                valores.Add(elemento.Value);
            }
            return valores;
        }

        private static string? Texto(XElement padre, string nombre)
        {
            return padre.Element(Ns + nombre)?.Value;
        }

        private static XDocument Parsear(string texto)
        {
            // Evita DTD y entidades externas en la respuesta (XXE).
            var opciones = new XmlReaderSettings { DtdProcessing = DtdProcessing.Prohibit, XmlResolver = null };
            using var lector = XmlReader.Create(new StringReader(texto), opciones);
            return XDocument.Load(lector);
        }

        private static string MensajeHttp(int estadoHttp)
        {
            switch (estadoHttp)
            {
                case 404:
                    return "No se encontró el servicio en esa URL (HTTP 404). Revise que termine en /WSConUni.svc.";
                case 400:
                    return "El servidor rechazó la petición (HTTP 400). Si usa IIS Express desde otro equipo, revise el nombre de host.";
                default:
                    return "El servidor respondió con el estado HTTP " + estadoHttp + ".";
            }
        }
    }
}
