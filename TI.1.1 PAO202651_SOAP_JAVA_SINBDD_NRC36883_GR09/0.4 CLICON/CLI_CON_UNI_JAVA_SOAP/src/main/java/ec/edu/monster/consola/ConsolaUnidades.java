package ec.edu.monster.consola;

import ec.edu.monster.cliente.*;
import java.io.*;
import java.util.List;

public final class ConsolaUnidades {
    private static final String SEPARADOR = "----------------------------------------";

    private void imprimirSeccion(String titulo) {
        salida.println();
        salida.println(SEPARADOR);
        salida.println(titulo);
        salida.println(SEPARADOR);
    }
    private final ClienteSoap cliente;
    private final BufferedReader entrada;
    private final PrintWriter salida;
    private String token;
    public ConsolaUnidades(ClienteSoap cliente, BufferedReader entrada, PrintWriter salida) {
        this.cliente = cliente; this.entrada = entrada; this.salida = salida;
    }
    public void ejecutar() {
        salida.println("CONVERTIDOR DE UNIDADES");
        try {
            while (true) {
                imprimirSeccion("=== MENÚ PRINCIPAL ===");
                salida.println("Sesión: " + (token == null ? "no iniciada" : "iniciada"));
                salida.println();
                salida.println("Acciones:");
                salida.println("  1. Iniciar sesión");
                salida.println("  2. Consultar catálogo");
                salida.println("  3. Convertir unidades");
                salida.println("  4. Cerrar sesión local");
                salida.println("  0. Salir");
                salida.println(SEPARADOR);
                String opcion = leer("Opción: ");
                try {
                    switch (opcion) {
                        case "1" -> iniciarSesion();
                        case "2" -> { if (haySesion()) consultarCatalogo(); }
                        case "3" -> { if (haySesion()) convertir(); }
                        case "4" -> { token = null; salida.println("Sesion local cerrada. Inicie sesion con la opcion 1 para continuar."); }
                        case "0" -> { salida.println("Hasta luego."); return; }
                        default -> salida.println("Opcion invalida. Escribe una opcion del menu, del 0 al 4.");
                    }
                } catch (IOException e) {
                    salida.println("Error: " + e.getMessage());
                    if (Thread.currentThread().isInterrupted()) return;
                }
            }
        } catch (FinEntrada e) { salida.println("\nHasta luego."); }
        catch (IOException e) { salida.println("Error de lectura: " + e.getMessage()); }
    }
    private void iniciarSesion() throws IOException {
        imprimirSeccion("=== INICIAR SESIÓN ===");
        token = null;
        String usuario = leerLiteral("Usuario: ");
        String clave;
        if (System.console() != null) {
            char[] caracteres = System.console().readPassword("Clave: ");
            if (caracteres == null) throw new FinEntrada();
            clave = new String(caracteres); java.util.Arrays.fill(caracteres, '\0');
        } else clave = leerLiteral("Clave: ");
        var r = cliente.autenticar(usuario, clave);
        if (mostrar(r)) {
            if (r.token() == null || r.token().isBlank()) throw new IOException("El servidor no entrego un token");
            token = r.token();
            salida.println("Sesión iniciada.");
            if (!r.expiraEn().isBlank()) salida.println("Sesion vigente hasta: " + r.expiraEn());
        }
    }
    private boolean haySesion() {
        if (token != null) return true;
        salida.println("Inicie sesion con la opcion 1 para continuar.");
        return false;
    }
    private void consultarCatalogo() throws IOException {
        imprimirSeccion("=== CONSULTAR CATÁLOGO ===");
        var categorias = cliente.obtenerCategorias(token);
        if (!mostrar(categorias)) return;
        String categoria = elegir("Categoria", categorias.categorias());
        if (categoria == null) return;
        var unidades = cliente.obtenerUnidades(token, categoria);
        if (mostrar(unidades)) for (String unidad : unidades.unidades()) salida.println("- " + unidad);
    }
    private void convertir() throws IOException {
        imprimirSeccion("=== CONVERTIR UNIDADES ===");
        var categorias = cliente.obtenerCategorias(token);
        if (!mostrar(categorias)) return;
        List<String> convertibles = categorias.categorias().stream()
            .filter(c -> List.of("LONGITUD", "MASA", "TEMPERATURA").contains(c)).toList();
        String categoria = elegir("Categoria a convertir", convertibles);
        if (categoria == null) return;
        var unidades = cliente.obtenerUnidades(token, categoria);
        if (!mostrar(unidades)) return;
        String origen = elegir("Unidad de origen", unidades.unidades());
        if (origen == null) return;
        String destino = elegir("Unidad de destino", unidades.unidades());
        if (destino == null) return;
        double valor = leerNumero();
        var r = cliente.convertir(token, categoria, origen, destino, valor);
        if (mostrar(r)) {
            if (r.resultado() == null) throw new IOException("El servidor no entrego el resultado de conversion");
            salida.println();
            salida.println("Conversión completada:");
            salida.println("    " + valor + " " + origen + " = " + r.resultado() + " " + r.unidadDestino());
        }
    }
    private boolean mostrar(RespuestaSoap r) {
        salida.println();
        salida.println((r.exito() ? "" : "[ERROR] ") + r.codigo() + ": " + r.mensaje());
        if (r.sesionInvalida()) { token = null; salida.println("Inicie sesion nuevamente."); }
        return r.exito();
    }
    private String elegir(String titulo, List<String> opciones) throws IOException {
        if (opciones.isEmpty()) { salida.println("No hay opciones disponibles."); return null; }
        salida.println(titulo + ":");
        for (int i = 0; i < opciones.size(); i++) salida.println("  " + (i + 1) + ". " + opciones.get(i));
        salida.println("  0. Volver al menú principal");
        while (true) {
            try {
                int opcion = Integer.parseInt(leer("Seleccione una opción: "));
                if (opcion == 0) return null;
                if (opcion >= 1 && opcion <= opciones.size()) return opciones.get(opcion - 1);
            } catch (NumberFormatException e) { /* Solicitar otra opcion. */ }
            salida.println("Opcion invalida.");
        }
    }
    private double leerNumero() throws IOException {
        while (true) {
            try {
                double valor = Double.parseDouble(leer("Valor: ").replace(',', '.'));
                if (Double.isFinite(valor)) return valor;
            } catch (NumberFormatException e) { /* Solicitar un numero valido. */ }
            salida.println("Ingrese un numero finito; puede usar punto o coma decimal.");
        }
    }
    private String leer(String mensaje) throws IOException {
        return leerLiteral(mensaje).trim();
    }
    private String leerLiteral(String mensaje) throws IOException {
        salida.print(mensaje); salida.flush();
        String linea = entrada.readLine();
        if (linea == null) throw new FinEntrada();
        return linea;
    }
    private static final class FinEntrada extends RuntimeException { }
}
