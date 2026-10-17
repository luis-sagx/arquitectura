package ec.edu.monster.vista;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;

/** Paleta y tratamientos compartidos por los formularios Swing. */
final class EstilosUI {
    static final int ANCHO_VENTANA = 790;
    static final int ALTO_VENTANA = 660;
    static final int ALTO_CONTROL = 36;
    static final Color FONDO = new Color(241, 244, 248);
    static final Color TINTA = new Color(29, 41, 57);
    static final Color AZUL_MARINO = new Color(24, 50, 87);
    static final Color AZUL_HOVER = new Color(35, 68, 112);
    static final Color SUAVE = new Color(232, 237, 244);
    static final Color GRIS = new Color(91, 105, 122);
    static final Color BORDE = new Color(211, 219, 229);
    static final Color BLANCO = new Color(252, 253, 254);

    private EstilosUI() { }

    static void configurarVentana(JFrame ventana) {
        Dimension dimension = new Dimension(ANCHO_VENTANA, ALTO_VENTANA);
        ventana.setSize(dimension);
        ventana.setMinimumSize(dimension);
        ventana.setMaximumSize(dimension);
        ventana.setResizable(false);
        ventana.setLocationRelativeTo(null);
    }

    static Border bordeCampo() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(3, 14, 3, 14));
    }

    static void estilizarCampo(JTextField campo) {
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        campo.setForeground(TINTA);
        campo.setBackground(BLANCO);
        campo.setBorder(bordeCampo());
        campo.setCaretColor(AZUL_MARINO);
        campo.setPreferredSize(new Dimension(220, ALTO_CONTROL));
        campo.setMinimumSize(new Dimension(0, ALTO_CONTROL));
    }

    static void estilizarSelector(JComboBox<?> selector) {
        selector.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        selector.setBackground(BLANCO);
        selector.setForeground(TINTA);
        selector.setPreferredSize(new Dimension(220, ALTO_CONTROL));
        selector.setMinimumSize(new Dimension(0, ALTO_CONTROL));
    }

    static void estilizarBotonPrincipal(JButton boton) {
        boton.setUI(new BasicButtonUI());
        boton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        boton.setForeground(BLANCO);
        boton.setBackground(AZUL_MARINO);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        boton.setPreferredSize(new Dimension(220, ALTO_CONTROL));
        boton.setMinimumSize(new Dimension(120, ALTO_CONTROL));
        boton.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        boton.getModel().addChangeListener(evento -> boton.setBackground(
                boton.getModel().isRollover() ? AZUL_HOVER : AZUL_MARINO));
    }

    static void estilizarBotonSecundario(JButton boton) {
        boton.setUI(new BasicButtonUI());
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.setForeground(AZUL_MARINO);
        boton.setBackground(SUAVE);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        Dimension preferred = boton.getPreferredSize();
        boton.setPreferredSize(new Dimension(Math.max(140, preferred.width), ALTO_CONTROL));
        boton.setMinimumSize(new Dimension(100, ALTO_CONTROL));
        boton.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }
}
