using System;
using System.Threading.Tasks;

namespace ec.edu.monster.pruebas;

internal static class PruebaClienteRest
{
    private static async Task Main()
    {
        await PruebaAutenticacion.EjecutarAsync();
        await PruebaRecuperacionClave.EjecutarAsync();
        await PruebaConsultaCatalogo.EjecutarAsync();
        await PruebaConsultaUnidades.EjecutarAsync();
        await PruebaConversion.EjecutarAsync();
        Console.WriteLine("Pruebas REST del cliente: OK");
    }
}

