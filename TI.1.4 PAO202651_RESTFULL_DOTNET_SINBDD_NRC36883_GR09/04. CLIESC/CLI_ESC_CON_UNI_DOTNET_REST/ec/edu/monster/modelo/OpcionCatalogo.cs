namespace ec.edu.monster.modelo
{
    public sealed class OpcionCatalogo
    {
        public OpcionCatalogo(string valor, string texto)
        {
            Valor = valor;
            Texto = texto;
        }

        public string Valor { get; private set; }
        public string Texto { get; private set; }

        public static OpcionCatalogo Desde(string id)
        {
            string texto;
            switch (id)
            {
                case "LONGITUD": texto = "Longitud"; break; case "MASA": texto = "Masa/peso"; break; case "VOLUMEN": texto = "Volumen"; break;
                case "TIEMPO": texto = "Tiempo"; break; case "TEMPERATURA": texto = "Temperatura"; break; case "AREA": texto = "Área"; break;
                case "KILOMETRO": texto = "Kilómetro (km)"; break; case "METRO": texto = "Metro (m)"; break; case "CENTIMETRO": texto = "Centímetro (cm)"; break;
                case "MILIMETRO": texto = "Milímetro (mm)"; break; case "MILLA": texto = "Milla (mi)"; break; case "PIE": texto = "Pie (ft)"; break;
                case "KILOGRAMO": texto = "Kilogramo (kg)"; break; case "GRAMO": texto = "Gramo (g)"; break; case "MILIGRAMO": texto = "Miligramo (mg)"; break;
                case "LIBRA": texto = "Libra (lb)"; break; case "CELSIUS": texto = "Celsius (°C)"; break; case "FAHRENHEIT": texto = "Fahrenheit (°F)"; break;
                case "KELVIN": texto = "Kelvin (K)"; break; case "MILILITRO": texto = "Mililitro (mL)"; break; case "LITRO": texto = "Litro (L)"; break;
                case "METRO_CUBICO": texto = "Metro cúbico (m³)"; break; case "GALON_US": texto = "Galón estadounidense (gal)"; break;
                case "SEGUNDO": texto = "Segundo (s)"; break; case "MINUTO": texto = "Minuto (min)"; break; case "HORA": texto = "Hora (h)"; break; case "DIA": texto = "Día (d)"; break;
                case "METRO_CUADRADO": texto = "Metro cuadrado (m²)"; break; case "KILOMETRO_CUADRADO": texto = "Kilómetro cuadrado (km²)"; break;
                case "HECTAREA": texto = "Hectárea (ha)"; break; case "ACRE": texto = "Acre (ac)"; break;
                default: texto = id.Replace('_', ' '); break;
            }
            return new OpcionCatalogo(id, texto);
        }

        public override string ToString()
        {
            return Texto;
        }
    }
}
