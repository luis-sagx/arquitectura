package ec.edu.monster.consola.controlador;

import ec.edu.monster.cliente.ws.ClienteSoap;
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
        try {
            while (true) {
                vista.mostrarMenuPrincipal(token != null);
                String opcion = vista.leer("Opción: ");
                boolean esperar = true;
                try {
                    switch (opcion) {
                        case "1" -> iniciarSesion();
                        case "2" -> { if (haySesion()) esperar = consultarCatalogo(); }
                        case "3" -> { if (haySesion()) esperar = convertir(); }
                        case "4" -> { token = null; vista.mostrarMensaje("Sesion local cerrada. Inicie sesion con la opcion 1 para continuar."); }
                        case "5" -> recuperarClave();
                        case "0" -> { vista.mostrarMensaje("Hasta luego."); return; }
                        default -> vista.mostrarMensaje("Opcion invalida. Escribe una opcion del menu, del 0 al 5.");
                    }
                } catch (IOException e) {
                    vista.mostrarError(e.getMessage());
                    if (Thread.currentThread().isInterrupted()) return;
                }
                if (esperar) vista.esperarParaContinuar();
            }
        } catch (VistaConsola.FinEntrada e) {
            vista.mostrarMensaje("\nHasta luego.");
        } catch (IOException e) {
            vista.mostrarError("de lectura: " + e.getMessage());
        }
    }

    private void iniciarSesion() throws IOException {
        vista.mostrarSeccion("INICIAR SESIÓN");
        token = null;
        String usuario = vista.leerLiteral("Usuario: ");
        String clave = vista.leerClave();
        RespuestaSoap respuesta = cliente.autenticar(usuario, clave);
        if (mostrar(respuesta)) {
            if (respuesta.getToken() == null || respuesta.getToken().isBlank()) throw new IOException("El servidor no entrego un token");
            token = respuesta.getToken();
            vista.mostrarMensaje("Sesión iniciada.");
            if (!respuesta.getExpiraEn().isBlank()) vista.mostrarMensaje("Sesion vigente hasta: " + respuesta.getExpiraEn());
        }
    }

    private void recuperarClave() throws IOException {
        vista.mostrarSeccion("RECUPERAR CONTRASEÑA");
        String usuario = vista.leerLiteral("Usuario: ");
        String nuevaClave = vista.leerClave("Nueva clave: ");
        if (mostrar(cliente.recuperarClave(usuario, nuevaClave))) token = null;
    }
    private boolean haySesion() {
        if (token != null) return true;
        vista.mostrarMensaje("Inicie sesion con la opcion 1 para continuar.");
        return false;
    }

    private boolean consultarCatalogo() throws IOException {
        vista.mostrarSeccion("CATÁLOGO DE UNIDADES");
        RespuestaSoap categorias = cliente.obtenerCategorias(token);
        if (!mostrar(categorias)) return true;
        for (String categoria : categorias.getCategorias()) {
            RespuestaSoap unidades = cliente.obtenerUnidades(token, categoria);
            if (!mostrar(unidades)) return true;
            vista.mostrarCategoriaCatalogo(categoria, unidades.getUnidades(),
                List.of("LONGITUD", "MASA", "TEMPERATURA").contains(categoria));
        }
        return true;
    }

    private boolean convertir() throws IOException {
        vista.mostrarPasoConversion(1, null, null, null);
        RespuestaSoap categorias = cliente.obtenerCategorias(token);
        if (!mostrar(categorias)) return true;
        List<String> convertibles = categorias.getCategorias().stream()
            .filter(c -> List.of("LONGITUD", "MASA", "TEMPERATURA").contains(c)).toList();
        String categoria = vista.seleccionar("Categoria a convertir", convertibles);
        if (categoria == null) return false;
        vista.mostrarPasoConversion(2, categoria, null, null);
        RespuestaSoap unidades = cliente.obtenerUnidades(token, categoria);
        if (!mostrar(unidades)) return true;
        String origen = vista.seleccionar("Unidad de origen", unidades.getUnidades());
        if (origen == null) return false;
        vista.mostrarPasoConversion(3, categoria, origen, null);
        String destino = vista.seleccionar("Unidad de destino", unidades.getUnidades());
        if (destino == null) return false;
        vista.mostrarPasoConversion(4, categoria, origen, destino);
        double valor = vista.leerNumero();
        RespuestaSoap respuesta = cliente.convertir(token, categoria, origen, destino, valor);
        if (mostrar(respuesta)) {
            if (respuesta.getResultado() == null) throw new IOException("El servidor no entrego el resultado de conversion");
            vista.mostrarSeccion("RESULTADO DE CONVERSIÓN | " + categoria);
            vista.mostrarConversion(valor, origen, respuesta.getResultado(), respuesta.getUnidadDestino());
        }
        return true;
    }

    private boolean mostrar(RespuestaSoap respuesta) {
        vista.mostrarRespuesta(respuesta);
        if (respuesta.isSesionInvalida()) {
            token = null;
            vista.mostrarMensaje("Inicie sesion nuevamente.");
        }
        return respuesta.isExito();
    }
}
