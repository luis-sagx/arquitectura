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
        private static void ValidacionesTienenZonaSeparada()
        {
            using (var form = new ConversionForm())
            {
                form.MostrarMensaje("DATA-001 · La masa y la longitud no pueden ser negativas", true);
                Label etiquetaMensaje = (Label)ControlInterno(form, "mensaje");
                Label etiquetaSesion = (Label)ControlInterno(form, "sesion");
                Exigir(Object.ReferenceEquals(etiquetaMensaje.Parent, form), "Las validaciones deben usar el área inferior del formulario");
                Exigir(etiquetaMensaje.Text.StartsWith("DATA-001"), "El mensaje de validación debe permanecer visible");
                Exigir(etiquetaMensaje.ForeColor == Color.FromArgb(190, 44, 58), "La validación debe conservar el color de error");
                Exigir(etiquetaMensaje.Height == 24, "El mensaje debe ocupar una sola línea compacta");
                Exigir(etiquetaMensaje.AutoEllipsis, "El mensaje largo debe ajustarse a su columna con puntos suspensivos");
                Exigir(etiquetaMensaje.Top == etiquetaSesion.Top, "El mensaje y la fecha deben compartir una sola franja horizontal");
                Exigir(etiquetaMensaje.Right < etiquetaSesion.Left, "El mensaje debe quedar a la izquierda de la fecha de sesión");
                ToolTip ayuda = (ToolTip)typeof(ConversionForm).GetField("ayudaMensajes", BindingFlags.Instance | BindingFlags.NonPublic).GetValue(form);
                Exigir(ayuda.GetToolTip(etiquetaMensaje) == etiquetaMensaje.Text, "El tooltip debe conservar el texto completo de validación");
            }
        }
    }
}