using System;
using System.Windows.Forms;

namespace ec.edu.monster.vista
{
    /// <summary>Centraliza la navegación y mantiene una sola instancia de cada pantalla.</summary>
    public sealed class GestorVentanas
    {
        private static readonly GestorVentanas instancia = new GestorVentanas();
        private LoginForm acceso;
        private ConversionForm conversiones;

        private GestorVentanas() { }

        public static GestorVentanas Instancia
        {
            get { return instancia; }
        }

        public void RegistrarAcceso(LoginForm ventana)
        {
            if (ventana == null) throw new ArgumentNullException("ventana");
            if (acceso != null && !acceso.IsDisposed && !Object.ReferenceEquals(acceso, ventana))
                throw new InvalidOperationException("La ventana de acceso ya fue registrada.");
            acceso = ventana;
        }

        public void MostrarAcceso(string mensaje, bool esError)
        {
            if (acceso == null || acceso.IsDisposed)
                throw new InvalidOperationException("Primero debe registrarse la ventana de acceso.");

            acceso.LimpiarClave();
            if (!String.IsNullOrWhiteSpace(mensaje)) acceso.MostrarMensaje(mensaje, esError);
            acceso.WindowState = FormWindowState.Normal;
            acceso.Show();
            acceso.BringToFront();
            acceso.Activate();
        }

        public ConversionForm MostrarConversiones(Func<ConversionForm> crearVentana)
        {
            if (crearVentana == null) throw new ArgumentNullException("crearVentana");
            if (acceso == null || acceso.IsDisposed)
                throw new InvalidOperationException("Primero debe registrarse la ventana de acceso.");

            if (conversiones != null && !conversiones.IsDisposed)
            {
                conversiones.WindowState = FormWindowState.Normal;
                conversiones.Show();
                conversiones.BringToFront();
                conversiones.Activate();
                acceso.Hide();
                return conversiones;
            }

            ConversionForm nueva = crearVentana();
            if (nueva == null) throw new InvalidOperationException("La fábrica no creó una ventana de conversión.");
            conversiones = nueva;
            nueva.FormClosed += ConversionesCerrada;
            acceso.Hide();
            nueva.StartPosition = FormStartPosition.CenterScreen;
            nueva.Show();
            nueva.BringToFront();
            nueva.Activate();
            return nueva;
        }

        public void VolverAlAcceso(string mensaje, bool esError)
        {
            ConversionForm anterior = conversiones;
            conversiones = null;
            if (anterior != null && !anterior.IsDisposed)
            {
                anterior.FormClosed -= ConversionesCerrada;
                anterior.Close();
            }
            MostrarAcceso(mensaje, esError);
        }

        private void ConversionesCerrada(object sender, FormClosedEventArgs evento)
        {
            if (!Object.ReferenceEquals(sender, conversiones)) return;
            conversiones = null;
            if (acceso != null && !acceso.IsDisposed) MostrarAcceso(null, false);
        }
    }
}
