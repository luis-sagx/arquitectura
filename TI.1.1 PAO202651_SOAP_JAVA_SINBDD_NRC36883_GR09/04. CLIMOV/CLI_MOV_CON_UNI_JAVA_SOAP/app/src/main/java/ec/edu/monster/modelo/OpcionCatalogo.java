package ec.edu.monster.modelo;

import java.util.Locale;

/** Identificador enviado al servicio y etiqueta legible para los selectores de Android. */
public final class OpcionCatalogo {
    private final String id;
    private final String etiqueta;

    public OpcionCatalogo(String id, String etiqueta) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del catálogo es obligatorio");
        }
        this.id = id;
        this.etiqueta = etiqueta == null || etiqueta.trim().isEmpty() ? formatear(id) : etiqueta;
    }

    public static OpcionCatalogo desde(String id) {
        return new OpcionCatalogo(id, formatear(id));
    }

    public String id() {
        return id;
    }

    public String etiqueta() {
        return etiqueta;
    }

    /** ArrayAdapter usa toString() para pintar cada opción. */
    @Override
    public String toString() {
        return etiqueta;
    }

    private static String formatear(String valor) {
        String id = valor.trim().toUpperCase(Locale.ROOT);
        switch (id) {
            case "LONGITUD": return "Longitud";
            case "MASA": return "Masa";
            case "TEMPERATURA": return "Temperatura";
            case "VOLUMEN": return "Volumen";
            case "TIEMPO": return "Tiempo";
            case "AREA": return "Área";
            case "KILOMETRO": return "Kilómetro (km)";
            case "METRO": return "Metro (m)";
            case "CENTIMETRO": return "Centímetro (cm)";
            case "MILIMETRO": return "Milímetro (mm)";
            case "MILLA": return "Milla (mi)";
            case "PIE": return "Pie (ft)";
            case "KILOGRAMO": return "Kilogramo (kg)";
            case "GRAMO": return "Gramo (g)";
            case "MILIGRAMO": return "Miligramo (mg)";
            case "LIBRA": return "Libra (lb)";
            case "CELSIUS": return "Celsius (°C)";
            case "FAHRENHEIT": return "Fahrenheit (°F)";
            case "KELVIN": return "Kelvin (K)";
            case "MILILITRO": return "Mililitro (mL)";
            case "LITRO": return "Litro (L)";
            case "METRO_CUBICO": return "Metro cúbico (m³)";
            case "GALON_US": return "Galón estadounidense (gal)";
            case "SEGUNDO": return "Segundo (s)";
            case "MINUTO": return "Minuto (min)";
            case "HORA": return "Hora (h)";
            case "DIA": return "Día (d)";
            case "METRO_CUADRADO": return "Metro cuadrado (m²)";
            case "KILOMETRO_CUADRADO": return "Kilómetro cuadrado (km²)";
            case "HECTAREA": return "Hectárea (ha)";
            case "ACRE": return "Acre (ac)";
            default: return capitalizar(id.replace('_', ' ').toLowerCase(Locale.ROOT));
        }
    }

    private static String capitalizar(String texto) {
        if (texto.isEmpty()) return texto;
        return texto.substring(0, 1).toUpperCase(Locale.ROOT) + texto.substring(1);
    }
}
