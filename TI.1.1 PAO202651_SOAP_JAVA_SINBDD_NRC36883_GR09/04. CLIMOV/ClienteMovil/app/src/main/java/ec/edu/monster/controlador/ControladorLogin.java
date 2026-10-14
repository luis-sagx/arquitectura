package ec.edu.monster.controlador;

import ec.edu.monster.modelo.RespuestaSoap;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.servicio.ClienteSoap;
import ec.edu.monster.servicio.EjecutorTareas;
import ec.edu.monster.vista.VistaLogin;
import java.io.IOException;

/** Controla el acceso y la recuperación sin mezclar reglas con la vista. */
public final class ControladorLogin {
    private final VistaLogin vista;
    private final EjecutorTareas ejecutor;
    private final String endpoint;

    public ControladorLogin(VistaLogin vista, EjecutorTareas ejecutor, String endpoint) {
        this.vista = vista;
        this.ejecutor = ejecutor;
        this.endpoint = endpoint;
    }

    public void ingresar() {
        String usuario = vista.usuarioIngresado();
        String clave = vista.claveIngresada();
        if (usuario.trim().isEmpty() || clave.isEmpty()) {
            vista.mostrarMensaje("Escribe el usuario y la contraseña.", true);
            return;
        }
        final ClienteSoap cliente = crearCliente();
        if (cliente == null) return;

        vista.setOcupado(true);
        vista.mostrarMensaje("Conectando con el servicio…", false);
        ejecutor.ejecutar(() -> cliente.autenticar(usuario, clave), new EjecutorTareas.Resultado<RespuestaSoap>() {
            @Override
            public void exito(RespuestaSoap respuesta) {
                vista.setOcupado(false);
                vista.limpiarClave();
                if (!respuesta.isExito() || respuesta.token().trim().isEmpty()) {
                    vista.mostrarMensaje(mensajeRespuesta(respuesta), true);
                    return;
                }
                vista.mostrarMensaje(" ", false);
                vista.abrirConversiones(cliente.endpoint(), new Sesion(respuesta.token(), respuesta.expiraEn()));
            }

            @Override
            public void error(IOException ex) {
                vista.setOcupado(false);
                vista.limpiarClave();
                vista.mostrarMensaje(mensajeError(ex), true);
            }
        });
    }

    public void solicitarRecuperacion() {
        String usuario = vista.usuarioIngresado();
        if (usuario.trim().isEmpty()) {
            vista.mostrarMensaje("Escribe tu usuario antes de recuperar la contraseña.", true);
            return;
        }
        vista.pedirNuevaClave(usuario);
    }

    public void recuperarClave(String nuevaClave, String confirmacion) {
        String usuario = vista.usuarioIngresado();
        if (usuario.trim().isEmpty()) {
            vista.mostrarMensaje("Escribe tu usuario antes de recuperar la contraseña.", true);
            return;
        }
        if (nuevaClave == null || nuevaClave.isEmpty()) {
            vista.mostrarMensaje("La nueva contraseña no puede estar vacía.", true);
            return;
        }
        if (!nuevaClave.equals(confirmacion)) {
            vista.mostrarMensaje("Las contraseñas no coinciden.", true);
            return;
        }
        final ClienteSoap cliente = crearCliente();
        if (cliente == null) return;

        vista.setOcupado(true);
        vista.mostrarMensaje("Actualizando contraseña…", false);
        ejecutor.ejecutar(() -> cliente.recuperarClave(usuario, nuevaClave),
                new EjecutorTareas.Resultado<RespuestaSoap>() {
            @Override
            public void exito(RespuestaSoap respuesta) {
                vista.setOcupado(false);
                vista.mostrarMensaje(respuesta.isExito() ? respuesta.mensaje() : mensajeRespuesta(respuesta),
                        !respuesta.isExito());
            }

            @Override
            public void error(IOException ex) {
                vista.setOcupado(false);
                vista.mostrarMensaje(mensajeError(ex), true);
            }
        });
    }

    public void liberar() {
        ejecutor.cerrar();
    }

    private ClienteSoap crearCliente() {
        try {
            return new ClienteSoap(endpoint);
        } catch (IllegalArgumentException ex) {
            vista.mostrarMensaje(ex.getMessage(), true);
            return null;
        }
    }

    static String mensajeError(IOException ex) {
        return ex.getMessage() != null ? ex.getMessage()
                : "No se pudo completar la operación. Inténtalo nuevamente.";
    }

    private static String mensajeRespuesta(RespuestaSoap respuesta) {
        return respuesta.codigo() + " · " + respuesta.mensaje();
    }
}
