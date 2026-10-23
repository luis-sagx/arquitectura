package ec.edu.monster;

import ec.edu.monster.controlador.ControladorLogin;
import ec.edu.monster.servicio.ClienteSoap;
import ec.edu.monster.vista.LoginFrame;
import java.net.URI;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Punto de entrada de la aplicación de escritorio. */
public final class ClienteEscritorio {
    private static final String ENDPOINT_PREDETERMINADO =
            "http://localhost:8080/WS_CON_UNI_JAVA_SOAP/WSConUni";

    private ClienteEscritorio() { }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> iniciar(args));
    }

    private static void iniciar(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ignored) {
            // La aplicación conserva el tema Swing predeterminado si el sistema no lo ofrece.
        }

        String endpoint = args.length > 0
                ? args[0]
                : System.getProperty("conversor.endpoint", ENDPOINT_PREDETERMINADO);
        try {
            ClienteSoap cliente = new ClienteSoap(URI.create(endpoint));
            LoginFrame acceso = new LoginFrame();
            new ControladorLogin(cliente, acceso).mostrar();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(null,
                    "La dirección del servicio no es válida:\n" + ex.getMessage(),
                    "Configuración incorrecta", JOptionPane.ERROR_MESSAGE);
        }
    }
}
