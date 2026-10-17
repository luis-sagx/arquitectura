using System;
using System.Collections.Generic;
using System.Globalization;

namespace ec.edu.monster.modelo
{
    /// <summary>Identificador enviado al servicio y etiqueta legible para los selectores.</summary>
    public sealed class OpcionCatalogo
    {
        private static readonly Dictionary<string, string> Etiquetas = new Dictionary<string, string>
        {
            { "LONGITUD", "Longitud" }, { "MASA", "Masa" }, { "TEMPERATURA", "Temperatura" },
            { "KILOMETRO", "Kilómetro" }, { "METRO", "Metro" }, { "CENTIMETRO", "Centímetro" },
            { "MILIMETRO", "Milímetro" }, { "MILLA", "Milla" }, { "PIE", "Pie" },
            { "KILOGRAMO", "Kilogramo" }, { "GRAMO", "Gramo" }, { "MILIGRAMO", "Miligramo" },
            { "LIBRA", "Libra" }, { "CELSIUS", "Celsius" }, { "FAHRENHEIT", "Fahrenheit" },
            { "KELVIN", "Kelvin" }
        };

        public OpcionCatalogo(string id, string? etiqueta)
        {
            if (String.IsNullOrWhiteSpace(id))
                throw new ArgumentException("El identificador del catálogo es obligatorio.", nameof(id));
            Id = id;
            Etiqueta = String.IsNullOrWhiteSpace(etiqueta) ? Formatear(id) : etiqueta;
        }

        public string Id { get; }
        public string Etiqueta { get; }

        public static OpcionCatalogo Desde(string id)
        {
            return new OpcionCatalogo(id, Formatear(id));
        }

        /// <summary>El Picker usa ToString() para pintar cada opción.</summary>
        public override string ToString()
        {
            return Etiqueta;
        }

        private static string Formatear(string valor)
        {
            string id = valor.Trim().ToUpperInvariant();
            string? etiqueta;
            if (Etiquetas.TryGetValue(id, out etiqueta)) return etiqueta;
            string texto = id.Replace('_', ' ').ToLowerInvariant();
            return texto.Length == 0 ? texto
                : texto.Substring(0, 1).ToUpper(CultureInfo.InvariantCulture) + texto.Substring(1);
        }
    }
}
