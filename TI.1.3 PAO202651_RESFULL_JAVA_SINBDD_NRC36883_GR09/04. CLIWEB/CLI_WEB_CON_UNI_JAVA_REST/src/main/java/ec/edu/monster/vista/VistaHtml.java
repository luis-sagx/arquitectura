package ec.edu.monster.vista;

/** Funciones de escape usadas por las vistas JSP. */
public final class VistaHtml {
    private VistaHtml() { }

    public static String escapar(String valor) {
        if (valor == null) return "";
        return valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
