using System;
using System.Configuration;
using System.IO;
using System.Linq;
using System.Net;
using System.ServiceModel;
using System.ServiceModel.Channels;
using ec.edu.monster.clientesws;
using ec.edu.monster.modelo;

namespace ec.edu.monster.servicio
{
    public sealed class ClienteSoap : IClienteSoap
    {
        private readonly Uri endpoint;
        private readonly string errorConfiguracion;

        public ClienteSoap(Uri endpoint)
        {
            this.endpoint = Validar(endpoint);
        }

        private ClienteSoap(string endpointConfigurado, bool desdeConfiguracion)
        {
            Uri direccion;
            if (!Uri.TryCreate(endpointConfigurado, UriKind.Absolute, out direccion))
            {
                errorConfiguracion = "Configure una URL absoluta HTTP o HTTPS en App.config (conversor.endpoint).";
                return;
            }

            try
            {
                endpoint = Validar(direccion);
            }
            catch (ArgumentException error)
            {
                errorConfiguracion = error.Message;
            }
        }

        public static ClienteSoap DesdeConfiguracion()
        {
            return new ClienteSoap(ConfigurationManager.AppSettings["conversor.endpoint"], true);
        }

        public RespuestaSoap Autenticar(string usuario, string clave)
        {
            return Invocar(proxy => proxy.autenticar(usuario, clave));
        }

        public RespuestaSoap RecuperarClave(string usuario, string nuevaClave)
        {
            return Invocar(proxy => proxy.recuperarClave(usuario, nuevaClave));
        }

        public RespuestaSoap ObtenerCategorias(string token)
        {
            return Invocar(proxy => proxy.obtenerCategorias(token));
        }

        public RespuestaSoap ObtenerUnidades(string token, string categoria)
        {
            return Invocar(proxy => proxy.obtenerUnidades(token, categoria));
        }

        public RespuestaSoap Convertir(string token, string categoria, string unidadOrigen,
            string unidadDestino, double valor)
        {
            return Invocar(proxy => proxy.convertir(token, categoria, unidadOrigen, unidadDestino, valor));
        }

        private RespuestaSoap Invocar(Func<WSConUniClient, Respuesta> operacion)
        {
            WSConUniClient proxy = CrearProxy();
            try
            {
                Respuesta respuesta = operacion(proxy);
                RespuestaSoap normalizada = Normalizar(respuesta);
                proxy.Close();
                return normalizada;
            }
            catch (FaultException error)
            {
                proxy.Abort();
                throw new IOException("El servidor devolvió un fallo SOAP: " + error.Message, error);
            }
            catch (TimeoutException error)
            {
                proxy.Abort();
                throw new IOException("Se agotó el tiempo de espera del servicio SOAP.", error);
            }
            catch (CommunicationException error)
            {
                proxy.Abort();
                throw new IOException("No se pudo comunicar con el servidor. Revise la URL, la red y el despliegue.", error);
            }
            catch
            {
                proxy.Abort();
                throw;
            }
        }

        private WSConUniClient CrearProxy()
        {
            if (!String.IsNullOrEmpty(errorConfiguracion))
                throw new IOException(errorConfiguracion);

            BasicHttpBinding binding = new BasicHttpBinding(
                endpoint.Scheme.Equals(Uri.UriSchemeHttps, StringComparison.OrdinalIgnoreCase)
                    ? BasicHttpSecurityMode.Transport : BasicHttpSecurityMode.None);
            binding.OpenTimeout = TimeSpan.FromSeconds(10);
            binding.SendTimeout = TimeSpan.FromSeconds(30);
            binding.ReceiveTimeout = TimeSpan.FromSeconds(30);
            binding.CloseTimeout = TimeSpan.FromSeconds(5);
            binding.MaxReceivedMessageSize = 1024 * 1024;
            return new WSConUniClient(binding, new EndpointAddress(endpoint));
        }

        private static RespuestaSoap Normalizar(Respuesta respuesta)
        {
            if (respuesta == null ||
                (!String.Equals(respuesta.estado, "EXITO", StringComparison.OrdinalIgnoreCase) &&
                 !String.Equals(respuesta.estado, "ERROR", StringComparison.OrdinalIgnoreCase)) ||
                String.IsNullOrWhiteSpace(respuesta.codigo) || String.IsNullOrWhiteSpace(respuesta.mensaje))
                throw new IOException("El servidor devolvió una respuesta incompleta.");

            if (respuesta.resultado.HasValue &&
                (Double.IsNaN(respuesta.resultado.Value) || Double.IsInfinity(respuesta.resultado.Value)))
                throw new IOException("El servidor devolvió un resultado no finito.");

            if ((respuesta.categorias ?? new string[0]).Any(String.IsNullOrWhiteSpace) ||
                (respuesta.unidades ?? new string[0]).Any(String.IsNullOrWhiteSpace))
                throw new IOException("El servidor devolvió una lista incompleta.");

            return new RespuestaSoap(respuesta.estado, respuesta.codigo, respuesta.mensaje,
                respuesta.token, respuesta.expiraEn, respuesta.resultado, respuesta.categoria,
                respuesta.unidadOrigen, respuesta.unidadDestino, respuesta.categorias, respuesta.unidades);
        }

        private static Uri Validar(Uri direccion)
        {
            if (direccion == null ||
                (!direccion.Scheme.Equals(Uri.UriSchemeHttp, StringComparison.OrdinalIgnoreCase) &&
                 !direccion.Scheme.Equals(Uri.UriSchemeHttps, StringComparison.OrdinalIgnoreCase)) ||
                String.IsNullOrWhiteSpace(direccion.Host) || !String.IsNullOrEmpty(direccion.Fragment))
                throw new ArgumentException("Indique una URL HTTP o HTTPS válida para el servicio SOAP.");
            return direccion;
        }
    }
}
