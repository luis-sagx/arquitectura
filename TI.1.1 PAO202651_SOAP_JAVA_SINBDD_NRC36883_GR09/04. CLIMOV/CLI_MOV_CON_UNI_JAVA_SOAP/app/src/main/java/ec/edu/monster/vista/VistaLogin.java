package ec.edu.monster.vista;

import ec.edu.monster.modelo.Sesion;

/** Lo que el controlador de acceso necesita de la pantalla, sin depender de Android. */
public interface VistaLogin {
    String usuarioIngresado();

    String claveIngresada();

    void limpiarClave();

    void setOcupado(boolean ocupado);

    void mostrarMensaje(String mensaje, boolean error);

    /** Muestra el diálogo de nueva clave; al confirmar llama a ControladorLogin.recuperarClave. */
    void pedirNuevaClave(String usuario);

    void abrirConversiones(String endpoint, Sesion sesion);
}
