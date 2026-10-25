package ec.edu.monster.controlador;

import ec.edu.monster.modelo.RespuestaRest;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.servicio.ClienteRest;
import ec.edu.monster.vista.GestorPantallas;
import ec.edu.monster.vista.LoginFrame;
import ec.edu.monster.vista.VentanaConversiones;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;

public final class ControladorLogin {
    private final ClienteRest cliente;
    private final LoginFrame vista;

    public ControladorLogin(ClienteRest cliente, LoginFrame vista) {
        this.cliente = cliente;
        this.vista = vista;
        vista.alIngresar(this::ingresar);
        vista.alRecuperar(this::recuperarClave);
    }

    public void mostrar() {
        GestorPantallas.INSTANCIA.registrarAcceso(vista);
        GestorPantallas.INSTANCIA.mostrarAcceso();
    }

    private void ingresar(ActionEvent evento) {
        String usuario = vista.usuarioIngresado();
        char[] temporal = vista.claveIngresada();
        if (usuario.isBlank() || temporal.length == 0) {
            Arrays.fill(temporal, '\0');
            vista.mostrarMensaje("Escribe el usuario y la contraseña.", true);
            return;
        }
        String clave = new String(temporal);
        Arrays.fill(temporal, '\0');
        vista.setOcupado(true);
        vista.mostrarMensaje("Conectando con el servidor…", false);
        new SwingWorker<RespuestaRest, Void>() {
            @Override protected RespuestaRest doInBackground() throws IOException {
                return cliente.autenticar(usuario, clave);
            }
            @Override protected void done() {
                vista.setOcupado(false);
                vista.limpiarClave();
                try {
                    RespuestaRest respuesta = get();
                    if (!respuesta.isExito() || respuesta.getToken() == null || respuesta.getToken().isBlank()) {
                        vista.mostrarMensaje(mensajeRespuesta(respuesta), true);
                        return;
                    }
                    VentanaConversiones conversiones = new VentanaConversiones();
                    new ControladorConversiones(cliente,
                            new Sesion(respuesta.getToken(), respuesta.getExpiraEn()), conversiones).iniciar();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    vista.mostrarMensaje("Se interrumpió el inicio de sesión.", true);
                } catch (ExecutionException ex) {
                    vista.mostrarMensaje(mensajeError(ex), true);
                }
            }
        }.execute();
    }

    private void recuperarClave(ActionEvent evento) {
        String usuario = vista.usuarioIngresado();
        if (usuario.isBlank()) {
            vista.mostrarMensaje("Escribe tu usuario antes de recuperar la contraseña.", true);
            return;
        }
        char[] temporal = vista.pedirNuevaClave();
        if (temporal == null) return;
        if (temporal.length == 0) {
            Arrays.fill(temporal, '\0');
            vista.mostrarMensaje("La nueva contraseña no puede estar vacía.", true);
            return;
        }
        String nuevaClave = new String(temporal);
        Arrays.fill(temporal, '\0');
        vista.setOcupado(true);
        vista.mostrarMensaje("Actualizando contraseña…", false);
        new SwingWorker<RespuestaRest, Void>() {
            @Override protected RespuestaRest doInBackground() throws IOException {
                return cliente.recuperarClave(usuario, nuevaClave);
            }
            @Override protected void done() {
                vista.setOcupado(false);
                try {
                    RespuestaRest respuesta = get();
                    boolean exito = respuesta.isExito();
                    vista.mostrarMensaje(exito ? texto(respuesta.getMensaje(), "Contraseña actualizada.")
                            : mensajeRespuesta(respuesta), !exito);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    vista.mostrarMensaje("Se interrumpió la recuperación de contraseña.", true);
                } catch (ExecutionException ex) {
                    vista.mostrarMensaje(mensajeError(ex), true);
                }
            }
        }.execute();
    }

    static String mensajeError(ExecutionException ex) {
        Throwable cause = ex.getCause();
        return cause instanceof IOException && cause.getMessage() != null
                ? cause.getMessage() : "No se pudo completar la operación. Inténtalo nuevamente.";
    }

    static String mensajeRespuesta(RespuestaRest respuesta) {
        String codigo = texto(respuesta.getCodigo(), "HTTP " + respuesta.getEstadoHttp());
        return codigo + " · " + texto(respuesta.getMensaje(), "El servidor rechazó la solicitud.");
    }

    private static String texto(String valor, String alternativo) {
        return valor == null || valor.isBlank() ? alternativo : valor;
    }
}
