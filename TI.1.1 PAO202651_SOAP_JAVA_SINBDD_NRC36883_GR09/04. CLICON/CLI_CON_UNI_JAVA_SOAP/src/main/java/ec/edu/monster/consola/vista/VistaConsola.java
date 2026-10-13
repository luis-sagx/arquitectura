package ec.edu.monster.consola.vista;

import ec.edu.monster.modelo.RespuestaSoap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

/** Presenta la interfaz de texto y recoge las entradas del usuario. */
public final class VistaConsola {
    private static final String SEPARADOR = "----------------------------------------";
    private final BufferedReader entrada;
    private final PrintWriter salida;

    public VistaConsola(BufferedReader entrada, PrintWriter salida) {
        this.entrada = entrada;
        this.salida = salida;
    }

    public void mostrarBienvenida() {
        salida.println("CONVERTIDOR DE UNIDADES");
    }

    public void mostrarMenuPrincipal(boolean sesionIniciada) {
        imprimirSeccion("=== MENÚ PRINCIPAL ===");
        salida.println("Sesión: " + (sesionIniciada ? "iniciada" : "no iniciada"));
        salida.println();
        salida.println("Acciones:");
        salida.println("  1. Iniciar sesión");
        salida.println("  2. Consultar catálogo");
        salida.println("  3. Convertir unidades");
        salida.println("  4. Cerrar sesión local");
        salida.println("  5. Recuperar contraseña");
        salida.println("  0. Salir");
        salida.println(SEPARADOR);
    }

    public void mostrarSeccion(String titulo) {
        imprimirSeccion(titulo);
    }

    public String leer(String mensaje) throws IOException {
        return leerLiteral(mensaje).trim();
    }

    public String leerLiteral(String mensaje) throws IOException {
        salida.print(mensaje);
        salida.flush();
        String linea = entrada.readLine();
        if (linea == null) throw new FinEntrada();
        return linea;
    }

    public String leerClave() throws IOException {
        return leerClave("Clave: ");
    }

    public String leerClave(String mensaje) throws IOException {
        if (System.console() != null) {
            char[] caracteres = System.console().readPassword(mensaje);
            if (caracteres == null) throw new FinEntrada();
            String clave = new String(caracteres);
            java.util.Arrays.fill(caracteres, '\0');
            return clave;
        }
        return leerLiteral(mensaje);
    }

    public String seleccionar(String titulo, List<String> opciones) throws IOException {
        if (opciones.isEmpty()) {
            salida.println("No hay opciones disponibles.");
            return null;
        }
        salida.println(titulo + ":");
        for (int i = 0; i < opciones.size(); i++) salida.println("  " + (i + 1) + ". " + opciones.get(i));
        salida.println("  0. Volver al menú principal");
        while (true) {
            try {
                int opcion = Integer.parseInt(leer("Seleccione una opción: "));
                if (opcion == 0) return null;
                if (opcion >= 1 && opcion <= opciones.size()) return opciones.get(opcion - 1);
            } catch (NumberFormatException e) {
                // Volver a solicitar la opción.
            }
            salida.println("Opcion invalida.");
        }
    }

    public double leerNumero() throws IOException {
        while (true) {
            try {
                double valor = Double.parseDouble(leer("Valor: ").replace(',', '.'));
                if (Double.isFinite(valor)) return valor;
            } catch (NumberFormatException e) {
                // Volver a solicitar un número.
            }
            salida.println("Ingrese un numero finito; puede usar punto o coma decimal.");
        }
    }

    public void mostrarRespuesta(RespuestaSoap respuesta) {
        salida.println();
        salida.println((respuesta.isExito() ? "" : "[ERROR] ") + respuesta.getCodigo() + ": " + respuesta.getMensaje());
    }

    public void mostrarError(String mensaje) {
        salida.println("Error: " + mensaje);
    }

    public void mostrarMensaje(String mensaje) {
        salida.println(mensaje);
    }

    public void mostrarUnidades(List<String> unidades) {
        for (String unidad : unidades) salida.println("- " + unidad);
    }

    public void mostrarConversion(double valor, String origen, Double resultado, String destino) {
        salida.println();
        salida.println("Conversión completada:");
        salida.println(SEPARADOR);
        salida.printf(Locale.ROOT, "    %.4f %s = %.4f %s%n", valor, origen, resultado, destino);
        salida.println(SEPARADOR);
    }

    private void imprimirSeccion(String titulo) {
        salida.println();
        salida.println(SEPARADOR);
        salida.println(titulo);
        salida.println(SEPARADOR);
    }

    public static final class FinEntrada extends RuntimeException { }
}
