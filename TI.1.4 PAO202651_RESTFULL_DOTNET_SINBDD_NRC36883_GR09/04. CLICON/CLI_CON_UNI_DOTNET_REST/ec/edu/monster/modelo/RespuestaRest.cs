using System.Collections.Generic;

namespace ec.edu.monster.modelo
{
    /// <summary>Respuesta normalizada del servicio REST de conversión.</summary>
    internal sealed class RespuestaRest
    {
        public RespuestaRest(string estado, string codigo, string mensaje, string token,
            string expiraEn, double? resultado, string unidadOrigen, string unidadDestino,
            IList<string> categorias, IList<string> unidades)
        {
            Estado = estado ?? string.Empty;
            Codigo = codigo ?? string.Empty;
            Mensaje = mensaje ?? string.Empty;
            Token = token ?? string.Empty;
            ExpiraEn = expiraEn ?? string.Empty;
            Resultado = resultado;
            UnidadOrigen = unidadOrigen ?? string.Empty;
            UnidadDestino = unidadDestino ?? string.Empty;
            Categorias = categorias ?? new List<string>();
            Unidades = unidades ?? new List<string>();
        }

        public string Estado { get; private set; }
        public string Codigo { get; private set; }
        public string Mensaje { get; private set; }
        public string Token { get; private set; }
        public string ExpiraEn { get; private set; }
        public double? Resultado { get; private set; }
        public string UnidadOrigen { get; private set; }
        public string UnidadDestino { get; private set; }
        public IList<string> Categorias { get; private set; }
        public IList<string> Unidades { get; private set; }

        public bool EsExito { get { return Estado == "EXITO" && Codigo == "OK"; } }
        public bool EsSesionInvalida { get { return Codigo == "AUTH-002" || Codigo == "AUTH-003"; } }
    }
}
