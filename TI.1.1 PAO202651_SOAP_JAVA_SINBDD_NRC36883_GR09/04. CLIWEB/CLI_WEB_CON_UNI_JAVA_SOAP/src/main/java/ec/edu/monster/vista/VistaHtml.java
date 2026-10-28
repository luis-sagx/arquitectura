package ec.edu.monster.vista;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/** Funciones compartidas de presentación para las vistas JSP. */
public final class VistaHtml {
    private VistaHtml() { }

    public static String escapar(String valor) {
        if (valor == null) return "";
        return valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    public static String formatearResultado(double numero) {
        NumberFormat formato = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-EC"));
        formato.setMaximumFractionDigits(4);
        formato.setMinimumFractionDigits(0);
        formato.setRoundingMode(RoundingMode.HALF_UP);
        return formato.format(numero);
    }
}
