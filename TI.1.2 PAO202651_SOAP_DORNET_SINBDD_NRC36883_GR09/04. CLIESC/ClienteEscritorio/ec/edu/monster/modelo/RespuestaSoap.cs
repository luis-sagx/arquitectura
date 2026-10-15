using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;

namespace ec.edu.monster.modelo
{
    public sealed class RespuestaSoap
    {
        public RespuestaSoap(string estado, string codigo, string mensaje, string token,
            string expiraEn, double? resultado, string categoria, string unidadOrigen,
            string unidadDestino, IEnumerable<string> categorias, IEnumerable<string> unidades)
        {
            Estado = estado ?? String.Empty;
            Codigo = codigo ?? String.Empty;
            Mensaje = mensaje ?? String.Empty;
            Token = token ?? String.Empty;
            ExpiraEn = expiraEn ?? String.Empty;
            Resultado = resultado;
            Categoria = categoria ?? String.Empty;
            UnidadOrigen = unidadOrigen ?? String.Empty;
            UnidadDestino = unidadDestino ?? String.Empty;
            Categorias = Copiar(categorias);
            Unidades = Copiar(unidades);
        }

        public string Estado { get; private set; }
        public string Codigo { get; private set; }
        public string Mensaje { get; private set; }
        public string Token { get; private set; }
        public string ExpiraEn { get; private set; }
        public double? Resultado { get; private set; }
        public string Categoria { get; private set; }
        public string UnidadOrigen { get; private set; }
        public string UnidadDestino { get; private set; }
        public IList<string> Categorias { get; private set; }
        public IList<string> Unidades { get; private set; }

        public bool IsExito
        {
            get { return String.Equals(Estado, "EXITO", StringComparison.OrdinalIgnoreCase); }
        }

        private static IList<string> Copiar(IEnumerable<string> valores)
        {
            var copia = valores == null ? new List<string>() : new List<string>(valores);
            return new ReadOnlyCollection<string>(copia);
        }
    }
}
