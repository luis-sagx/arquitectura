package ec.edu.monster.controlador;

import ec.edu.monster.modelo.RespuestaSoap;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.servicio.ClienteSoap;
import ec.edu.monster.vista.LoginFrame;
import ec.edu.monster.vista.GestorPantallas;
import ec.edu.monster.vista.VentanaConversiones;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/** Controla el acceso y la recuperación sin mezclar reglas con la vista. */
public final class ControladorLogin {
    private final ClienteSoap cliente;
    private final LoginFrame vista;

    public ControladorLogin(ClienteSoap cliente, LoginFrame vista) {
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
        char[] claveTemporal = vista.claveIngresada();
        if (usuario.isBlank() || claveTemporal.length == 0) {
            Arrays.fill(claveTemporal, '\0');
            vista.mostrarMensaje("Escribe el usuario y la contraseña.", true);
            return;
        }

        String clave = new String(claveTemporal);
        Arrays.fill(claveTemporal, '\0');
        vista.setOcupado(true);
        vista.mostrarMensaje("Conectando con el servicio…", false);
        new SwingWorker<RespuestaSoap, Void>() {
            @Override
            protected RespuestaSoap doInBackground() throws IOException {
                return cliente.autenticar(usuario, clave);
            }

            @Override
            protected void done() {
                vista.setOcupado(false);
                vista.limpiarClave();
                try {
                    RespuestaSoap respuesta = get();
                    if (!respuesta.isExito() || respuesta.token().isBlank()) {
                        vista.mostrarMensaje(mensajeRespuesta(respuesta), true);
                        return;
                    }
                    abrirSesion(new Sesion(respuesta.token(), respuesta.expiraEn()));
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
        char[] nuevaClaveTemporal = vista.pedirNuevaClave();
        if (nuevaClaveTemporal == null) return;
        if (nuevaClaveTemporal.length == 0) {
            Arrays.fill(nuevaClaveTemporal, '\0');
            vista.mostrarMensaje("La nueva contraseña no puede estar vacía.", true);
            return;
        }
        String nuevaClave = new String(nuevaClaveTemporal);
        Arrays.fill(nuevaClaveTemporal, '\0');

        vista.setOcupado(true);
        vista.mostrarMensaje("Actualizando contraseña…", false);
        new SwingWorker<RespuestaSoap, Void>() {
            @Override
            protected RespuestaSoap doInBackground() throws IOException {
                return cliente.recuperarClave(usuario, nuevaClave);
            }

            @Override
            protected void done() {
                vista.setOcupado(false);
                try {
                    RespuestaSoap respuesta = get();
                    vista.mostrarMensaje(respuesta.isExito()
                            ? respuesta.mensaje() : mensajeRespuesta(respuesta), !respuesta.isExito());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    vista.mostrarMensaje("Se interrumpió la recuperación.", true);
                } catch (ExecutionException ex) {
                    vista.mostrarMensaje(mensajeError(ex), true);
                }
            }
        }.execute();
    }

    private void abrirSesion(Sesion sesion) {
        VentanaConversiones ventana = new VentanaConversiones();
        new ControladorConversiones(cliente, sesion, ventana).iniciar();
    }

    static String mensajeError(ExecutionException ex) {
        Throwable causa = ex.getCause();
        return causa instanceof IOException && causa.getMessage() != null
                ? causa.getMessage() : "No se pudo completar la operación. Inténtalo nuevamente.";
    }

    private static String mensajeRespuesta(RespuestaSoap respuesta) {
        return respuesta.codigo() + " · " + respuesta.mensaje();
    }
}
