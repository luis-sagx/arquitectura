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
        private static void AbrirConversionesReutilizaInstanciaVigente()
        {
            PrepararGestor();
            int creaciones = 0;
            Func<ConversionForm> fabrica = () =>
            {
                creaciones++;
                return new ConversionForm();
            };
            ventanaConversiones = MostrarConversiones(fabrica);
            ConversionForm segundaSolicitud = MostrarConversiones(fabrica);
            Exigir(Object.ReferenceEquals(ventanaConversiones, segundaSolicitud), "El gestor debe devolver la misma ventana activa");
            Exigir(creaciones == 1, "El gestor no debe invocar la fábrica otra vez si ya hay ventana activa");
        }
    }
}