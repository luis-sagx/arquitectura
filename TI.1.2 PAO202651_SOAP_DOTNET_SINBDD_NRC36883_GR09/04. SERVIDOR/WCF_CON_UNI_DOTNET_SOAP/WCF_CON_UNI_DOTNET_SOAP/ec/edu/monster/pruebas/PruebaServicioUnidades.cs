using System;
using System.Collections.Generic;
using System.Reflection;
using System.ServiceModel;
using System.ServiceModel.Description;
using System.Web.Services.Description;
using System.Xml.Schema;
using ec.edu.monster.controladores.ws;
using ec.edu.monster.modelos;
using ec.edu.monster.servicios;

namespace ec.edu.monster.pruebas
{
internal static partial class PruebaServicioUnidades
{
    private static int comprobaciones;

    private static void Exigir(bool condicion, string mensaje)
    {
        comprobaciones++;
        if (!condicion) throw new InvalidOperationException(mensaje);
    }

    private static void Codigo(Respuesta respuesta, string esperado)
    {
        Exigir(respuesta != null && respuesta.Codigo == esperado,
            "Se esperaba codigo " + esperado + ", se obtuvo " + (respuesta == null ? "null" : respuesta.Codigo));
    }

    private static void Resultado(Respuesta respuesta, double esperado)
    {
        Codigo(respuesta, "OK");
        Exigir(respuesta.Resultado.HasValue && Math.Abs(respuesta.Resultado.Value - esperado) <= Math.Abs(esperado) * 1e-12 + 1e-12,
            "Resultado esperado " + esperado + ", obtenido " + (respuesta.Resultado.HasValue ? respuesta.Resultado.Value.ToString("R") : "null"));
    }

    private static string Token(ConversorUnidadesServices servicio)
    {
        Respuesta respuesta = servicio.Autenticar("Monster", "Monster9");
        Codigo(respuesta, "OK");
        Exigir(!String.IsNullOrWhiteSpace(respuesta.Token), "Autenticacion debe emitir token");
        return respuesta.Token;
    }

    private static void Main()
    {
        try
        {
            AutenticacionYCatalogo();
            ConversionesYValidaciones();
            SesionYRecuperacion();
            ContratoSoap();
            Console.WriteLine("PruebaServicioUnidades: OK (" + comprobaciones + " comprobaciones)");
        }
        catch (Exception error)
        {
            Console.Error.WriteLine("PruebaServicioUnidades: ERROR: " + error.Message);
            Environment.ExitCode = 1;
        }
    }








}
}
