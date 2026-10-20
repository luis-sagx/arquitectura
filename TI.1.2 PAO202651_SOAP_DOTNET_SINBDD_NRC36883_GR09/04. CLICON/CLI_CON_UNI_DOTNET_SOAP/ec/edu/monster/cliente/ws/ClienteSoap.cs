using System;
using System.Collections.Generic;
using System.IO;
using System.ServiceModel;
using ec.edu.monster.modelo;
using ec.edu.monster.ws;

namespace ec.edu.monster.cliente.ws
{
    /// <summary>Adaptador entre la aplicación de consola y el contrato WCF.</summary>
    internal sealed class ClienteSoap : IDisposable
    {
        private readonly Uri endpoint;
        private ChannelFactory<IWSConUni> fabrica;
        private IWSConUni puerto;

        public ClienteSoap(Uri endpoint)
        {
            this.endpoint = endpoint ?? throw new ArgumentNullException("endpoint");
        }

        public static Uri ValidarEndpoint(string valor)
        {
            Uri uri;
            if (!Uri.TryCreate(valor, UriKind.Absolute, out uri)
                || (uri.Scheme != Uri.UriSchemeHttp && uri.Scheme != Uri.UriSchemeHttps)
                || string.IsNullOrEmpty(uri.Host) || !string.IsNullOrEmpty(uri.Fragment))
                throw new ArgumentException("Indique una URL absoluta HTTP o HTTPS del servicio WCF.");

            if (string.Equals(uri.Query, "?wsdl", StringComparison.OrdinalIgnoreCase))
                uri = new UriBuilder(uri) { Query = string.Empty }.Uri;
            return uri;
        }

        public RespuestaSoap Autenticar(string usuario, string clave)
        {
            return Invocar(p => p.Autenticar(usuario, clave));
        }

        public RespuestaSoap RecuperarClave(string usuario, string nuevaClave)
        {
            return Invocar(p => p.RecuperarClave(usuario, nuevaClave));
        }

        public RespuestaSoap ObtenerCategorias(string token)
        {
            return Invocar(p => p.ObtenerCategorias(token));
        }

        public RespuestaSoap ObtenerUnidades(string token, string categoria)
        {
            return Invocar(p => p.ObtenerUnidades(token, categoria));
        }

        public RespuestaSoap Convertir(string token, string categoria, string origen, string destino, double valor)
        {
            return Invocar(p => p.Convertir(token, categoria, origen, destino, valor));
        }

        private RespuestaSoap Invocar(Func<IWSConUni, RespuestaWcf> operacion)
        {
            try
            {
                RespuestaWcf respuesta = operacion(Puerto());
                if (respuesta == null || (respuesta.Estado != "EXITO" && respuesta.Estado != "ERROR")
                    || string.IsNullOrWhiteSpace(respuesta.Codigo)
                    || string.IsNullOrWhiteSpace(respuesta.Mensaje))
                    throw new IOException("El servidor devolvió una respuesta incompleta.");

                if (respuesta.Resultado.HasValue
                    && (double.IsNaN(respuesta.Resultado.Value) || double.IsInfinity(respuesta.Resultado.Value)))
                    throw new IOException("El servidor devolvió un resultado numérico no válido.");
                if (ContieneNulos(respuesta.Categorias) || ContieneNulos(respuesta.Unidades))
                    throw new IOException("El servidor devolvió una lista incompleta.");

                return new RespuestaSoap(respuesta.Estado, respuesta.Codigo, respuesta.Mensaje,
                    respuesta.Token, respuesta.ExpiraEn, respuesta.Resultado,
                    respuesta.UnidadOrigen, respuesta.UnidadDestino,
                    respuesta.Categorias ?? new List<string>(), respuesta.Unidades ?? new List<string>());
            }
            catch (FaultException ex)
            {
                ReiniciarCanal();
                throw new IOException("El servidor devolvió un fallo SOAP: " + ex.Message, ex);
            }
            catch (TimeoutException ex)
            {
                ReiniciarCanal();
                throw new IOException("Se agotó el tiempo de espera al comunicarse con el servidor WCF.", ex);
            }
            catch (CommunicationException ex)
            {
                ReiniciarCanal();
                throw new IOException("No se pudo comunicar con el servidor. Revise el endpoint y que el servicio esté iniciado.", ex);
            }
        }

        private IWSConUni Puerto()
        {
            if (puerto == null)
            {
                var binding = new BasicHttpBinding
                {
                    OpenTimeout = TimeSpan.FromSeconds(10),
                    SendTimeout = TimeSpan.FromSeconds(30),
                    ReceiveTimeout = TimeSpan.FromSeconds(30),
                    CloseTimeout = TimeSpan.FromSeconds(5)
                };
                binding.Security.Mode = endpoint.Scheme == Uri.UriSchemeHttps
                    ? BasicHttpSecurityMode.Transport
                    : BasicHttpSecurityMode.None;
                fabrica = new ChannelFactory<IWSConUni>(binding, new EndpointAddress(endpoint));
                puerto = fabrica.CreateChannel();
            }
            return puerto;
        }

        private void ReiniciarCanal()
        {
            CerrarCanal();
            if (fabrica != null)
            {
                try { fabrica.Abort(); }
                catch (CommunicationException) { }
                fabrica = null;
            }
        }

        private void CerrarCanal()
        {
            var canal = puerto as IClientChannel;
            puerto = null;
            if (canal == null) return;
            try
            {
                if (canal.State == CommunicationState.Faulted) canal.Abort();
                else canal.Close();
            }
            catch (CommunicationException) { canal.Abort(); }
            catch (TimeoutException) { canal.Abort(); }
        }

        private static bool ContieneNulos(IList<string> valores)
        {
            return valores != null && valores.Contains(null);
        }

        public void Dispose()
        {
            CerrarCanal();
            if (fabrica == null) return;
            try
            {
                if (fabrica.State == CommunicationState.Faulted) fabrica.Abort();
                else fabrica.Close();
            }
            catch (CommunicationException) { fabrica.Abort(); }
            catch (TimeoutException) { fabrica.Abort(); }
            fabrica = null;
        }
    }
}
