package ec.edu.monster.vista;

import java.util.Objects;

/** Singleton que centraliza el cambio entre las ventanas de la aplicación. */
public enum GestorPantallas {
    INSTANCIA;

    private LoginFrame acceso;
    private VentanaConversiones conversiones;

    public void registrarAcceso(LoginFrame acceso) {
        Objects.requireNonNull(acceso, "La ventana de acceso es obligatoria");
        if (this.acceso != null && this.acceso != acceso) {
            throw new IllegalStateException("La ventana de acceso ya fue registrada");
        }
        this.acceso = acceso;
    }

    public void mostrarAcceso() {
        if (acceso == null) {
            throw new IllegalStateException("Primero debe registrarse la ventana de acceso");
        }
        acceso.setVisible(true);
        acceso.toFront();
    }

    public void mostrarConversiones(VentanaConversiones ventana) {
        Objects.requireNonNull(ventana, "La ventana de conversiones es obligatoria");
        if (acceso == null) {
            throw new IllegalStateException("Primero debe registrarse la ventana de acceso");
        }
        if (conversiones != null && conversiones != ventana) {
            conversiones.dispose();
        }
        conversiones = ventana;
        acceso.setVisible(false);
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
        ventana.toFront();
    }

    public void volverAlAcceso(VentanaConversiones ventana, String mensaje, boolean error) {
        if (ventana != null) {
            ventana.dispose();
            if (conversiones == ventana) {
                conversiones = null;
            }
        }
        if (acceso == null) {
            throw new IllegalStateException("Primero debe registrarse la ventana de acceso");
        }
        acceso.limpiarClave();
        acceso.mostrarMensaje(mensaje, error);
        acceso.setVisible(true);
        acceso.toFront();
    }
}
