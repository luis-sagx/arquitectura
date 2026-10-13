package ec.edu.monster.controlador;

import ec.edu.monster.modelo.OpcionCatalogo;
import ec.edu.monster.modelo.RespuestaSoap;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.servicio.ClienteSoap;
import ec.edu.monster.vista.GestorPantallas;
import ec.edu.monster.vista.VentanaConversiones;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/** Controla la sesión, el catálogo remoto y las conversiones. */
public final class ControladorConversiones {
    private final ClienteSoap cliente;
    private final Sesion sesion;
    private final VentanaConversiones vista;

    public ControladorConversiones(ClienteSoap cliente, Sesion sesion, VentanaConversiones vista) {
        this.cliente = cliente;
        this.sesion = sesion;
        this.vista = vista;
        vista.alCambiarCategoria(this::cargarUnidades);
        vista.alConvertir(this::convertir);
        vista.alCerrarSesion(this::cerrarSesion);
        vista.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evento) {
                cerrarSesion(null);
            }
        });
    }

    public void iniciar() {
        GestorPantallas.INSTANCIA.mostrarConversiones(vista);
        cargarCategorias();
    }

    private void cargarCategorias() {
        vista.setOcupado(true);
        vista.mostrarEstado("Consultando categorías del servidor…");
        new SwingWorker<RespuestaSoap, Void>() {
            @Override
            protected RespuestaSoap doInBackground() throws IOException {
                return cliente.obtenerCategorias(sesion.token());
            }

            @Override
            protected void done() {
                vista.setOcupado(false);
                try {
                    RespuestaSoap respuesta = get();
                    if (respuesta.isSesionInvalida()) {
                        expirarSesion(respuesta.mensaje());
                    } else if (!respuesta.isExito()) {
                        vista.mostrarEstado("No se pudieron cargar las categorías.");
                        vista.mostrarAviso(respuesta.mensaje(), "Catálogo", JOptionPane.WARNING_MESSAGE);
                    } else {
                        List<OpcionCatalogo> opciones = respuesta.categorias().stream()
                                .map(OpcionCatalogo::desde).toList();
                        vista.setCategorias(opciones);
                        if (opciones.isEmpty()) {
                            vista.mostrarEstado("El servidor no publicó categorías.");
                        } else {
                            vista.mostrarEstado("Catálogo cargado · sesión hasta " + sesion.expiraEn());
                            vista.seleccionarPrimeraCategoria();
                            cargarUnidades(null);
                        }
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    errorConexion("Se interrumpió la consulta de categorías.");
                } catch (ExecutionException ex) {
                    errorConexion(ControladorLogin.mensajeError(ex));
                }
            }
        }.execute();
    }

    private void cargarUnidades(ActionEvent evento) {
        OpcionCatalogo categoria = vista.categoriaSeleccionada();
        vista.limpiarUnidades();
        vista.limpiarResultado();
        if (categoria == null) {
            vista.mostrarEstado("Selecciona una categoría.");
            return;
        }

        vista.setOcupado(true);
        vista.mostrarEstado("Consultando unidades de " + categoria.etiqueta() + "…");
        new SwingWorker<RespuestaSoap, Void>() {
            @Override
            protected RespuestaSoap doInBackground() throws IOException {
                return cliente.obtenerUnidades(sesion.token(), categoria.id());
            }

            @Override
            protected void done() {
                vista.setOcupado(false);
                try {
                    RespuestaSoap respuesta = get();
                    if (respuesta.isSesionInvalida()) {
                        expirarSesion(respuesta.mensaje());
                    } else if (!respuesta.isExito()) {
                        vista.mostrarEstado("No se pudieron cargar las unidades.");
                        vista.mostrarAviso(respuesta.mensaje(), "Unidades", JOptionPane.WARNING_MESSAGE);
                    } else {
                        List<OpcionCatalogo> opciones = respuesta.unidades().stream()
                                .map(OpcionCatalogo::desde).toList();
                        vista.setUnidades(opciones);
                        vista.mostrarEstado(opciones.isEmpty()
                                ? "Esta categoría no tiene unidades disponibles." : " ");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    errorConexion("Se interrumpió la consulta de unidades.");
                } catch (ExecutionException ex) {
                    errorConexion(ControladorLogin.mensajeError(ex));
                }
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

        double valor;
        try {
            valor = parsearValor(vista.valorIngresado());
        } catch (IllegalArgumentException ex) {
            vista.mostrarAviso(ex.getMessage(), "Valor inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        vista.setOcupado(true);
        vista.mostrarEstado("Convirtiendo en el servidor…");
        new SwingWorker<RespuestaSoap, Void>() {
            @Override
            protected RespuestaSoap doInBackground() throws IOException {
                return cliente.convertir(sesion.token(), categoria.id(), origen.id(), destino.id(), valor);
            }

            @Override
            protected void done() {
                vista.setOcupado(false);
                try {
                    RespuestaSoap respuesta = get();
                    if (respuesta.isSesionInvalida()) {
                        expirarSesion(respuesta.mensaje());
                    } else if (respuesta.isExito() && respuesta.resultado() != null) {
                        NumberFormat formato = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-EC"));
                        formato.setMaximumFractionDigits(4);
                        String resultado = formato.format(respuesta.resultado());
                        vista.mostrarResultado(resultado + " " + destino.etiqueta());
                        vista.mostrarEstado(respuesta.mensaje());
                    } else {
                        vista.mostrarEstado(respuesta.codigo() + " · " + respuesta.mensaje());
                        vista.mostrarAviso(respuesta.mensaje(), "No se pudo convertir", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    errorConexion("Se interrumpió la conversión.");
                } catch (ExecutionException ex) {
                    errorConexion(ControladorLogin.mensajeError(ex));
                }
            }
        }.execute();
    }

    private void cerrarSesion(ActionEvent evento) {
        volverAlAcceso("La sesión se cerró correctamente.", false);
    }

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
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Escribe el valor que deseas convertir.");
        }
        String limpio = texto.trim();
        if (limpio.contains(",") && limpio.contains(".")) {
            throw new IllegalArgumentException("Usa un solo separador decimal: punto o coma.");
        }
        limpio = limpio.replace(',', '.');
        try {
            double numero = Double.parseDouble(limpio);
            if (!Double.isFinite(numero)) {
                throw new IllegalArgumentException("El valor debe ser un número finito.");
            }
            return numero;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Escribe un número válido; puedes usar punto o coma decimal.");
        }
    }
}
