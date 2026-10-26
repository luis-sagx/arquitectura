package ec.edu.monster.vista;

import ec.edu.monster.modelo.OpcionCatalogo;
import java.util.List;

/** Lo que el controlador de conversiones necesita de la pantalla, sin depender de Android. */
public interface VistaConversiones {
    void setOcupado(boolean ocupado);

    void mostrarEstado(String mensaje);

    void mostrarAviso(String titulo, String mensaje);

    void mostrarCatalogo(String mensaje);

    void setCategorias(List<OpcionCatalogo> categorias);

    void setUnidades(List<OpcionCatalogo> unidades);

    void limpiarUnidades();

    OpcionCatalogo categoriaSeleccionada();

    OpcionCatalogo origenSeleccionado();

    OpcionCatalogo destinoSeleccionado();

    String valorIngresado();

    void mostrarResultado(String resultado);

    void limpiarResultado();

    void volverAlAcceso(String mensaje, boolean error);
}
