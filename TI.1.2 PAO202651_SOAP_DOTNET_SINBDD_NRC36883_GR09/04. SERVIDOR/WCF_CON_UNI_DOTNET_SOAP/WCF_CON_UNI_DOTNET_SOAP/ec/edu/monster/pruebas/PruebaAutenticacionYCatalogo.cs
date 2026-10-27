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
    private static void AutenticacionYCatalogo()
    {
        var servicio = new ConversorUnidadesServices();
        string token1 = Token(servicio);
        string token2 = Token(servicio);
        Exigir(token1 != token2, "Cada autenticacion debe generar un token distinto");
        Codigo(servicio.Autenticar("Monster", "incorrecta"), "AUTH-001");
        Codigo(servicio.Autenticar("desconocido", "Monster9"), "AUTH-001");
        Codigo(servicio.Autenticar(null, null), "AUTH-001");
        Codigo(servicio.Autenticar("", ""), "AUTH-001");
        Codigo(servicio.ObtenerCategorias(null), "AUTH-002");

        Respuesta categorias = servicio.ObtenerCategorias(token1);
        Codigo(categorias, "OK");
        Exigir(String.Join(",", categorias.Categorias) == "LONGITUD,MASA,TEMPERATURA", "Catalogo de categorias incorrecto");
        categorias.Categorias.Clear();
        Exigir(servicio.ObtenerCategorias(token1).Categorias.Count == 3, "El cliente no debe modificar el catalogo interno");
        Exigir(String.Join(",", servicio.ObtenerUnidades(token1, " longitud ").Unidades) ==
            "KILOMETRO,METRO,CENTIMETRO,MILIMETRO,MILLA,PIE", "Catalogo de longitud incorrecto");
        Exigir(String.Join(",", servicio.ObtenerUnidades(token1, "MASA").Unidades) ==
            "KILOGRAMO,GRAMO,MILIGRAMO,LIBRA", "Catalogo de masa incorrecto");
        Exigir(String.Join(",", servicio.ObtenerUnidades(token1, "TEMPERATURA").Unidades) ==
            "CELSIUS,FAHRENHEIT,KELVIN", "Catalogo de temperatura incorrecto");
        Codigo(servicio.ObtenerUnidades(token1, "INEXISTENTE"), "DATA-001");
    }
}
}