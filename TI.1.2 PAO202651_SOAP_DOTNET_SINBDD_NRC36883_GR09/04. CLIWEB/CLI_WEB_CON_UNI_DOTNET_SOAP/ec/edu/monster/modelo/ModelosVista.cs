using System.Collections.Generic;

namespace ec.edu.monster.modelo
{
    /// <summary>Datos de la página de acceso.</summary>
    public sealed class LoginModelo
    {
        public string Usuario { get; set; } = "";
        public string? Error { get; set; }
        public string? Aviso { get; set; }
    }

    /// <summary>Datos de la página de recuperación de clave.</summary>
    public sealed class RecuperarModelo
    {
        public string Usuario { get; set; } = "";
        public string? Error { get; set; }
        public string? Exito { get; set; }
    }

    /// <summary>Datos de la página de conversión: catálogo, selección actual y resultado.</summary>
    public sealed class ConversorModelo
    {
        public IList<OpcionCatalogo> Categorias { get; set; } = new List<OpcionCatalogo>();
        public IList<OpcionCatalogo> Unidades { get; set; } = new List<OpcionCatalogo>();
        public string? Categoria { get; set; }
        public string? Origen { get; set; }
        public string? Destino { get; set; }
        public string Valor { get; set; } = "";
        public string? Resultado { get; set; }
        public string? DetalleResultado { get; set; }
        public string? Error { get; set; }
        public string HoraExpiracion { get; set; } = "";
    }
}
