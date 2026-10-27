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
        private static void BotonesRedondeadosSinMarcoNegro()
        {
            Type estilos = typeof(LoginForm).Assembly.GetType("ec.edu.monster.vista.EstilosUI", true);
            MethodInfo fabrica = estilos.GetMethod("Boton", BindingFlags.Static | BindingFlags.Public | BindingFlags.NonPublic);
            Exigir(fabrica != null, "Debe existir la fábrica compartida de botones");
            using (var botonPrincipal = (Button)fabrica.Invoke(null, new object[] { "Primario", 0, 0, 180, 40, true }))
            using (var botonSecundario = (Button)fabrica.Invoke(null, new object[] { "Secundario", 0, 0, 180, 40, false }))
            {
                Exigir(botonPrincipal.FlatAppearance.BorderSize == 0, "El botón principal no debe pintar un borde nativo");
                Exigir(botonPrincipal.FlatAppearance.BorderColor == Color.FromArgb(24, 50, 87), "El botón principal debe usar borde azul marino");
                Exigir(botonSecundario.FlatAppearance.BorderSize == 0, "El botón secundario no debe pintar un borde nativo");
                Exigir(botonSecundario.FlatAppearance.BorderColor == Color.FromArgb(211, 219, 229), "El borde secundario debe ser gris suave");
            }
        }
    }
}