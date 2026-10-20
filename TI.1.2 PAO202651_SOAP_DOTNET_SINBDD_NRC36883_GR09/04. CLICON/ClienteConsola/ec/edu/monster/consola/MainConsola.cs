using System;
using System.Configuration;
using System.Text;
using ec.edu.monster.cliente.ws;
using ec.edu.monster.consola.controlador;
using ec.edu.monster.consola.vista;

namespace ec.edu.monster.consola
{
    internal static class MainConsola
    {
        private const string EndpointPredeterminado = "http://localhost:14028/WSConUni.svc";

        private static void Main(string[] args)
        {
            Console.OutputEncoding = Encoding.UTF8;

            string endpoint = args.Length > 0
                ? args[0]
                : ConfigurationManager.AppSettings["conversor.endpoint"];
            if (string.IsNullOrWhiteSpace(endpoint)) endpoint = EndpointPredeterminado;

            try
            {
                Uri direccion = ClienteSoap.ValidarEndpoint(endpoint);
                using (var cliente = new ClienteSoap(direccion))
                {
                    Console.WriteLine("Servicio SOAP: " + direccion);
                    new ControladorConsola(cliente, new VistaConsola()).Ejecutar();
                }
            }
            catch (ArgumentException ex)
            {
                Console.Error.WriteLine("Endpoint inválido: " + ex.Message);
                Console.Error.WriteLine("Uso: ClienteConsola.exe [URL_DEL_SERVICIO]");
            }
        }
    }
}
