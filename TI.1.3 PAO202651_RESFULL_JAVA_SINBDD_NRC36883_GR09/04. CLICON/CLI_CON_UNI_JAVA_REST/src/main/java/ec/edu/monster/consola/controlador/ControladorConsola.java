package ec.edu.monster.consola.controlador;

import ec.edu.monster.cliente.ClienteRest;
import ec.edu.monster.consola.vista.VistaConsola;
import ec.edu.monster.modelo.RespuestaRest;
import java.io.IOException;
import java.util.List;

/** Coordina la navegación de consola y las llamadas al API REST. */
public final class ControladorConsola {
    private static final List<String> CATEGORIAS_CONVERTIBLES = List.of("LONGITUD", "MASA", "TEMPERATURA");

    private final ClienteRest cliente;
    private final VistaConsola vista;
    private String token;

    public ControladorConsola(ClienteRest cliente, VistaConsola vista) {
        this.cliente = cliente;
        this.vista = vista;
    }

    public void ejecutar() {
        vista.mostrarBienvenida();
        try {
            while (true) {
                vista.mostrarMenuPrincipal(token != null);
                String opcion = vista.leer("Opción: ");
                try {
                    switch (opcion) {
                        case "1" -> iniciarSesion();
                        case "2" -> { if (haySesion()) consultarCatalogo(); }
                        case "3" -> { if (haySesion()) convertir(); }
                        case "4" -> {
                            token = null;
                            vista.mostrarMensaje("Sesión local cerrada. Inicie sesión con la opción 1 para continuar.");
                        }
                        case "5" -> recuperarClave();
                        case "0" -> {
                            vista.mostrarMensaje("Hasta luego.");
                            return;
                        }
                        default -> vista.mostrarMensaje("Opción inválida. Escriba una opción del menú, del 0 al 5.");
                    }
                } catch (IOException e) {
                    vista.mostrarError(e.getMessage());
                    if (Thread.currentThread().isInterrupted()) return;
                }
            }
        } catch (VistaConsola.FinEntrada e) {
            vista.mostrarMensaje("\nHasta luego.");
        } catch (IOException e) {
            vista.mostrarError("de lectura: " + e.getMessage());
        }
    }

    private void iniciarSesion() throws IOException {
        vista.mostrarSeccion("=== INICIAR SESIÓN ===");
        token = null;
        String usuario = vista.leerLiteral("Usuario: ");
        String clave = vista.leerClave();
        RespuestaRest respuesta = cliente.autenticar(usuario, clave);
        if (mostrar(respuesta)) {
            if (respuesta.getToken() == null || respuesta.getToken().isBlank()) {
                throw new IOException("El servidor no entregó un token de sesión.");
            }
            token = respuesta.getToken();
            vista.mostrarMensaje("Sesión iniciada.");
            if (respuesta.getExpiraEn() != null && !respuesta.getExpiraEn().isBlank()) {
                vista.mostrarMensaje("Sesión vigente hasta: " + respuesta.getExpiraEn());
            }
        }
    }

    private void recuperarClave() throws IOException {
        vista.mostrarSeccion("=== RECUPERAR CONTRASEÑA ===");
        String usuario = vista.leerLiteral("Usuario: ");
        String nuevaClave = vista.leerClave("Nueva clave: ");
        if (mostrar(cliente.recuperarClave(usuario, nuevaClave))) token = null;
    }

    private boolean haySesion() {
        if (token != null) return true;
        vista.mostrarMensaje("Inicie sesión con la opción 1 para continuar.");
        return false;
    }

    private void consultarCatalogo() throws IOException {
        vista.mostrarSeccion("=== CONSULTAR CATÁLOGO ===");
        RespuestaRest categorias = cliente.obtenerCategorias(token);
        if (!mostrar(categorias)) return;
        String categoria = vista.seleccionar("Categoría", categorias.getCategorias());
        if (categoria == null) return;
        RespuestaRest unidades = cliente.obtenerUnidades(token, categoria);
        if (mostrar(unidades)) vista.mostrarUnidades(unidades.getUnidades());
    }

    private void convertir() throws IOException {
        vista.mostrarSeccion("=== CONVERTIR UNIDADES ===");
        RespuestaRest categorias = cliente.obtenerCategorias(token);
        if (!mostrar(categorias)) return;
        List<String> convertibles = categorias.getCategorias().stream()
            .filter(CATEGORIAS_CONVERTIBLES::contains).toList();
        String categoria = vista.seleccionar("Categoría a convertir", convertibles);
        if (categoria == null) return;
        RespuestaRest unidades = cliente.obtenerUnidades(token, categoria);
        if (!mostrar(unidades)) return;
        String origen = vista.seleccionar("Unidad de origen", unidades.getUnidades());
        if (origen == null) return;
        String destino = vista.seleccionar("Unidad de destino", unidades.getUnidades());
        if (destino == null) return;
        double valor = vista.leerNumero();
        RespuestaRest respuesta = cliente.convertir(token, categoria, origen, destino, valor);
        if (mostrar(respuesta)) {
            if (respuesta.getResultado() == null) {
                throw new IOException("El servidor no entregó el resultado de conversión.");
            }
            String unidadDestino = respuesta.getUnidadDestino() == null
                ? destino : respuesta.getUnidadDestino();
            vista.mostrarConversion(valor, origen, respuesta.getResultado(), unidadDestino);
        }
    }

    private boolean mostrar(RespuestaRest respuesta) {
        vista.mostrarRespuesta(respuesta);
        if (respuesta.isSesionInvalida()) {
            token = null;
            vista.mostrarMensaje("La sesión dejó de ser válida. Inicie sesión nuevamente.");
        }
        return respuesta.isExito();
    }
}
