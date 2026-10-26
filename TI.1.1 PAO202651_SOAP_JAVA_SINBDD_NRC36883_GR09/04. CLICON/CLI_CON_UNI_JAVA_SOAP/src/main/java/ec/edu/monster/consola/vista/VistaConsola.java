package ec.edu.monster.consola.vista;

import ec.edu.monster.modelo.RespuestaSoap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

/** Presenta la interfaz de texto y recoge las entradas del usuario. */
public final class VistaConsola {
    private static final String SEPARADOR = "------------------------------------------------------------";
    private final BufferedReader entrada;
    private final PrintWriter salida;
    private final boolean interactiva;

    public VistaConsola(BufferedReader entrada, PrintWriter salida) {
        this(entrada, salida, false);
    }

    public VistaConsola(BufferedReader entrada, PrintWriter salida, boolean interactiva) {
        this.entrada = entrada;
        this.salida = salida;
        this.interactiva = interactiva;
    }

    public void mostrarMenuPrincipal(boolean sesionIniciada) {
        imprimirSeccion("MENÚ PRINCIPAL");
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

    public void mostrarPasoConversion(int paso, String categoria, String origen, String destino) {
        String[] etapas = { "Categoría", "Unidad de origen", "Unidad de destino", "Cantidad" };
        imprimirSeccion("CONVERTIR UNIDADES | Paso " + paso + "/4: " + etapas[paso - 1]);
        if (categoria != null) salida.println("Categoría: " + categoria);
        if (origen != null) salida.println("Origen:    " + origen);
        if (destino != null) salida.println("Destino:   " + destino);
        salida.println();
    }

    public void esperarParaContinuar() throws IOException {
        if (!interactiva) return;
        salida.println();
        salida.println(SEPARADOR);
        leerLiteral("Presiona Enter para volver al menú principal...");
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
        if (interactiva && System.console() != null) {
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
        salida.println();
        salida.println(titulo + ":");
        salida.println();
        for (int i = 0; i < opciones.size(); i++) salida.println("  " + (i + 1) + ". " + opciones.get(i));
        salida.println("  0. Volver al menú principal");
        salida.println();
        while (true) {
            try {
                int opcion = Integer.parseInt(leer("Opción (0-" + opciones.size() + "): "));
                if (opcion == 0) return null;
                if (opcion >= 1 && opcion <= opciones.size()) return opciones.get(opcion - 1);
            } catch (NumberFormatException e) {
                // Volver a solicitar la opción.
            }
            salida.println("Opción inválida. Selecciona una de las unidades u opciones de la lista.");
            salida.println();
        }
    }

    public double leerNumero() throws IOException {
        salida.println("Ingresa la cantidad que deseas convertir.");
        salida.println();
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
        salida.println();
        salida.println("Error: " + mensaje);
    }

    public void mostrarMensaje(String mensaje) {
        salida.println(mensaje);
    }

    public void mostrarUnidades(List<String> unidades) {
        salida.println();
        salida.println("Unidades disponibles:");
        salida.println();
        for (String unidad : unidades) salida.println("- " + medida(unidad));
    }

    public void mostrarCategoriaCatalogo(String categoria, List<String> unidades, boolean convertible) {
        salida.println();
        salida.println(titulo(categoria) + (convertible ? " (conversión disponible)" : " (solo consulta)"));
        for (String unidad : unidades) salida.println("  - " + medida(unidad));
    }

    private String titulo(String id) {
        return switch (id) { case "MASA" -> "Masa/peso"; case "AREA" -> "Área";
            default -> id.substring(0, 1) + id.substring(1).toLowerCase(Locale.ROOT); };
    }

    private String medida(String id) {
        return switch (id) { case "KILOMETRO" -> "Kilómetro (km)"; case "METRO" -> "Metro (m)";
            case "CENTIMETRO" -> "Centímetro (cm)"; case "MILIMETRO" -> "Milímetro (mm)";
            case "MILLA" -> "Milla (mi)"; case "PIE" -> "Pie (ft)"; case "KILOGRAMO" -> "Kilogramo (kg)";
            case "GRAMO" -> "Gramo (g)"; case "MILIGRAMO" -> "Miligramo (mg)"; case "LIBRA" -> "Libra (lb)";
            case "CELSIUS" -> "Celsius (°C)"; case "FAHRENHEIT" -> "Fahrenheit (°F)"; case "KELVIN" -> "Kelvin (K)";
            case "MILILITRO" -> "Mililitro (mL)"; case "LITRO" -> "Litro (L)"; case "METRO_CUBICO" -> "Metro cúbico (m³)";
            case "GALON_US" -> "Galón estadounidense (gal)"; case "SEGUNDO" -> "Segundo (s)"; case "MINUTO" -> "Minuto (min)";
            case "HORA" -> "Hora (h)"; case "DIA" -> "Día (d)"; case "METRO_CUADRADO" -> "Metro cuadrado (m²)";
            case "KILOMETRO_CUADRADO" -> "Kilómetro cuadrado (km²)"; case "HECTAREA" -> "Hectárea (ha)"; case "ACRE" -> "Acre (ac)";
            default -> id; };
    }

    public void mostrarConversion(double valor, String origen, Double resultado, String destino) {
        salida.println();
        salida.println("Conversión completada:");
        salida.println(SEPARADOR);
        salida.printf(Locale.ROOT, "    %.4f %s = %.4f %s%n", valor, origen, resultado, destino);
        salida.println(SEPARADOR);
    }

    private void imprimirSeccion(String titulo) {
        limpiarPantalla();
        salida.println();
        salida.println("CONVERTIDOR DE UNIDADES");
        salida.println(SEPARADOR);
        salida.println(titulo);
        salida.println(SEPARADOR);
        salida.println();
    }

    private void limpiarPantalla() {
        if (!interactiva) return;
        if (System.console() != null) {
            if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows")) {
                salida.flush();
                try {
                    if (new ProcessBuilder("cmd.exe", "/c", "cls").inheritIO().start().waitFor() == 0) return;
                } catch (IOException e) {
                    // Separar con espacios cuando la terminal no permite limpiar.
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                salida.print("\033[H\033[2J");
                salida.flush();
                return;
            }
        }
        // La salida del IDE conserva su historial: desplazarlo deja visible la etapa actual.
        salida.print("\n".repeat(24));
        salida.flush();
    }

    public static final class FinEntrada extends RuntimeException { }
}
