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
    private static void SesionYRecuperacion()
    {
        DateTimeOffset ahora = new DateTimeOffset(2026, 10, 9, 0, 0, 0, TimeSpan.Zero);
        var servicio = new ConversorUnidadesServices("Monster", "Monster9", () => ahora, TimeSpan.FromMinutes(15));
        string token = Token(servicio);
        ahora = ahora.AddSeconds(899);
        Resultado(servicio.Convertir(token, "LONGITUD", "METRO", "CENTIMETRO", 1.0), 100.0);
        ahora = ahora.AddSeconds(1);
        Codigo(servicio.Convertir(token, "LONGITUD", "METRO", "CENTIMETRO", 1.0), "AUTH-003");
        Codigo(servicio.ObtenerCategorias(token), "AUTH-003");
        Codigo(servicio.ObtenerUnidades(token, "MASA"), "AUTH-003");

        Codigo(servicio.RecuperarClave("desconocido", "nueva123"), "AUTH-001");
        Codigo(servicio.RecuperarClave("Monster", "  "), "DATA-001");
        string tokenAnterior = Token(servicio);
        Codigo(servicio.RecuperarClave("Monster", "nueva123"), "OK");
        Codigo(servicio.Autenticar("Monster", "Monster9"), "AUTH-001");
        Respuesta nuevaSesion = servicio.Autenticar("Monster", "nueva123");
        Codigo(nuevaSesion, "OK");
        Codigo(servicio.ObtenerCategorias(tokenAnterior), "AUTH-002");
    }
}
}