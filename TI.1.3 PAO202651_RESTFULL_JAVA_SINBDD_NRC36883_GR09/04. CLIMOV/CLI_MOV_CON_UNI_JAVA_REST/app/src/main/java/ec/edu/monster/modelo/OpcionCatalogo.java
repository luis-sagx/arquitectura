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
            case "KILOMETRO": return "Kilómetro";
            case "METRO": return "Metro";
            case "CENTIMETRO": return "Centímetro";
            case "MILIMETRO": return "Milímetro";
            case "MILLA": return "Milla";
            case "PIE": return "Pie";
            case "KILOGRAMO": return "Kilogramo";
            case "GRAMO": return "Gramo";
            case "MILIGRAMO": return "Miligramo";
            case "LIBRA": return "Libra";
            case "CELSIUS": return "Celsius";
            case "FAHRENHEIT": return "Fahrenheit";
            case "KELVIN": return "Kelvin";
            default: return capitalizar(id.replace('_', ' ').toLowerCase(Locale.ROOT));
        }
    }

    private static String capitalizar(String texto) {
        if (texto.isEmpty()) return texto;
        return texto.substring(0, 1).toUpperCase(Locale.ROOT) + texto.substring(1);
    }
}
