using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;

namespace ec.edu.monster.modelo
{
    /// <summary>Respuesta de negocio normalizada para que el MVC no dependa del JSON del servicio.</summary>
    public sealed class RespuestaRest
    {
        public RespuestaRest(string? estado, string? codigo, string? mensaje, string? token,
            string? expiraEn, double? resultado, string? unidadOrigen, string? unidadDestino,
            IEnumerable<string>? categorias, IEnumerable<string>? unidades)
        {
            Estado = estado ?? String.Empty;
            Codigo = codigo ?? String.Empty;
            Mensaje = mensaje ?? String.Empty;
            Token = token ?? String.Empty;
            ExpiraEn = expiraEn ?? String.Empty;
            Resultado = resultado;
            UnidadOrigen = unidadOrigen ?? String.Empty;
            UnidadDestino = unidadDestino ?? String.Empty;
            Categorias = Copiar(categorias);
            Unidades = Copiar(unidades);
        }

        public string Estado { get; }
        public string Codigo { get; }
        public string Mensaje { get; }
        public string Token { get; }
        public string ExpiraEn { get; }
        public double? Resultado { get; }
        public string UnidadOrigen { get; }
        public string UnidadDestino { get; }
        public IList<string> Categorias { get; }
        public IList<string> Unidades { get; }

        public bool IsExito
        {
            get
            {
                return String.Equals(Estado, "EXITO", StringComparison.OrdinalIgnoreCase)
                    && String.Equals(Codigo, "OK", StringComparison.OrdinalIgnoreCase);
            }
        }

        public bool IsSesionInvalida
        {
            get { return Codigo == "AUTH-002" || Codigo == "AUTH-003"; }
        }

        private static IList<string> Copiar(IEnumerable<string>? valores)
        {
            var copia = valores == null ? new List<string>() : new List<string>(valores);
            return new ReadOnlyCollection<string>(copia);
        }
    }
}
