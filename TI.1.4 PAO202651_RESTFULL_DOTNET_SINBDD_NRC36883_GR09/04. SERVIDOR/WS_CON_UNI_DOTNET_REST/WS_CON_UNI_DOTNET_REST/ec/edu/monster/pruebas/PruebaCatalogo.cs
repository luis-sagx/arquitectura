using ec.edu.monster.modelos;
using ec.edu.monster.servicios;

internal static class PruebaCatalogo
{
    internal static void Ejecutar(Action<bool, string> exigir, Action<Respuesta, string> codigo, Func<ConversorUnidadesServices, string> token)
    {
        var servicio = new ConversorUnidadesServices("Monster", "Monster9", () => DateTimeOffset.UtcNow, TimeSpan.FromMinutes(15));
        string sesion = token(servicio);
        var categorias = servicio.ObtenerCategorias(sesion);
        codigo(categorias, "OK");
        exigir(string.Join(",", categorias.Categorias) == "LONGITUD,MASA,TEMPERATURA", "Catálogo de categorías incorrecto");
        exigir(string.Join(",", servicio.ObtenerUnidades(sesion, "masa").Unidades) == "KILOGRAMO,GRAMO,MILIGRAMO,LIBRA", "Catálogo de masa incorrecto");
        exigir(string.Join(",", servicio.ObtenerUnidades(sesion, "longitud").Unidades) == "KILOMETRO,METRO,CENTIMETRO,MILIMETRO,MILLA,PIE", "Catálogo de longitud incorrecto");
        exigir(string.Join(",", servicio.ObtenerUnidades(sesion, "temperatura").Unidades) == "CELSIUS,FAHRENHEIT,KELVIN", "Catálogo de temperatura incorrecto");
        codigo(servicio.ObtenerCategorias(null), "AUTH-002");
        codigo(servicio.ObtenerUnidades("token-invalido", "longitud"), "AUTH-002");
        codigo(servicio.ObtenerUnidades(sesion, "desconocida"), "DATA-001");
    }
}
