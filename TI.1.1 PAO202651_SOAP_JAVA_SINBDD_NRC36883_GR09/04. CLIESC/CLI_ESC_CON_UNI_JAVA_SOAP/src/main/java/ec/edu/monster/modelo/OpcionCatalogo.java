package ec.edu.monster.modelo;

import java.util.Locale;

/** Identificador enviado al servicio y etiqueta legible para los selectores Swing. */
public record OpcionCatalogo(String id, String etiqueta) {
    public OpcionCatalogo {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El identificador del catálogo es obligatorio");
        }
        etiqueta = etiqueta == null || etiqueta.isBlank() ? formatear(id) : etiqueta;
    }

    public static OpcionCatalogo desde(String id) {
        return new OpcionCatalogo(id, formatear(id));
    }

    @Override
    public String toString() {
        return etiqueta;
    }

    private static String formatear(String valor) {
        String id = valor.trim().toUpperCase(Locale.ROOT);
        return switch (id) {
            case "LONGITUD" -> "Longitud";
            case "MASA" -> "Masa";
            case "TEMPERATURA" -> "Temperatura";
            case "VOLUMEN" -> "Volumen";
            case "TIEMPO" -> "Tiempo";
            case "AREA" -> "Área";
            case "KILOMETRO" -> "Kilómetro (km)";
            case "METRO" -> "Metro (m)";
            case "CENTIMETRO" -> "Centímetro (cm)";
            case "MILIMETRO" -> "Milímetro (mm)";
            case "MILLA" -> "Milla (mi)";
            case "PIE" -> "Pie (ft)";
            case "KILOGRAMO" -> "Kilogramo (kg)";
            case "GRAMO" -> "Gramo (g)";
            case "MILIGRAMO" -> "Miligramo (mg)";
            case "LIBRA" -> "Libra (lb)";
            case "CELSIUS" -> "Celsius (°C)";
            case "FAHRENHEIT" -> "Fahrenheit (°F)";
            case "KELVIN" -> "Kelvin (K)";
            case "MILILITRO" -> "Mililitro (mL)";
            case "LITRO" -> "Litro (L)";
            case "METRO_CUBICO" -> "Metro cúbico (m³)";
            case "GALON_US" -> "Galón estadounidense (gal)";
            case "SEGUNDO" -> "Segundo (s)";
            case "MINUTO" -> "Minuto (min)";
            case "HORA" -> "Hora (h)";
            case "DIA" -> "Día (d)";
            case "METRO_CUADRADO" -> "Metro cuadrado (m²)";
            case "KILOMETRO_CUADRADO" -> "Kilómetro cuadrado (km²)";
            case "HECTAREA" -> "Hectárea (ha)";
            case "ACRE" -> "Acre (ac)";
            default -> capitalizar(id.replace('_', ' ').toLowerCase(Locale.ROOT));
        };
    }

    private static String capitalizar(String texto) {
        if (texto.isEmpty()) return texto;
        return texto.substring(0, 1).toUpperCase(Locale.ROOT) + texto.substring(1);
    }
}
