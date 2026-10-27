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
        private static void PaletaCoincideConReferenciaJava()
        {
            Type estilos = typeof(LoginForm).Assembly.GetType("ec.edu.monster.vista.EstilosUI", true);
            Exigir(ColorDe(estilos, "Fondo") == Color.FromArgb(241, 244, 248), "Fondo debe ser RGB(241,244,248)");
            Exigir(ColorDe(estilos, "Tinta") == Color.FromArgb(29, 41, 57), "Tinta debe ser RGB(29,41,57)");
            Exigir(ColorDe(estilos, "TextoSecundario") == Color.FromArgb(91, 105, 122), "TextoSecundario debe ser RGB(91,105,122)");
            Exigir(ColorDe(estilos, "Acento") == Color.FromArgb(24, 50, 87), "Acento debe ser el azul marino RGB(24,50,87)");
            Exigir(ColorDe(estilos, "AcentoHover") == Color.FromArgb(35, 68, 112), "AcentoHover debe ser RGB(35,68,112)");
            Exigir(ColorDe(estilos, "Suave") == Color.FromArgb(232, 237, 244), "Suave debe ser RGB(232,237,244)");
            Exigir(ColorDe(estilos, "Borde") == Color.FromArgb(211, 219, 229), "Borde debe ser RGB(211,219,229)");
            Exigir(ColorDe(estilos, "Blanco") == Color.FromArgb(252, 253, 254), "Blanco debe ser RGB(252,253,254)");
        }
    }
}