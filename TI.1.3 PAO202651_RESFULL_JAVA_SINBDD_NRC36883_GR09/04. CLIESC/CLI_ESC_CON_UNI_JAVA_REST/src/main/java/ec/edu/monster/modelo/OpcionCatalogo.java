package ec.edu.monster.modelo;

import java.util.Locale;

public record OpcionCatalogo(String id, String etiqueta) {
    public OpcionCatalogo {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("El identificador del catálogo es obligatorio");
        etiqueta = etiqueta == null || etiqueta.isBlank() ? formatear(id) : etiqueta;
    }

    public static OpcionCatalogo desde(String id) { return new OpcionCatalogo(id, formatear(id)); }
    @Override public String toString() { return etiqueta; }

    private static String formatear(String valor) {
        String id = valor.trim().toUpperCase(Locale.ROOT);
        return switch (id) {
            case "LONGITUD" -> "Longitud";
            case "MASA" -> "Masa";
            case "TEMPERATURA" -> "Temperatura";
            case "KILOMETRO" -> "Kilómetro";
            case "METRO" -> "Metro";
            case "CENTIMETRO" -> "Centímetro";
            case "MILIMETRO" -> "Milímetro";
            case "MILLA" -> "Milla";
            case "PIE" -> "Pie";
            case "KILOGRAMO" -> "Kilogramo";
            case "GRAMO" -> "Gramo";
            case "MILIGRAMO" -> "Miligramo";
            case "LIBRA" -> "Libra";
            case "CELSIUS" -> "Celsius";
            case "FAHRENHEIT" -> "Fahrenheit";
            case "KELVIN" -> "Kelvin";
            default -> capitalizar(id.replace('_', ' ').toLowerCase(Locale.ROOT));
        };
    }

    private static String capitalizar(String texto) {
        return texto.isEmpty() ? texto : texto.substring(0, 1).toUpperCase(Locale.ROOT) + texto.substring(1);
    }
}
