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
        private static void SesionApareceDebajoConFechaLegible()
        {
            using (var form = new ConversionForm())
            {
                var fecha = new Sesion("token", "2026-10-15T18:26:44.5306397Z");
                form.MostrarSesion(fecha);
                Label etiquetaSesion = (Label)ControlInterno(form, "sesion");
                Label etiquetaMensaje = (Label)ControlInterno(form, "mensaje");
                Exigir(Object.ReferenceEquals(etiquetaSesion.Parent, form), "La sesión debe aparecer debajo de la tarjeta, fuera de ella");
                Exigir(etiquetaSesion.Top > 556, "La sesión debe estar en el pie de la ventana");
                Exigir(etiquetaSesion.Bottom <= form.ClientSize.Height - 10, "La sesión debe conservar al menos 10 px de margen inferior");
                Exigir(etiquetaSesion.Text.Contains("15/10/2026"), "La fecha debe usar un formato corto legible: " + etiquetaSesion.Text);
                Exigir(!etiquetaSesion.Text.Contains("T18") && !etiquetaSesion.Text.EndsWith("Z"), "La fecha no debe mostrarse como timestamp ISO crudo");
                Exigir(etiquetaSesion.Top == etiquetaMensaje.Top, "La sesión y la validación deben compartir una sola franja horizontal");
                Exigir(etiquetaSesion.TextAlign == ContentAlignment.MiddleRight, "La sesión debe alinearse a la derecha");
            }
        }
    }
}