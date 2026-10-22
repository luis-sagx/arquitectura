using System;
using System.Configuration;
using System.Text;
using ec.edu.monster.cliente.rest;
using ec.edu.monster.consola.controlador;
using ec.edu.monster.consola.vista;

namespace ec.edu.monster.consola
{
    internal static class MainConsola
    {
        private const string EndpointPredeterminado = "http://localhost:5111/api/ws-con-uni";

        private static void Main(string[] args)
        {
            Console.OutputEncoding = Encoding.UTF8;

            string endpoint = args.Length > 0
                ? args[0]
                : ConfigurationManager.AppSettings["conversor.endpoint"];
            if (string.IsNullOrWhiteSpace(endpoint)) endpoint = EndpointPredeterminado;

            try
            {
                Uri direccion = ClienteRest.ValidarEndpoint(endpoint);
                using (var cliente = new ClienteRest(direccion))
                {
                    Console.WriteLine("Servicio REST: " + direccion);
                    new ControladorConsola(cliente, new VistaConsola()).Ejecutar();
                }
            }
            catch (ArgumentException ex)
            {
                Console.Error.WriteLine("Endpoint inválido: " + ex.Message);
                Console.Error.WriteLine("Uso: ClienteConsola.exe [URL_DE_LA_API, por ejemplo http://localhost:5111/api/ws-con-uni]");
            }
        }
    }
}
