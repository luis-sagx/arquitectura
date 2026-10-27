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
        private static void CrearVentanaTrasCerrarLaAnterior()
        {
            PrepararGestor();
            Exigir(ventanaConversiones != null, "El caso anterior debe registrar una ventana activa");
            ConversionForm anterior = ventanaConversiones;
            anterior.Close();
            anterior.Dispose();
            Application.DoEvents();

            int creaciones = 0;
            ConversionForm nueva = MostrarConversiones(() =>
            {
                creaciones++;
                return new ConversionForm();
            });
            Exigir(!Object.ReferenceEquals(anterior, nueva), "Tras cerrar la ventana debe crearse una instancia nueva");
            Exigir(creaciones == 1 && !nueva.IsDisposed, "La ventana nueva debe quedar activa después de cerrar la anterior");
            nueva.Close();
            nueva.Dispose();
            Application.DoEvents();
            accesoPrueba.Close();
            accesoPrueba.Dispose();
            accesoPrueba = null;
            gestor = null;
            tipoGestor = null;
            ventanaConversiones = null;
        }
    }
}