package ec.edu.monster.consola.controlador;

import ec.edu.monster.cliente.ClienteSoap;
import ec.edu.monster.consola.vista.VistaConsola;
import ec.edu.monster.modelo.RespuestaSoap;
import java.io.IOException;
import java.util.List;

/** Coordina la navegación de consola y las llamadas al servicio SOAP. */
public final class ControladorConsola {
    private final ClienteSoap cliente;
    private final VistaConsola vista;
    private String token;

    public ControladorConsola(ClienteSoap cliente, VistaConsola vista) {
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
                        case "4" -> { token = null; vista.mostrarMensaje("Sesion local cerrada. Inicie sesion con la opcion 1 para continuar."); }
                        case "5" -> recuperarClave();
                        case "0" -> { vista.mostrarMensaje("Hasta luego."); return; }
                        default -> vista.mostrarMensaje("Opcion invalida. Escribe una opcion del menu, del 0 al 5.");
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
        RespuestaSoap respuesta = cliente.autenticar(usuario, clave);
        if (mostrar(respuesta)) {
            if (respuesta.token() == null || respuesta.token().isBlank()) throw new IOException("El servidor no entrego un token");
            token = respuesta.token();
            vista.mostrarMensaje("Sesión iniciada.");
            if (!respuesta.expiraEn().isBlank()) vista.mostrarMensaje("Sesion vigente hasta: " + respuesta.expiraEn());
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
        vista.mostrarMensaje("Inicie sesion con la opcion 1 para continuar.");
        return false;
    }

    private void consultarCatalogo() throws IOException {
        vista.mostrarSeccion("=== CONSULTAR CATÁLOGO ===");
        RespuestaSoap categorias = cliente.obtenerCategorias(token);
        if (!mostrar(categorias)) return;
        String categoria = vista.seleccionar("Categoria", categorias.categorias());
        if (categoria == null) return;
        RespuestaSoap unidades = cliente.obtenerUnidades(token, categoria);
        if (mostrar(unidades)) vista.mostrarUnidades(unidades.unidades());
    }

    private void convertir() throws IOException {
        vista.mostrarSeccion("=== CONVERTIR UNIDADES ===");
        RespuestaSoap categorias = cliente.obtenerCategorias(token);
        if (!mostrar(categorias)) return;
        List<String> convertibles = categorias.categorias().stream()
            .filter(c -> List.of("LONGITUD", "MASA", "TEMPERATURA").contains(c)).toList();
        String categoria = vista.seleccionar("Categoria a convertir", convertibles);
        if (categoria == null) return;
        RespuestaSoap unidades = cliente.obtenerUnidades(token, categoria);
        if (!mostrar(unidades)) return;
        String origen = vista.seleccionar("Unidad de origen", unidades.unidades());
        if (origen == null) return;
        String destino = vista.seleccionar("Unidad de destino", unidades.unidades());
        if (destino == null) return;
        double valor = vista.leerNumero();
        RespuestaSoap respuesta = cliente.convertir(token, categoria, origen, destino, valor);
        if (mostrar(respuesta)) {
            if (respuesta.resultado() == null) throw new IOException("El servidor no entrego el resultado de conversion");
            vista.mostrarConversion(valor, origen, respuesta.resultado(), respuesta.unidadDestino());
        }
    }

    private boolean mostrar(RespuestaSoap respuesta) {
        vista.mostrarRespuesta(respuesta);
        if (respuesta.sesionInvalida()) {
            token = null;
            vista.mostrarMensaje("Inicie sesion nuevamente.");
        }
        return respuesta.exito();
    }
}
