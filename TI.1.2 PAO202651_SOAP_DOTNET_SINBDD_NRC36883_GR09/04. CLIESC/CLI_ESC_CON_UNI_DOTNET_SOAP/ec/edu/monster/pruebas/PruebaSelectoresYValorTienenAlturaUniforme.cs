using System;
using System.Drawing;
using System.Globalization;
using System.Reflection;
using System.Windows.Forms;
using ec.edu.monster.modelo;
using ec.edu.monster.vista;

namespace ec.edu.monster.pruebas
{
    internal static partial class PruebaClienteEscritorio
    {
        private static void SelectoresYValorTienenAlturaUniforme()
        {
            using (var form = new ConversionForm())
            {
                int alturaCategorias = ControlInterno(form, "categorias").Height;
                int alturaOrigen = ControlInterno(form, "origen").Height;
                int alturaDestino = ControlInterno(form, "destino").Height;
                int alturaValor = ControlInterno(form, "valor").Height;
                Exigir(alturaCategorias == 42, "El selector de categoría debe tener 42 px; tiene " + alturaCategorias);
                Exigir(alturaOrigen == alturaCategorias, "El selector de origen debe igualar la altura de categoría");
                Exigir(alturaDestino == alturaCategorias, "El selector de destino debe igualar la altura de categoría");
                Exigir(alturaValor == alturaCategorias, "El campo de valor debe igualar la altura de los selectores; tiene " + alturaValor);
            }
        }
    }
}