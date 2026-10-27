using System.Text.Json;
using ec.edu.monster.modelos;
using ec.edu.monster.servicios;

internal static class PruebaContratoRest
{
    internal static void Ejecutar(Action<bool, string> exigir, Func<ConversorUnidadesServices, string> token)
    {
        string json = JsonSerializer.Serialize(new Respuesta { Estado = "EXITO", Codigo = "OK", Categorias = ["LONGITUD"] }, new JsonSerializerOptions(JsonSerializerDefaults.Web));
        exigir(json.Contains("\"estado\"") && json.Contains("\"categorias\""), "El JSON REST debe conservar nombres camelCase");
        var servicio = new ConversorUnidadesServices("Monster", "Monster9", () => DateTimeOffset.UtcNow, TimeSpan.FromMinutes(15));
        string sesion = token(servicio);
        var categorias = servicio.ObtenerCategorias(sesion);
        categorias.Categorias.Clear();
        exigir(servicio.ObtenerCategorias(sesion).Categorias.Count == 3, "La respuesta no debe permitir mutar el catálogo interno");
    }
}
