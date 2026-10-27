using ec.edu.monster.modelos;
using ec.edu.monster.servicios;

internal static class PruebaSesiones
{
    internal static void Ejecutar(Action<bool, string> exigir, Action<Respuesta, string> codigo)
    {
        var ahora = new DateTimeOffset(2026, 10, 21, 0, 0, 0, TimeSpan.Zero);
        var servicio = new ConversorUnidadesServices("Monster", "Monster9", () => ahora, TimeSpan.FromMinutes(1));
        var sesion = servicio.Autenticar("Monster", "Monster9");
        codigo(sesion, "OK");
        ahora = ahora.AddMinutes(1);
        codigo(servicio.ObtenerCategorias(sesion.Token), "AUTH-003");
    }
}
