using System;
using System.Collections.Generic;
using System.IO;
using System.ServiceModel;
using System.Threading.Tasks;
using ec.edu.monster.modelo;
using ec.edu.monster.ws;

namespace ec.edu.monster.servicio
{
    /// <summary>
    /// Adaptador entre los controladores web y el contrato WCF.
    /// Usa una sola ChannelFactory (segura entre hilos) y un canal por llamada.
    /// </summary>
    public sealed class ClienteSoap : IDisposable
    {
        private readonly ChannelFactory<IWSConUni> fabrica;

        public ClienteSoap(string? direccion)
        {
            Endpoint = ValidarEndpoint(direccion);
            var binding = new BasicHttpBinding
            {
                OpenTimeout = TimeSpan.FromSeconds(10),
                SendTimeout = TimeSpan.FromSeconds(30),
                ReceiveTimeout = TimeSpan.FromSeconds(30),
                CloseTimeout = TimeSpan.FromSeconds(5)
            };
            binding.Security.Mode = Endpoint.Scheme == Uri.UriSchemeHttps
                ? BasicHttpSecurityMode.Transport
                : BasicHttpSecurityMode.None;
            fabrica = new ChannelFactory<IWSConUni>(binding, new EndpointAddress(Endpoint));
        }

        public Uri Endpoint { get; }

        /// <summary>Valida la dirección y acepta también la URL terminada en ?wsdl.</summary>
        public static Uri ValidarEndpoint(string? valor)
        {
            Uri? uri;
            if (String.IsNullOrWhiteSpace(valor) || !Uri.TryCreate(valor.Trim(), UriKind.Absolute, out uri)
                || (uri.Scheme != Uri.UriSchemeHttp && uri.Scheme != Uri.UriSchemeHttps)
                || String.IsNullOrEmpty(uri.Host) || !String.IsNullOrEmpty(uri.Fragment))
                throw new ArgumentException("Configure en appsettings.json (Conversor:EndpointSoap) una URL HTTP o HTTPS del servicio WCF.");
            if (String.Equals(uri.Query, "?wsdl", StringComparison.OrdinalIgnoreCase))
                uri = new UriBuilder(uri) { Query = String.Empty }.Uri;
            return uri;
        }

        public Task<RespuestaSoap> AutenticarAsync(string usuario, string clave)
        {
            return InvocarAsync(p => p.AutenticarAsync(usuario, clave));
        }

        public Task<RespuestaSoap> RecuperarClaveAsync(string usuario, string nuevaClave)
        {
            return InvocarAsync(p => p.RecuperarClaveAsync(usuario, nuevaClave));
        }

        public Task<RespuestaSoap> ObtenerCategoriasAsync(string token)
        {
            return InvocarAsync(p => p.ObtenerCategoriasAsync(token));
        }

        public Task<RespuestaSoap> ObtenerUnidadesAsync(string token, string categoria)
        {
            return InvocarAsync(p => p.ObtenerUnidadesAsync(token, categoria));
        }

        public Task<RespuestaSoap> ConvertirAsync(string token, string categoria, string origen,
            string destino, double valor)
        {
            return InvocarAsync(p => p.ConvertirAsync(token, categoria, origen, destino, valor));
        }

        private async Task<RespuestaSoap> InvocarAsync(Func<IWSConUni, Task<RespuestaWcf>> operacion)
        {
            IWSConUni canal = fabrica.CreateChannel();
            try
            {
                RespuestaWcf respuesta = await operacion(canal);
                Cerrar(canal);
                return Normalizar(respuesta);
            }
            catch (FaultException ex)
            {
                Abortar(canal);
                throw new IOException("El servidor devolvió un fallo SOAP: " + ex.Message, ex);
            }
            catch (TimeoutException ex)
            {
                Abortar(canal);
                throw new IOException("Se agotó el tiempo de espera al comunicarse con el servidor WCF.", ex);
            }
            catch (CommunicationException ex)
            {
                Abortar(canal);
                throw new IOException("No se pudo comunicar con el servidor. Revise el endpoint y que el servicio WCF esté iniciado.", ex);
            }
        }

        private static RespuestaSoap Normalizar(RespuestaWcf? respuesta)
        {
            if (respuesta == null || (respuesta.Estado != "EXITO" && respuesta.Estado != "ERROR")
                || String.IsNullOrWhiteSpace(respuesta.Codigo) || String.IsNullOrWhiteSpace(respuesta.Mensaje))
                throw new IOException("El servidor devolvió una respuesta incompleta.");
            if (respuesta.Resultado.HasValue
                && (Double.IsNaN(respuesta.Resultado.Value) || Double.IsInfinity(respuesta.Resultado.Value)))
                throw new IOException("El servidor devolvió un resultado numérico no válido.");
            if (ContieneVacios(respuesta.Categorias) || ContieneVacios(respuesta.Unidades))
                throw new IOException("El servidor devolvió una lista incompleta.");

            return new RespuestaSoap(respuesta.Estado, respuesta.Codigo, respuesta.Mensaje,
                respuesta.Token, respuesta.ExpiraEn, respuesta.Resultado,
                respuesta.UnidadOrigen, respuesta.UnidadDestino, respuesta.Categorias, respuesta.Unidades);
        }

        private static bool ContieneVacios(List<string>? valores)
        {
            return valores != null && valores.Exists(String.IsNullOrWhiteSpace);
        }

        private static void Cerrar(IWSConUni canal)
        {
            var cliente = (IClientChannel)canal;
            try
            {
                if (cliente.State == CommunicationState.Faulted) cliente.Abort();
                else cliente.Close();
            }
            catch (CommunicationException) { cliente.Abort(); }
            catch (TimeoutException) { cliente.Abort(); }
        }

        private static void Abortar(IWSConUni canal)
        {
            ((IClientChannel)canal).Abort();
        }

        public void Dispose()
        {
            try
            {
                if (fabrica.State == CommunicationState.Faulted) fabrica.Abort();
                else fabrica.Close();
            }
            catch (CommunicationException) { fabrica.Abort(); }
            catch (TimeoutException) { fabrica.Abort(); }
        }
    }
}
