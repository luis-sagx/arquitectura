package ec.edu.monster;

import ec.edu.monster.controlador.ControladorLogin;
import ec.edu.monster.servicio.ClienteRest;
import ec.edu.monster.vista.LoginFrame;
import java.net.URI;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class ClienteEscritorio {
    private static final String ENDPOINT_PREDETERMINADO =
            "http://localhost:8080/WS_CON_UNI_JAVA_REST/api";

    private ClienteEscritorio() { }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> iniciar(args));
    }

    private static void iniciar(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ignored) {
            // Se conserva el tema Swing predeterminado si el sistema no ofrece uno.
        }
        String endpoint = args.length > 0 ? args[0]
                : System.getProperty("conversor.endpoint", ENDPOINT_PREDETERMINADO);
        try {
            ClienteRest cliente = new ClienteRest(URI.create(endpoint));
            new ControladorLogin(cliente, new LoginFrame()).mostrar();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(null, "La dirección del servidor no es válida:\n" + ex.getMessage(),
                    "Configuración incorrecta", JOptionPane.ERROR_MESSAGE);
        }
    }
}
