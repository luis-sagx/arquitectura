using System;
using System.Threading.Tasks;

namespace ec.edu.monster.pruebas;

internal static class PruebaClienteWcf
{
    private static async Task Main()
    {
        await PruebaAutenticacion.Ejecutar();
        await PruebaRecuperacionClave.Ejecutar();
        await PruebaConsultaCatalogo.Ejecutar();
        await PruebaConsultaUnidades.Ejecutar();
        await PruebaConversion.Ejecutar();
        Console.WriteLine("Pruebas WCF del cliente web: OK");
    }
}

