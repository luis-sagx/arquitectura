using System.Text.Json.Serialization;

namespace ec.edu.monster.modelos;

public sealed class Respuesta
{
    [JsonPropertyName("estado")]
    public string? Estado { get; set; }

    [JsonPropertyName("codigo")]
    public string? Codigo { get; set; }

    [JsonPropertyName("mensaje")]
    public string? Mensaje { get; set; }

    [JsonPropertyName("token")]
    public string? Token { get; set; }

    [JsonPropertyName("expiraEn")]
    public string? ExpiraEn { get; set; }

    [JsonPropertyName("resultado")]
    public double? Resultado { get; set; }

    [JsonPropertyName("categoria")]
    public string? Categoria { get; set; }

    [JsonPropertyName("unidadOrigen")]
    public string? UnidadOrigen { get; set; }

    [JsonPropertyName("unidadDestino")]
    public string? UnidadDestino { get; set; }

    [JsonPropertyName("categorias")]
    public List<string> Categorias { get; set; } = [];

    [JsonPropertyName("unidades")]
    public List<string> Unidades { get; set; } = [];

    public static Respuesta Exito(string mensaje) => new() { Estado = "EXITO", Codigo = "OK", Mensaje = mensaje };

    public static Respuesta Error(string codigo, string mensaje) => new() { Estado = "ERROR", Codigo = codigo, Mensaje = mensaje };
}
