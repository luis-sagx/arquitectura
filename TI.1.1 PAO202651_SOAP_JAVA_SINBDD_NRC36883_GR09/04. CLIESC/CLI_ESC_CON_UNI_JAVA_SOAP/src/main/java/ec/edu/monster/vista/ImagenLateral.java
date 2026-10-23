package ec.edu.monster.vista;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/** Panel lateral que muestra la ilustración del cliente sin deformarla. */
final class ImagenLateral extends JPanel {
    private static final int ANCHO = 410;
    private final BufferedImage imagen;

    ImagenLateral() {
        setPreferredSize(new Dimension(ANCHO, 0));
        setBackground(EstilosUI.AZUL_MARINO);
        var recurso = ImagenLateral.class.getResource("/monster.jpg");
        if (recurso == null) {
            throw new IllegalStateException("No se encontró el recurso /monster.jpg");
        }
        try {
            imagen = ImageIO.read(recurso);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo cargar /monster.jpg", ex);
        }
        getAccessibleContext().setAccessibleDescription("Monster en una oficina usando una computadora");
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (imagen == null || getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        double escala = Math.max((double) getWidth() / imagen.getWidth(), (double) getHeight() / imagen.getHeight());
        int ancho = (int) Math.ceil(imagen.getWidth() * escala);
        int alto = (int) Math.ceil(imagen.getHeight() * escala);
        int x = (getWidth() - ancho) / 2;
        int y = (getHeight() - alto) / 2;
        g.drawImage(imagen, x, y, ancho, alto, this);
        g.dispose();
    }
}
