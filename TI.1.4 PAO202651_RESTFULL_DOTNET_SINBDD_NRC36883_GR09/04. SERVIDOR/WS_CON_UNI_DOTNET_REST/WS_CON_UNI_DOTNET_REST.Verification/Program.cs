using System.Text.Json;
using ec.edu.monster.modelos;
using ec.edu.monster.servicios;

var checks = 0;
void Check(bool condition, string message)
{
    if (!condition) throw new InvalidOperationException(message);
    checks++;
}
void Code(Respuesta response, string expected) => Check(response.Codigo == expected, $"Expected {expected}, got {response.Codigo}");
void Near(double? actual, double expected) => Check(actual.HasValue && Math.Abs(actual.Value - expected) < 1e-9, $"Expected {expected}, got {actual}");

var service = new ConversorUnidadesServices("monster", "monster123", () => DateTimeOffset.UtcNow, TimeSpan.FromMinutes(15));
Code(service.Autenticar("monster", "bad"), "AUTH-001");
var login = service.Autenticar("monster", "monster123");
Code(login, "OK");
Check(Guid.TryParse(login.Token, out _), "Login must issue a GUID token");
Check(!string.IsNullOrWhiteSpace(login.ExpiraEn), "Login must issue an expiry instant");
Code(service.ObtenerCategorias(login.Token), "OK");
Check(string.Join(",", service.ObtenerCategorias(login.Token).Categorias) == "LONGITUD,MASA,TEMPERATURA", "Category catalog mismatch");
Check(string.Join(",", service.ObtenerUnidades(login.Token, "masa").Unidades) == "KILOGRAMO,GRAMO,MILIGRAMO,LIBRA", "Mass units mismatch");
Near(service.Convertir(login.Token, "longitud", "metro", "centimetro", 2).Resultado, 200);
Near(service.Convertir(login.Token, "masa", "kilogramo", "libra", 0.45359237).Resultado, 1);
Near(service.Convertir(login.Token, "temperatura", "celsius", "fahrenheit", 100).Resultado, 212);
Code(service.Convertir(login.Token, "longitud", "metro", "libra", 1), "DATA-001");
Code(service.Convertir(login.Token, "longitud", "desconocida", "metro", 1), "UNIT-001");
Code(service.Convertir(login.Token, "longitud", "metro", "centimetro", null), "DATA-001");
Code(service.ObtenerCategorias(null), "AUTH-002");

var now = new DateTimeOffset(2026, 10, 21, 0, 0, 0, TimeSpan.Zero);
var expiring = new ConversorUnidadesServices("monster", "monster123", () => now, TimeSpan.FromMinutes(1));
var shortSession = expiring.Autenticar("monster", "monster123");
now = now.AddMinutes(1);
Code(expiring.ObtenerCategorias(shortSession.Token), "AUTH-003");
var recover = service.Autenticar("monster", "monster123");
Code(service.RecuperarClave("monster", "new-password"), "OK");
Code(service.ObtenerCategorias(recover.Token), "AUTH-002");
Code(service.Autenticar("monster", "monster123"), "AUTH-001");
var currentLogin = service.Autenticar("monster", "new-password");
Code(currentLogin, "OK");

var json = JsonSerializer.Serialize(new Respuesta { Estado = "EXITO", Codigo = "OK", Categorias = ["LONGITUD"] }, new JsonSerializerOptions(JsonSerializerDefaults.Web));
Check(json.Contains("\"estado\"") && json.Contains("\"categorias\""), "Response JSON fields must use lower camel case");
Code(service.RecuperarClave("unknown", "new-password"), "AUTH-001");
Code(service.RecuperarClave("monster", "  "), "DATA-001");
Code(service.ObtenerUnidades(currentLogin.Token, "missing"), "DATA-001");
Code(service.Convertir(currentLogin.Token, "LONGITUD", "METRO", "LIBRA", 1), "DATA-001");
Code(service.Convertir(currentLogin.Token, "LONGITUD", "METRO", "DESCONOCIDA", 1), "UNIT-001");
Code(service.Convertir(currentLogin.Token, "LONGITUD", "METRO", "CENTIMETRO", double.NaN), "DATA-001");
Code(service.Convertir(currentLogin.Token, "LONGITUD", "METRO", "CENTIMETRO", double.PositiveInfinity), "DATA-001");
Code(service.Convertir(currentLogin.Token, "LONGITUD", "METRO", "CENTIMETRO", -1), "DATA-001");
Code(service.Convertir(currentLogin.Token, "TEMPERATURA", "KELVIN", "CELSIUS", -0.01), "DATA-001");
Code(service.Convertir("invalid", "LONGITUD", "METRO", "CENTIMETRO", 1), "AUTH-002");
var defensiveCatalog = service.ObtenerCategorias(currentLogin.Token);
defensiveCatalog.Categorias.Clear();
Check(service.ObtenerCategorias(currentLogin.Token).Categorias.Count == 3, "Clients must not mutate the internal category catalog");
Console.WriteLine($"Behavior verification passed: {checks} checks" );
