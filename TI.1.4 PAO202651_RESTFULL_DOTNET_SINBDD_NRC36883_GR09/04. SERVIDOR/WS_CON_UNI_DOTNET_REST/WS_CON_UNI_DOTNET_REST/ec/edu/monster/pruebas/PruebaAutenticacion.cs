using ec.edu.monster.modelos;
using ec.edu.monster.servicios;

internal static class PruebaAutenticacion
{
    internal static void Ejecutar(Action<bool, string> exigir, Action<Respuesta, string> codigo)
    {
        var servicio = new ConversorUnidadesServices("Monster", "Monster9", () => DateTimeOffset.UtcNow, TimeSpan.FromMinutes(15));
        codigo(servicio.Autenticar("Monster", "incorrecta"), "AUTH-001");
        var login = servicio.Autenticar("Monster", "Monster9");
        codigo(login, "OK");
        exigir(Guid.TryParse(login.Token, out _), "El login debe emitir un token GUID");
        exigir(!string.IsNullOrWhiteSpace(login.ExpiraEn), "El login debe devolver el vencimiento de sesión");
    }
}
