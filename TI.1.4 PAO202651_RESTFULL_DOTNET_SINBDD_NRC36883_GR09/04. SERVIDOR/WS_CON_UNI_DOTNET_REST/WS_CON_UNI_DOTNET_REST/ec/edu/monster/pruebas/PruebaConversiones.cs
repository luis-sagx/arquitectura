using ec.edu.monster.modelos;
using ec.edu.monster.servicios;

internal static class PruebaConversiones
{
    internal static void Ejecutar(Action<bool, string> exigir, Action<Respuesta, string> codigo, Action<double?, double> resultado, Func<ConversorUnidadesServices, string> token)
    {
        var servicio = new ConversorUnidadesServices("Monster", "Monster9", () => DateTimeOffset.UtcNow, TimeSpan.FromMinutes(15));
        string sesion = token(servicio);
        resultado(servicio.Convertir(sesion, "longitud", "metro", "centimetro", 2).Resultado, 200);
        resultado(servicio.Convertir(sesion, "masa", "kilogramo", "libra", 0.45359237).Resultado, 1);
        resultado(servicio.Convertir(sesion, "temperatura", "celsius", "fahrenheit", 100).Resultado, 212);
        codigo(servicio.Convertir(sesion, "longitud", "metro", "libra", 1), "DATA-001");
        codigo(servicio.Convertir(sesion, "longitud", "desconocida", "metro", 1), "UNIT-001");
        codigo(servicio.Convertir(sesion, "longitud", "metro", "centimetro", null), "DATA-001");
        codigo(servicio.Convertir(sesion, "longitud", "metro", "centimetro", double.NaN), "DATA-001");
        codigo(servicio.Convertir(sesion, "longitud", "metro", "centimetro", double.PositiveInfinity), "DATA-001");
        codigo(servicio.Convertir(sesion, "longitud", "metro", "centimetro", -1), "DATA-001");
        codigo(servicio.Convertir(sesion, "temperatura", "kelvin", "celsius", -0.01), "DATA-001");
    }
}
