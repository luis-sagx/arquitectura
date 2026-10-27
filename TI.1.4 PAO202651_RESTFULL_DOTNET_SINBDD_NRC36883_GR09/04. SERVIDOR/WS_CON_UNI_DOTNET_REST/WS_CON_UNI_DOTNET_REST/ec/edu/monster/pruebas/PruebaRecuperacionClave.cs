using ec.edu.monster.modelos;
using ec.edu.monster.servicios;

internal static class PruebaRecuperacionClave
{
    internal static void Ejecutar(Action<bool, string> exigir, Action<Respuesta, string> codigo, Func<ConversorUnidadesServices, string> token)
    {
        var servicio = new ConversorUnidadesServices("Monster", "Monster9", () => DateTimeOffset.UtcNow, TimeSpan.FromMinutes(15));
        string tokenAnterior = token(servicio);
        codigo(servicio.RecuperarClave("Monster", "clave-temporal"), "OK");
        codigo(servicio.ObtenerCategorias(tokenAnterior), "AUTH-002");
        codigo(servicio.Autenticar("Monster", "Monster9"), "AUTH-001");
        codigo(servicio.Autenticar("Monster", "clave-temporal"), "OK");
        codigo(servicio.RecuperarClave("usuario-desconocido", "nueva"), "AUTH-001");
        codigo(servicio.RecuperarClave("Monster", "  "), "DATA-001");
    }
}
