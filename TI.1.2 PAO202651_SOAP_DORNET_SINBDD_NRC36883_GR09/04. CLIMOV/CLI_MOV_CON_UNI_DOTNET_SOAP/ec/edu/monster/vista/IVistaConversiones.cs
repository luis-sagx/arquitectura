using System.Collections.Generic;
using ec.edu.monster.modelo;

namespace ec.edu.monster.vista
{
    /// <summary>Lo que el controlador de conversiones necesita de la pantalla, sin depender de MAUI.</summary>
    public interface IVistaConversiones
    {
        void EstablecerOcupado(bool ocupado);
        void MostrarEstado(string mensaje);
        void MostrarAviso(string titulo, string mensaje);

        void EstablecerCategorias(IList<OpcionCatalogo> categorias);
        void EstablecerUnidades(IList<OpcionCatalogo> unidades);
        void LimpiarUnidades();

        OpcionCatalogo? CategoriaSeleccionada { get; }
        OpcionCatalogo? OrigenSeleccionado { get; }
        OpcionCatalogo? DestinoSeleccionado { get; }
        string ValorIngresado { get; }

        void MostrarResultado(string resultado);
        void LimpiarResultado();
        void VolverAlAcceso(string mensaje, bool error);
    }
}
