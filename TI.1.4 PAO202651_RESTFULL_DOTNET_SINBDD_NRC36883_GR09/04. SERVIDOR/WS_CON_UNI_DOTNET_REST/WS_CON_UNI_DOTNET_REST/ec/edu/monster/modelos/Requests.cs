namespace ec.edu.monster.modelos.requests;

public sealed class AutenticarRequest
{
    public string? Usuario { get; set; }
    public string? Clave { get; set; }
}

public sealed class RecuperarClaveRequest
{
    public string? Usuario { get; set; }
    public string? NuevaClave { get; set; }
}

public sealed class ConvertirRequest
{
    public string? Categoria { get; set; }
    public string? UnidadOrigen { get; set; }
    public string? UnidadDestino { get; set; }
    public double? Valor { get; set; }
}
