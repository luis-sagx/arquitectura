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
            { "LONGITUD", "Longitud" }, { "MASA", "Masa/peso" }, { "TEMPERATURA", "Temperatura" },
            { "VOLUMEN", "Volumen" }, { "TIEMPO", "Tiempo" }, { "AREA", "Área" },
            { "KILOMETRO", "Kilómetro (km)" }, { "METRO", "Metro (m)" }, { "CENTIMETRO", "Centímetro (cm)" },
            { "MILIMETRO", "Milímetro (mm)" }, { "MILLA", "Milla (mi)" }, { "PIE", "Pie (ft)" },
            { "KILOGRAMO", "Kilogramo (kg)" }, { "GRAMO", "Gramo (g)" }, { "MILIGRAMO", "Miligramo (mg)" },
            { "LIBRA", "Libra (lb)" }, { "CELSIUS", "Celsius (°C)" }, { "FAHRENHEIT", "Fahrenheit (°F)" },
            { "KELVIN", "Kelvin (K)" }, { "MILILITRO", "Mililitro (mL)" }, { "LITRO", "Litro (L)" },
            { "METRO_CUBICO", "Metro cúbico (m³)" }, { "GALON_US", "Galón estadounidense (gal)" },
            { "SEGUNDO", "Segundo (s)" }, { "MINUTO", "Minuto (min)" }, { "HORA", "Hora (h)" }, { "DIA", "Día (d)" },
            { "METRO_CUADRADO", "Metro cuadrado (m²)" }, { "KILOMETRO_CUADRADO", "Kilómetro cuadrado (km²)" },
            { "HECTAREA", "Hectárea (ha)" }, { "ACRE", "Acre (ac)" }
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
