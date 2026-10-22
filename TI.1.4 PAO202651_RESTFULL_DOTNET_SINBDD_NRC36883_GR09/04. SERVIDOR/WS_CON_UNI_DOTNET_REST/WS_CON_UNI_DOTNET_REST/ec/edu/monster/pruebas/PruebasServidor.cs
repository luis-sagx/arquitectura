using System.Text.Json;
using ec.edu.monster.modelos;
using ec.edu.monster.servicios;

var checks = 0;
void Check(bool condition, string message)
{
    if (!condition) throw new InvalidOperationException(message);
    checks++;
}
void Code(Respuesta response, string expected) => Check(response.Codigo == expected, $"Se esperaba {expected}; se recibió {response.Codigo}");
void Near(double? actual, double expected) => Check(actual.HasValue && Math.Abs(actual.Value - expected) < 1e-9, $"Se esperaba {expected}; se recibió {actual}");

var servicio = new ConversorUnidadesServices("monster", "monster123", () => DateTimeOffset.UtcNow, TimeSpan.FromMinutes(15));
Code(servicio.Autenticar("monster", "incorrecta"), "AUTH-001");
var login = servicio.Autenticar("monster", "monster123");
Code(login, "OK");
Check(Guid.TryParse(login.Token, out _), "El login debe emitir un token GUID");
Check(!string.IsNullOrWhiteSpace(login.ExpiraEn), "El login debe devolver el vencimiento de sesión");

var categorias = servicio.ObtenerCategorias(login.Token);
Code(categorias, "OK");
Check(string.Join(",", categorias.Categorias) == "LONGITUD,MASA,TEMPERATURA", "Catálogo de categorías incorrecto");
Check(string.Join(",", servicio.ObtenerUnidades(login.Token, "masa").Unidades) == "KILOGRAMO,GRAMO,MILIGRAMO,LIBRA", "Catálogo de masa incorrecto");
Check(string.Join(",", servicio.ObtenerUnidades(login.Token, "longitud").Unidades) == "KILOMETRO,METRO,CENTIMETRO,MILIMETRO,MILLA,PIE", "Catálogo de longitud incorrecto");
Check(string.Join(",", servicio.ObtenerUnidades(login.Token, "temperatura").Unidades) == "CELSIUS,FAHRENHEIT,KELVIN", "Catálogo de temperatura incorrecto");

Near(servicio.Convertir(login.Token, "longitud", "metro", "centimetro", 2).Resultado, 200);
Near(servicio.Convertir(login.Token, "masa", "kilogramo", "libra", 0.45359237).Resultado, 1);
Near(servicio.Convertir(login.Token, "temperatura", "celsius", "fahrenheit", 100).Resultado, 212);
Code(servicio.Convertir(login.Token, "longitud", "metro", "libra", 1), "DATA-001");
Code(servicio.Convertir(login.Token, "longitud", "desconocida", "metro", 1), "UNIT-001");
Code(servicio.Convertir(login.Token, "longitud", "metro", "centimetro", null), "DATA-001");
Code(servicio.Convertir(login.Token, "longitud", "metro", "centimetro", double.NaN), "DATA-001");
Code(servicio.Convertir(login.Token, "longitud", "metro", "centimetro", double.PositiveInfinity), "DATA-001");
Code(servicio.Convertir(login.Token, "longitud", "metro", "centimetro", -1), "DATA-001");
Code(servicio.Convertir(login.Token, "temperatura", "kelvin", "celsius", -0.01), "DATA-001");
Code(servicio.ObtenerCategorias(null), "AUTH-002");
Code(servicio.ObtenerUnidades("token-invalido", "longitud"), "AUTH-002");
Code(servicio.ObtenerUnidades(login.Token, "desconocida"), "DATA-001");

var ahora = new DateTimeOffset(2026, 10, 21, 0, 0, 0, TimeSpan.Zero);
var expirable = new ConversorUnidadesServices("monster", "monster123", () => ahora, TimeSpan.FromMinutes(1));
var sesionCorta = expirable.Autenticar("monster", "monster123");
ahora = ahora.AddMinutes(1);
Code(expirable.ObtenerCategorias(sesionCorta.Token), "AUTH-003");

var sesionAntesDeRecuperar = servicio.Autenticar("monster", "monster123");
Code(servicio.RecuperarClave("monster", "clave-temporal"), "OK");
Code(servicio.ObtenerCategorias(sesionAntesDeRecuperar.Token), "AUTH-002");
Code(servicio.Autenticar("monster", "monster123"), "AUTH-001");
var nuevoLogin = servicio.Autenticar("monster", "clave-temporal");
Code(nuevoLogin, "OK");
Code(servicio.RecuperarClave("usuario-desconocido", "nueva"), "AUTH-001");
Code(servicio.RecuperarClave("monster", "  "), "DATA-001");

string json = JsonSerializer.Serialize(new Respuesta { Estado = "EXITO", Codigo = "OK", Categorias = ["LONGITUD"] }, new JsonSerializerOptions(JsonSerializerDefaults.Web));
Check(json.Contains("\"estado\"") && json.Contains("\"categorias\""), "El JSON REST debe conservar nombres en minúscula camelCase");
var catalogoMutable = servicio.ObtenerCategorias(nuevoLogin.Token);
catalogoMutable.Categorias.Clear();
Check(servicio.ObtenerCategorias(nuevoLogin.Token).Categorias.Count == 3, "La respuesta no debe permitir mutar el catálogo interno");

Console.WriteLine($"Pruebas del servidor correctas: {checks} comprobaciones.");
