package ec.edu.monster.vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/** Pantalla de acceso y recuperación de clave. */
public final class LoginFrame extends JFrame {
    private final JTextField usuario = new JTextField();
    private final JPasswordField clave = new JPasswordField();
    private final JButton ingresar = new JButton("Ingresar");
    private final JButton recuperar = new JButton("Recuperar contraseña");
    private final JLabel mensaje = new JLabel(" ", SwingConstants.CENTER);

    public LoginFrame() {
        super("Monster · Conversor de unidades");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        EstilosUI.configurarVentana(this);

        JPanel fondo = new JPanel(new BorderLayout());
        fondo.setBackground(EstilosUI.FONDO);
        fondo.setBorder(BorderFactory.createEmptyBorder(28, 34, 30, 34));

        JPanel cabecera = new JPanel(new GridLayout(0, 1, 0, 8));
        cabecera.setOpaque(false);
        JLabel marca = new JLabel("MONSTER", SwingConstants.CENTER);
        marca.setFont(new Font("Segoe UI", Font.BOLD, 15));
        marca.setForeground(EstilosUI.AZUL_MARINO);
        JLabel titulo = new JLabel("Conversor de unidades", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 27));
        titulo.setForeground(EstilosUI.TINTA);
        JLabel subtitulo = new JLabel("Inicia sesión para continuar", SwingConstants.CENTER);
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitulo.setForeground(EstilosUI.GRIS);
        cabecera.add(marca);
        cabecera.add(titulo);
        cabecera.add(subtitulo);

        JPanel formulario = new JPanel(new GridLayout(0, 1, 0, 9));
        formulario.setOpaque(false);
        formulario.setBorder(BorderFactory.createEmptyBorder(30, 4, 8, 4));
        formulario.add(etiqueta("Usuario"));
        usuario.setName("usuario");
        EstilosUI.estilizarCampo(usuario);
        formulario.add(usuario);
        formulario.add(etiqueta("Contraseña"));
        clave.setName("clave");
        EstilosUI.estilizarCampo(clave);
        formulario.add(clave);
        formulario.add(new JLabel(" "));
        EstilosUI.estilizarBotonPrincipal(ingresar);
        formulario.add(ingresar);
        EstilosUI.estilizarBotonSecundario(recuperar);
        formulario.add(recuperar);
        mensaje.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        mensaje.setForeground(EstilosUI.GRIS);
        formulario.add(mensaje);

        JPanel tarjeta = new JPanel(new BorderLayout());
        tarjeta.setBackground(EstilosUI.BLANCO);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstilosUI.BORDE),
                BorderFactory.createEmptyBorder(27, 28, 22, 28)));
        tarjeta.add(cabecera, BorderLayout.NORTH);
        tarjeta.add(formulario, BorderLayout.CENTER);
        fondo.add(tarjeta, BorderLayout.CENTER);
        setContentPane(fondo);
        getRootPane().setDefaultButton(ingresar);
    }

    private JLabel etiqueta(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(EstilosUI.TINTA);
        return label;
    }

    public void alIngresar(ActionListener listener) { ingresar.addActionListener(listener); }
    public void alRecuperar(ActionListener listener) { recuperar.addActionListener(listener); }
    public String usuarioIngresado() { return usuario.getText().trim(); }
    public char[] claveIngresada() { return clave.getPassword(); }

    public char[] pedirNuevaClave() {
        JPasswordField nueva = new JPasswordField(18);
        EstilosUI.estilizarCampo(nueva);
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 8));
        panel.add(new JLabel("Nueva contraseña para " + usuarioIngresado() + ":"));
        panel.add(nueva);
        int opcion = JOptionPane.showConfirmDialog(this, panel, "Recuperar contraseña",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) {
            char[] valor = nueva.getPassword();
            Arrays.fill(valor, '\0');
            nueva.setText("");
            return null;
        }
        char[] valor = nueva.getPassword();
        nueva.setText("");
        return valor;
    }

    public void limpiarClave() { clave.setText(""); }

    public void setOcupado(boolean ocupado) {
        ingresar.setEnabled(!ocupado);
        recuperar.setEnabled(!ocupado);
        usuario.setEnabled(!ocupado);
        clave.setEnabled(!ocupado);
        setCursor(ocupado ? java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR)
                : java.awt.Cursor.getDefaultCursor());
    }

    public void mostrarMensaje(String texto, boolean error) {
        mensaje.setText("<html><div style='text-align:center'>" + escapar(texto) + "</div></html>");
        mensaje.setForeground(error ? new java.awt.Color(176, 67, 57) : EstilosUI.AZUL_MARINO);
    }

    public void mostrarDialogo(String texto, String titulo, int tipo) {
        JOptionPane.showMessageDialog(this, texto, titulo, tipo);
    }

    private static String escapar(String texto) {
        return texto == null ? "" : texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
