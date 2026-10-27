using System;
using System.Threading.Tasks;

namespace ec.edu.monster.pruebas;

internal static class PruebaClienteWcf
{
    private static void Main()
    {
        PruebaAutenticacion.Ejecutar();
        PruebaRecuperacionClave.Ejecutar();
        PruebaConsultaCatalogo.Ejecutar();
        PruebaConsultaUnidades.Ejecutar();
        PruebaConversion.Ejecutar();
        Console.WriteLine("Pruebas WCF del cliente: OK");
    }
}
