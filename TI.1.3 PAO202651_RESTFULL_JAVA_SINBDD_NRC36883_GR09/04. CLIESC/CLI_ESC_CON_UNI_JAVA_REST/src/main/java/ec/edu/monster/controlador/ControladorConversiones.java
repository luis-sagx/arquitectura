package ec.edu.monster.controlador;

import ec.edu.monster.modelo.OpcionCatalogo;
import ec.edu.monster.modelo.RespuestaRest;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.servicio.ClienteRest;
import ec.edu.monster.vista.GestorPantallas;
import ec.edu.monster.vista.VentanaConversiones;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

public final class ControladorConversiones {
    private final ClienteRest cliente;
    private final Sesion sesion;
    private final VentanaConversiones vista;

    public ControladorConversiones(ClienteRest cliente, Sesion sesion, VentanaConversiones vista) {
        this.cliente = cliente;
        this.sesion = sesion;
        this.vista = vista;
        vista.alCambiarCategoria(this::cargarUnidades);
        vista.alConvertir(this::convertir);
        vista.alConsultarCatalogo(this::consultarCatalogo);
        vista.alCerrarSesion(this::cerrarSesion);
        vista.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent evento) { cerrarSesion(null); }
        });
    }

    public void iniciar() {
        GestorPantallas.INSTANCIA.mostrarConversiones(vista);
        cargarCategorias();
    }

    private void cargarCategorias() {
        vista.setOcupado(true);
        vista.mostrarEstado("Consultando categorías del servidor…");
        new SwingWorker<RespuestaRest, Void>() {
            @Override protected RespuestaRest doInBackground() throws IOException {
                return cliente.obtenerCategorias(sesion.token());
            }
            @Override protected void done() {
                vista.setOcupado(false);
                try {
                    RespuestaRest r = get();
                    if (r.isSesionInvalida()) { expirarSesion(r.getMensaje()); return; }
                    if (!r.isExito()) {
                        vista.mostrarEstado("No se pudieron cargar las categorías.");
                        vista.mostrarAviso(ControladorLogin.mensajeRespuesta(r), "Catálogo", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    List<OpcionCatalogo> opciones = r.getCategorias().stream().filter(c -> List.of("LONGITUD", "MASA", "TEMPERATURA").contains(c)).map(OpcionCatalogo::desde).toList();
                    vista.setCategorias(opciones);
                    if (opciones.isEmpty()) vista.mostrarEstado("El servidor no publicó categorías.");
                    else {
                        vista.mostrarEstado("Catálogo cargado · sesión hasta " + sesion.expiraEn());
                        vista.seleccionarPrimeraCategoria();
                        cargarUnidades(null);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    errorConexion("Se interrumpió la consulta de categorías.");
                } catch (ExecutionException ex) { errorConexion(ControladorLogin.mensajeError(ex)); }
            }
        }.execute();
    }

    private void consultarCatalogo(ActionEvent evento) {
        vista.setOcupado(true);
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws IOException {
                RespuestaRest respuesta = cliente.obtenerCategorias(sesion.token());
                if (!respuesta.isExito()) throw new IOException(respuesta.getMensaje());
                StringBuilder texto = new StringBuilder();
                for (String categoria : respuesta.getCategorias()) {
                    RespuestaRest unidades = cliente.obtenerUnidades(sesion.token(), categoria);
                    if (!unidades.isExito()) throw new IOException(unidades.getMensaje());
                    texto.append(OpcionCatalogo.desde(categoria).etiqueta()).append('\n');
                    unidades.getUnidades().forEach(u -> texto.append("  • ").append(OpcionCatalogo.desde(u).etiqueta()).append('\n'));
                    texto.append('\n');
                }
                texto.append("Conversiones disponibles: longitud, masa/peso y temperatura.");
                return texto.toString();
            }
            @Override protected void done() {
                vista.setOcupado(false);
                try { vista.mostrarCatalogo(get()); }
                catch (InterruptedException error) { Thread.currentThread().interrupt(); vista.mostrarEstado("Se interrumpió la consulta del catálogo."); }
                catch (ExecutionException error) { vista.mostrarAviso(ControladorLogin.mensajeError(error), "Catálogo", JOptionPane.WARNING_MESSAGE); }
            }
        }.execute();
    }

    private void cargarUnidades(ActionEvent evento) {
        OpcionCatalogo categoria = vista.categoriaSeleccionada();
        vista.limpiarUnidades();
        vista.limpiarResultado();
        if (categoria == null) { vista.mostrarEstado("Selecciona una categoría."); return; }
        vista.setOcupado(true);
        vista.mostrarEstado("Consultando unidades de " + categoria.etiqueta() + "…");
        new SwingWorker<RespuestaRest, Void>() {
            @Override protected RespuestaRest doInBackground() throws IOException {
                return cliente.obtenerUnidades(sesion.token(), categoria.id());
            }
            @Override protected void done() {
                vista.setOcupado(false);
                try {
                    RespuestaRest r = get();
                    if (r.isSesionInvalida()) { expirarSesion(r.getMensaje()); return; }
                    if (!r.isExito()) {
                        vista.mostrarEstado("No se pudieron cargar las unidades.");
                        vista.mostrarAviso(ControladorLogin.mensajeRespuesta(r), "Unidades", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    List<OpcionCatalogo> unidades = r.getUnidades().stream().map(OpcionCatalogo::desde).toList();
                    vista.setUnidades(unidades);
                    vista.mostrarEstado(unidades.isEmpty() ? "Esta categoría no tiene unidades disponibles." : " ");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    errorConexion("Se interrumpió la consulta de unidades.");
                } catch (ExecutionException ex) { errorConexion(ControladorLogin.mensajeError(ex)); }
            }
        }.execute();
    }

    private void convertir(ActionEvent evento) {
        OpcionCatalogo categoria = vista.categoriaSeleccionada();
        OpcionCatalogo origen = vista.origenSeleccionado();
        OpcionCatalogo destino = vista.destinoSeleccionado();
        if (categoria == null || origen == null || destino == null) {
            vista.mostrarAviso("Selecciona la categoría y las unidades de origen y destino.",
                    "Faltan datos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        final double valor;
        try { valor = parsearValor(vista.valorIngresado()); }
        catch (IllegalArgumentException ex) {
            vista.mostrarAviso(ex.getMessage(), "Valor inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        vista.setOcupado(true);
        vista.mostrarEstado("Convirtiendo en el servidor…");
        new SwingWorker<RespuestaRest, Void>() {
            @Override protected RespuestaRest doInBackground() throws IOException {
                return cliente.convertir(sesion.token(), categoria.id(), origen.id(), destino.id(), valor);
            }
            @Override protected void done() {
                vista.setOcupado(false);
                try {
                    RespuestaRest r = get();
                    if (r.isSesionInvalida()) { expirarSesion(r.getMensaje()); return; }
                    if (r.isExito() && r.getResultado() != null && Double.isFinite(r.getResultado())) {
                        NumberFormat formato = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-EC"));
                        formato.setMaximumFractionDigits(4);
                        formato.setRoundingMode(RoundingMode.HALF_UP);
                        vista.mostrarResultado(formato.format(r.getResultado()) + " " + destino.etiqueta());
                        vista.mostrarEstado(r.getMensaje() == null ? "Conversión completada." : r.getMensaje());
                    } else {
                        vista.mostrarEstado(ControladorLogin.mensajeRespuesta(r));
                        vista.mostrarAviso(ControladorLogin.mensajeRespuesta(r), "No se pudo convertir",
                                JOptionPane.WARNING_MESSAGE);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    errorConexion("Se interrumpió la conversión.");
                } catch (ExecutionException ex) { errorConexion(ControladorLogin.mensajeError(ex)); }
            }
        }.execute();
    }

    private void cerrarSesion(ActionEvent evento) { volverAlAcceso("La sesión se cerró correctamente.", false); }

    private void expirarSesion(String detalle) {
        volverAlAcceso(detalle == null || detalle.isBlank()
                ? "La sesión expiró. Inicia sesión nuevamente." : detalle, true);
    }

    private void volverAlAcceso(String mensaje, boolean error) {
        sesion.limpiarToken();
        GestorPantallas.INSTANCIA.volverAlAcceso(vista, mensaje, error);
    }

    private void errorConexion(String mensaje) {
        vista.setOcupado(false);
        vista.mostrarEstado("No se pudo completar la operación.");
        vista.mostrarAviso(mensaje, "Error de comunicación", JOptionPane.ERROR_MESSAGE);
    }

    private static double parsearValor(String texto) {
        if (texto == null || texto.isBlank()) throw new IllegalArgumentException("Escribe el valor que deseas convertir.");
        String limpio = texto.trim();
        if (limpio.contains(",") && limpio.contains(".")) {
            throw new IllegalArgumentException("Usa un solo separador decimal: punto o coma.");
        }
        if (limpio.indexOf(',') != limpio.lastIndexOf(',') || limpio.indexOf('.') != limpio.lastIndexOf('.')) {
            throw new IllegalArgumentException("Usa un único separador decimal: punto o coma.");
        }
        limpio = limpio.replace(',', '.');
        try {
            double valor = Double.parseDouble(limpio);
            if (!Double.isFinite(valor)) throw new IllegalArgumentException("El valor debe ser un número finito.");
            return valor;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Escribe un número válido; puedes usar punto o coma decimal.");
        }
    }
}
