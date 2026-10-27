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
        private static void ResultadoMuestraMaximoCuatroDecimales()
        {
            CultureInfo culturaAnterior = CultureInfo.CurrentCulture;
            CultureInfo.CurrentCulture = new CultureInfo("es-EC");
            try
            {
                using (var form = new ConversionForm())
                {
                    Label etiquetaResultado = (Label)ControlInterno(form, "resultado");
                    form.MostrarMensaje("Convirtiendo…", false);
                    form.MostrarResultado(100, RespuestaExito(0.220462262184878));
                    Exigir(etiquetaResultado.Text == "100 GRAMO = 0,2205 LIBRA", "El resultado debe redondearse a cuatro decimales: " + etiquetaResultado.Text);
                    Exigir(((Label)ControlInterno(form, "mensaje")).Text == String.Empty, "Al completar la conversión no debe aparecer 'Conversión realizada' ni otro mensaje de éxito");

                    form.MostrarResultado(1, RespuestaExito(1.5));
                    Exigir(etiquetaResultado.Text == "1 GRAMO = 1,5 LIBRA", "El resultado debe quitar ceros decimales innecesarios: " + etiquetaResultado.Text);
                }
            }
            finally { CultureInfo.CurrentCulture = culturaAnterior; }
        }
    }
}