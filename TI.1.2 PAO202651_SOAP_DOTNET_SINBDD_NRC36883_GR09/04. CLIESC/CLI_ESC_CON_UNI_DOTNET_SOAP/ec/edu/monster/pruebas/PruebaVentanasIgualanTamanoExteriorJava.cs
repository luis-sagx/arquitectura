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
        private static void VentanasIgualanTamanoExteriorJava()
        {
            using (var acceso = new LoginForm())
            using (var conversiones = new ConversionForm())
            {
                Exigir(acceso.Size == new Size(790, 660), "LoginForm debe medir 790 x 660 incluyendo el marco; mide " + acceso.Size
                    + ", ClientSize=" + acceso.ClientSize + ", AutoScaleMode=" + acceso.AutoScaleMode
                    + ", AutoScaleDimensions=" + acceso.AutoScaleDimensions + ", CurrentAutoScaleDimensions=" + acceso.CurrentAutoScaleDimensions);
                Exigir(conversiones.Size == new Size(790, 660), "ConversionForm debe medir 790 x 660 incluyendo el marco; mide " + conversiones.Size
                    + ", ClientSize=" + conversiones.ClientSize + ", AutoScaleMode=" + conversiones.AutoScaleMode
                    + ", AutoScaleDimensions=" + conversiones.AutoScaleDimensions + ", CurrentAutoScaleDimensions=" + conversiones.CurrentAutoScaleDimensions);
                VerificarLimites(acceso, "LoginForm");
                VerificarLimites(conversiones, "ConversionForm");
            }
        }
    }
}