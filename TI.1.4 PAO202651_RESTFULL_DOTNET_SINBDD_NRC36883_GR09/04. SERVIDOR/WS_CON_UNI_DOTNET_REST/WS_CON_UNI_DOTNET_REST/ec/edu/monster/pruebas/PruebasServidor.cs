using System.Text.Json;
using ec.edu.monster.modelos;
using ec.edu.monster.servicios;

internal static class PruebasServidor
{
    private static int comprobaciones;

    private static void Exigir(bool condicion, string mensaje)
    {
        if (!condicion) throw new InvalidOperationException(mensaje);
        comprobaciones++;
    }

    private static void Codigo(Respuesta respuesta, string esperado) =>
        Exigir(respuesta.Codigo == esperado, $"Se esperaba {esperado}; se recibió {respuesta.Codigo}");

    private static void Resultado(double? actual, double esperado) =>
        Exigir(actual.HasValue && Math.Abs(actual.Value - esperado) < 1e-9, $"Se esperaba {esperado}; se recibió {actual}");

    private static string Token(ConversorUnidadesServices servicio)
    {
        var respuesta = servicio.Autenticar("Monster", "Monster9");
        Codigo(respuesta, "OK");
        return respuesta.Token;
    }

    private static void Main()
    {
        PruebaAutenticacion.Ejecutar(Exigir, Codigo);
        PruebaCatalogo.Ejecutar(Exigir, Codigo, Token);
        PruebaConversiones.Ejecutar(Exigir, Codigo, Resultado, Token);
        PruebaSesiones.Ejecutar(Exigir, Codigo);
        PruebaRecuperacionClave.Ejecutar(Exigir, Codigo, Token);
        PruebaContratoRest.Ejecutar(Exigir, Token);
        Console.WriteLine($"Pruebas del servidor correctas: {comprobaciones} comprobaciones.");
    }
}
