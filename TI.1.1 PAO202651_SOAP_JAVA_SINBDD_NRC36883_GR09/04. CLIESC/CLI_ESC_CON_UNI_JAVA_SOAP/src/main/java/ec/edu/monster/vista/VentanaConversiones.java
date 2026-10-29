package ec.edu.monster.vista;

import ec.edu.monster.modelo.OpcionCatalogo;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/** Formulario principal de conversión, alimentado por el catálogo remoto. */
public final class VentanaConversiones extends JFrame {
    private final JComboBox<OpcionCatalogo> categorias = new JComboBox<>();
    private final JComboBox<OpcionCatalogo> origen = new JComboBox<>();
    private final JComboBox<OpcionCatalogo> destino = new JComboBox<>();
    private final JTextField valor = new JTextField();
    private final JButton convertir = new JButton("Convertir");
    private final JButton catalogo = new JButton("Ver catalogo");
    private final JButton cerrarSesion = new JButton("Cerrar sesión");
    private final JLabel resultado = new JLabel("", SwingConstants.CENTER);
    private final JLabel estado = new JLabel(" ");
    private boolean actualizandoCatalogo;
    private JPanel vistaConversor;

    public VentanaConversiones() {
        super("Monster · Conversor de unidades");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        EstilosUI.configurarVentana(this);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(EstilosUI.FONDO);
        JPanel contenido = new JPanel(new BorderLayout(0, 22));
        contenido.setOpaque(false);
        contenido.setBorder(BorderFactory.createEmptyBorder(24, 34, 24, 54));
        contenido.add(crearCabecera(), BorderLayout.NORTH);
        contenido.add(crearFormulario(), BorderLayout.CENTER);
        contenido.add(crearPie(), BorderLayout.SOUTH);
        raiz.add(contenido, BorderLayout.CENTER);
        raiz.add(new ImagenLateral(), BorderLayout.EAST);
        vistaConversor = raiz;
        setContentPane(vistaConversor);

        categorias.addActionListener(evento -> {
            if (!actualizandoCatalogo) {
                limpiarResultado();
                if (categoriaSeleccionada() != null) {
                    estado.setText("Cargando unidades…");
                }
            }
        });
        for (JComboBox<OpcionCatalogo> combo : List.of(categorias, origen, destino)) {
            EstilosUI.estilizarSelector(combo);
        }
    }

    private JPanel crearCabecera() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JPanel textos = new JPanel(new java.awt.GridLayout(0, 1, 0, 5));
        textos.setOpaque(false);
        JLabel marca = new JLabel("MONSTER  /  SERVICIO SOAP");
        marca.setFont(new Font("Segoe UI", Font.BOLD, 12));
        marca.setForeground(EstilosUI.AZUL_MARINO);
        JLabel titulo = new JLabel("Conversor de unidades");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 27));
        titulo.setForeground(EstilosUI.TINTA);
        JLabel detalle = new JLabel("Selecciona una categoría y convierte con el catálogo del servidor.");
        detalle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        detalle.setForeground(EstilosUI.GRIS);
        textos.add(marca);
        textos.add(titulo);
        textos.add(detalle);
        EstilosUI.estilizarBotonSecundario(cerrarSesion);
        panel.add(textos, BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        acciones.setOpaque(false);
        acciones.add(cerrarSesion);
        panel.add(acciones, BorderLayout.EAST);
        return panel;
    }

    private JPanel crearFormulario() {
        JPanel tarjeta = new JPanel(new GridBagLayout());
        tarjeta.setBackground(EstilosUI.BLANCO);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstilosUI.BORDE),
                BorderFactory.createEmptyBorder(26, 28, 26, 28)));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        agregar(tarjeta, etiqueta("Categoría"), c, 0, 0, 2);
        categorias.setName("categoria");
        agregar(tarjeta, categorias, c, 0, 1, 2);

        agregar(tarjeta, etiqueta("Valor"), c, 0, 2, 2);
        valor.setName("valor");
        EstilosUI.estilizarCampo(valor);
        agregar(tarjeta, valor, c, 0, 3, 2);

        agregar(tarjeta, etiqueta("Unidad de origen"), c, 0, 4, 1);
        agregar(tarjeta, etiqueta("Unidad de destino"), c, 1, 4, 1);
        origen.setName("unidadOrigen");
        destino.setName("unidadDestino");
        agregar(tarjeta, origen, c, 0, 5, 1);
        agregar(tarjeta, destino, c, 1, 5, 1);

        EstilosUI.estilizarBotonPrincipal(convertir);
        c.gridwidth = 1;
        agregar(tarjeta, convertir, c, 0, 6, 1);
        EstilosUI.estilizarBotonPrincipal(catalogo);
        agregar(tarjeta, catalogo, c, 1, 6, 1);

        JPanel bloqueResultado = new JPanel(new BorderLayout(0, 16));
        bloqueResultado.setOpaque(false);
        JLabel tituloResultado = new JLabel("Resultado");
        tituloResultado.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tituloResultado.setForeground(EstilosUI.TINTA);
        bloqueResultado.add(tituloResultado, BorderLayout.NORTH);

        resultado.setFont(new Font("Segoe UI", Font.BOLD, 28));
        resultado.setPreferredSize(new Dimension(440, 64));
        resultado.setMinimumSize(new Dimension(440, 64));
        resultado.setForeground(java.awt.Color.WHITE);
        resultado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstilosUI.BORDE),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        resultado.setOpaque(true);
        resultado.setBackground(EstilosUI.AZUL_MARINO);
        bloqueResultado.add(resultado, BorderLayout.CENTER);
        agregar(tarjeta, bloqueResultado, c, 0, 7, 2);
        return tarjeta;
    }

    private JPanel crearPie() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        estado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        estado.setForeground(EstilosUI.GRIS);
        panel.add(estado, BorderLayout.WEST);
        return panel;
    }

    private static JLabel etiqueta(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(EstilosUI.TINTA);
        return label;
    }

    private static void agregar(JPanel panel, java.awt.Component componente,
            GridBagConstraints plantilla, int x, int y, int ancho) {
        GridBagConstraints c = (GridBagConstraints) plantilla.clone();
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = ancho;
        panel.add(componente, c);
    }

    public void alCambiarCategoria(ActionListener listener) { categorias.addActionListener(listener); }
    public void alConvertir(ActionListener listener) { convertir.addActionListener(listener); }
    public void alConsultarCatalogo(ActionListener listener) { catalogo.addActionListener(listener); }
    public void alCerrarSesion(ActionListener listener) { cerrarSesion.addActionListener(listener); }
    public OpcionCatalogo categoriaSeleccionada() { return (OpcionCatalogo) categorias.getSelectedItem(); }
    public OpcionCatalogo origenSeleccionado() { return (OpcionCatalogo) origen.getSelectedItem(); }
    public OpcionCatalogo destinoSeleccionado() { return (OpcionCatalogo) destino.getSelectedItem(); }
    public String valorIngresado() { return valor.getText().trim(); }

    public void setCategorias(List<OpcionCatalogo> opciones) {
        actualizandoCatalogo = true;
        categorias.removeAllItems();
        opciones.forEach(categorias::addItem);
        actualizandoCatalogo = false;
    }

    public void seleccionarPrimeraCategoria() {
        if (categorias.getItemCount() > 0) {
            actualizandoCatalogo = true;
            categorias.setSelectedIndex(0);
            actualizandoCatalogo = false;
        }
    }

    public void setUnidades(List<OpcionCatalogo> opciones) {
        actualizandoCatalogo = true;
        origen.removeAllItems();
        destino.removeAllItems();
        opciones.forEach(origen::addItem);
        opciones.forEach(destino::addItem);
        actualizandoCatalogo = false;
    }

    public void limpiarUnidades() { setUnidades(List.of()); }
    public void mostrarResultado(String texto) { resultado.setText(texto); }
    public void mostrarCatalogo(String texto) {
        JPanel raiz = new JPanel(new BorderLayout(0, 18));
        raiz.setBackground(EstilosUI.FONDO);
        raiz.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        JLabel titulo = new JLabel("Catálogo de unidades");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titulo.setForeground(EstilosUI.TINTA);
        JButton volver = new JButton("Volver al conversor");
        EstilosUI.estilizarBotonSecundario(volver);
        volver.addActionListener(e -> {
            setContentPane(vistaConversor);
            revalidate();
            repaint();
        });
        encabezado.add(titulo, BorderLayout.CENTER);
        encabezado.add(volver, BorderLayout.EAST);
        JPanel cuadrícula = new JPanel(new java.awt.GridLayout(0, 3, 16, 16));
        cuadrícula.setOpaque(false);
        JPanel tarjeta = null;
        JPanel unidades = null;
        for (String linea : texto.split("\\R")) {
            String limpio = linea.trim();
            if (limpio.isEmpty() || limpio.startsWith("Conversiones disponibles")) continue;
            if (!linea.startsWith(" ") && !linea.startsWith("\t")) {
                tarjeta = new JPanel(new BorderLayout(0, 10));
                tarjeta.setBackground(EstilosUI.BLANCO);
                tarjeta.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(EstilosUI.BORDE), BorderFactory.createEmptyBorder(18, 20, 18, 20)));
                JLabel categoría = new JLabel(limpio);
                categoría.setFont(new Font("Segoe UI", Font.BOLD, 17));
                categoría.setForeground(EstilosUI.AZUL_MARINO);
                unidades = new JPanel(new java.awt.GridLayout(0, 1, 0, 7));
                unidades.setOpaque(false);
                tarjeta.add(categoría, BorderLayout.NORTH);
                tarjeta.add(unidades, BorderLayout.CENTER);
                cuadrícula.add(tarjeta);
            } else if (unidades != null) {
                JLabel unidad = new JLabel(limpio.replaceFirst("^[•*-]\\s*", ""));
                unidad.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                unidad.setForeground(EstilosUI.TINTA);
                unidades.add(unidad);
            }
        }
        raiz.add(encabezado, BorderLayout.NORTH);
        raiz.add(new javax.swing.JScrollPane(cuadrícula), BorderLayout.CENTER);
        setContentPane(raiz);
        revalidate();
        repaint();
    }
    public void mostrarEstado(String texto) { estado.setText(texto == null || texto.isBlank() ? " " : texto); }

    public void setOcupado(boolean ocupado) {
        categorias.setEnabled(!ocupado);
        origen.setEnabled(!ocupado);
        destino.setEnabled(!ocupado);
        valor.setEnabled(!ocupado);
        convertir.setEnabled(!ocupado);
        catalogo.setEnabled(!ocupado);
        cerrarSesion.setEnabled(!ocupado);
        setCursor(ocupado ? java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR)
                : java.awt.Cursor.getDefaultCursor());
    }

    public void mostrarAviso(String texto, String titulo, int tipo) {
        javax.swing.JOptionPane.showMessageDialog(this, texto, titulo, tipo);
    }

    public void limpiarResultado() {
        resultado.setText("");
    }
}
