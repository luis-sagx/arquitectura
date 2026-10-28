package ec.edu.monster.controlador;

import ec.edu.monster.modelo.OpcionCatalogo;
import ec.edu.monster.modelo.RespuestaSoap;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.servicio.ClienteSoap;
import ec.edu.monster.servicio.EjecutorTareas;
import ec.edu.monster.vista.VistaConversiones;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Controla la sesión, el catálogo remoto y las conversiones. */
public final class ControladorConversiones {
    private final VistaConversiones vista;
    private final ClienteSoap cliente;
    private final Sesion sesion;
    private final EjecutorTareas ejecutor;
    /** Categoría cuyas unidades se cargaron o se están cargando. */
    private String categoriaActual;

    public ControladorConversiones(VistaConversiones vista, ClienteSoap cliente, Sesion sesion,
            EjecutorTareas ejecutor) {
        this.vista = vista;
        this.cliente = cliente;
        this.sesion = sesion;
        this.ejecutor = ejecutor;
    }

    public void iniciar() {
        cargarCategorias();
    }

    public void consultarCatalogo() {
        vista.setOcupado(true);
        final String token = sesion.token();
        ejecutor.ejecutar(() -> {
            RespuestaSoap respuesta = cliente.obtenerCategorias(token);
            if (!respuesta.isExito()) throw new IOException(respuesta.mensaje());
            StringBuilder texto = new StringBuilder();
            for (String categoria : respuesta.categorias()) {
                RespuestaSoap unidades = cliente.obtenerUnidades(token, categoria);
                if (!unidades.isExito()) throw new IOException(unidades.mensaje());
                texto.append(OpcionCatalogo.desde(categoria).etiqueta()).append('\n');
                for (String unidad : unidades.unidades()) texto.append("  • ").append(OpcionCatalogo.desde(unidad).etiqueta()).append('\n');
                texto.append('\n');
            }
            texto.append("Conversiones disponibles: longitud, masa/peso y temperatura.");
            return texto.toString();
        }, new EjecutorTareas.Resultado<String>() {
            @Override public void exito(String catalogo) { vista.setOcupado(false); vista.mostrarCatalogo(catalogo); }
            @Override public void error(IOException error) { vista.setOcupado(false); vista.mostrarAviso("Catálogo", error.getMessage()); }
        });
    }

    private void cargarCategorias() {
        vista.setOcupado(true);
        vista.mostrarEstado("Consultando categorías del servidor…");
        final String token = sesion.token();
        ejecutor.ejecutar(() -> cliente.obtenerCategorias(token), new EjecutorTareas.Resultado<RespuestaSoap>() {
            @Override
            public void exito(RespuestaSoap respuesta) {
                vista.setOcupado(false);
                if (respuesta.isSesionInvalida()) {
                    expirarSesion(respuesta.mensaje());
                } else if (!respuesta.isExito()) {
                    vista.mostrarEstado("No se pudieron cargar las categorías.");
                    vista.mostrarAviso("Catálogo", respuesta.mensaje());
                } else {
                    List<OpcionCatalogo> opciones = opciones(respuesta.categorias().stream()
                        .filter(c -> List.of("LONGITUD", "MASA", "TEMPERATURA").contains(c)).toList());
                    categoriaActual = null;
                    vista.setCategorias(opciones);
                    if (opciones.isEmpty()) {
                        vista.mostrarEstado("El servidor no publicó categorías.");
                    } else {
                        vista.mostrarEstado("Catálogo cargado · sesión hasta las " + sesion.horaExpiracion());
                        cambiarCategoria(opciones.get(0));
                    }
                }
            }

            @Override
            public void error(IOException ex) {
                errorConexion(ControladorLogin.mensajeError(ex));
            }
        });
    }

    /** La vista lo llama al elegir categoría; ignora la misma selección repetida. */
    public void cambiarCategoria(OpcionCatalogo categoria) {
        if (categoria == null || !sesion.isActiva() || categoria.id().equals(categoriaActual)) {
            return;
        }
        categoriaActual = categoria.id();
        vista.limpiarUnidades();
        vista.limpiarResultado();
        vista.setOcupado(true);
        vista.mostrarEstado("Consultando unidades de " + categoria.etiqueta() + "…");
        final String token = sesion.token();
        ejecutor.ejecutar(() -> cliente.obtenerUnidades(token, categoria.id()),
                new EjecutorTareas.Resultado<RespuestaSoap>() {
            @Override
            public void exito(RespuestaSoap respuesta) {
                if (!categoria.id().equals(categoriaActual)) {
                    return; // El usuario ya eligió otra categoría.
                }
                vista.setOcupado(false);
                if (respuesta.isSesionInvalida()) {
                    expirarSesion(respuesta.mensaje());
                } else if (!respuesta.isExito()) {
                    categoriaActual = null;
                    vista.mostrarEstado("No se pudieron cargar las unidades.");
                    vista.mostrarAviso("Unidades", respuesta.mensaje());
                } else {
                    List<OpcionCatalogo> opciones = opciones(respuesta.unidades());
                    vista.setUnidades(opciones);
                    vista.mostrarEstado(opciones.isEmpty()
                            ? "Esta categoría no tiene unidades disponibles."
                            : "Sesión activa hasta las " + sesion.horaExpiracion());
                }
            }

            @Override
            public void error(IOException ex) {
                if (categoria.id().equals(categoriaActual)) {
                    categoriaActual = null;
                    errorConexion(ControladorLogin.mensajeError(ex));
                }
            }
        });
    }

    public void convertir() {
        OpcionCatalogo categoria = vista.categoriaSeleccionada();
        OpcionCatalogo origen = vista.origenSeleccionado();
        OpcionCatalogo destino = vista.destinoSeleccionado();
        if (categoria == null || origen == null || destino == null) {
            vista.mostrarAviso("Faltan datos", "Selecciona la categoría y las unidades de origen y destino.");
            return;
        }

        final double valor;
        try {
            valor = parsearValor(vista.valorIngresado());
        } catch (IllegalArgumentException ex) {
            vista.mostrarAviso("Valor inválido", ex.getMessage());
            return;
        }

        vista.setOcupado(true);
        vista.limpiarResultado();
        vista.mostrarEstado("Convirtiendo en el servidor…");
        final String token = sesion.token();
        ejecutor.ejecutar(() -> cliente.convertir(token, categoria.id(), origen.id(), destino.id(), valor),
                new EjecutorTareas.Resultado<RespuestaSoap>() {
            @Override
            public void exito(RespuestaSoap respuesta) {
                vista.setOcupado(false);
                if (respuesta.isSesionInvalida()) {
                    expirarSesion(respuesta.mensaje());
                } else if (respuesta.isExito() && respuesta.resultado() != null) {
                    vista.mostrarResultado(formatear(respuesta.resultado()) + " " + destino.etiqueta());
                    vista.mostrarEstado(respuesta.mensaje());
                } else {
                    vista.mostrarEstado(respuesta.codigo() + " · " + respuesta.mensaje());
                    vista.mostrarAviso("No se pudo convertir", respuesta.mensaje());
                }
            }

            @Override
            public void error(IOException ex) {
                errorConexion(ControladorLogin.mensajeError(ex));
            }
        });
    }

    public void cerrarSesion() {
        volverAlAcceso("La sesión se cerró correctamente.", false);
    }

    public void liberar() {
        ejecutor.cerrar();
    }

    private void expirarSesion(String detalle) {
        volverAlAcceso(detalle == null || detalle.trim().isEmpty()
                ? "La sesión expiró. Inicia sesión nuevamente." : detalle, true);
    }

    private void volverAlAcceso(String mensaje, boolean error) {
        sesion.limpiarToken();
        ejecutor.cerrar();
        vista.volverAlAcceso(mensaje, error);
    }

    private void errorConexion(String mensaje) {
        vista.setOcupado(false);
        vista.mostrarEstado("No se pudo completar la operación.");
        vista.mostrarAviso("Error de comunicación", mensaje);
    }

    private static List<OpcionCatalogo> opciones(List<String> ids) {
        List<OpcionCatalogo> opciones = new ArrayList<>(ids.size());
        for (String id : ids) {
            opciones.add(OpcionCatalogo.desde(id));
        }
        return opciones;
    }

    static String formatear(double numero) {
        double absoluto = Math.abs(numero);
        if (numero != 0 && (absoluto < 0.0001 || absoluto >= 1e12)) {
            return String.format(Locale.ROOT, "%.4E", numero);
        }
        NumberFormat formato = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-EC"));
        formato.setMaximumFractionDigits(4);
        formato.setRoundingMode(RoundingMode.HALF_UP);
        return formato.format(numero);
    }

    static double parsearValor(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("Escribe el valor que deseas convertir.");
        }
        String limpio = texto.trim();
        if (limpio.contains(",") && limpio.contains(".")) {
            throw new IllegalArgumentException("Usa un solo separador decimal: punto o coma.");
        }
        limpio = limpio.replace(',', '.');
        double numero;
        try {
            numero = Double.parseDouble(limpio);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Escribe un número válido; puedes usar punto o coma decimal.");
        }
        if (Double.isNaN(numero) || Double.isInfinite(numero)) {
            throw new IllegalArgumentException("El valor debe ser un número finito.");
        }
        return numero;
    }
}
