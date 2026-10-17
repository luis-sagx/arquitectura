using System;
using ec.edu.monster.controlador;
using ec.edu.monster.modelo;
using Microsoft.Maui.Controls;

namespace ec.edu.monster.vista
{
    /// <summary>Pantalla de acceso y recuperación de clave.</summary>
    public partial class LoginPage : ContentPage, IVistaLogin
    {
        private readonly ControladorLogin controlador;

        public LoginPage(string? aviso = null, bool error = false)
        {
            InitializeComponent();
            controlador = new ControladorLogin(this, Configuracion.EndpointSoap);
            if (aviso != null) MostrarMensaje(aviso, error);
        }

        public string UsuarioIngresado
        {
            get { return usuario.Text ?? String.Empty; }
        }

        public string ClaveIngresada
        {
            get { return clave.Text ?? String.Empty; }
        }

        public void LimpiarClave()
        {
            clave.Text = String.Empty;
        }

        public void EstablecerOcupado(bool ocupado)
        {
            progreso.IsVisible = ocupado;
            progreso.IsRunning = ocupado;
            ingresar.IsEnabled = !ocupado;
            recuperar.IsEnabled = !ocupado;
            usuario.IsEnabled = !ocupado;
            clave.IsEnabled = !ocupado;
        }

        public void MostrarMensaje(string texto, bool error)
        {
            mensaje.Text = texto;
            mensaje.TextColor = Color(error ? "Error" : "Gris");
        }

        public void PedirNuevaClave(string nombreUsuario)
        {
            mensajeRecuperar.Text = "Escribe la nueva contraseña para " + nombreUsuario + ".";
            nuevaClave.Text = String.Empty;
            confirmarClave.Text = String.Empty;
            panelRecuperar.IsVisible = true;
            nuevaClave.Focus();
        }

        public void AbrirConversiones(Uri endpoint, Sesion sesion)
        {
            Navegacion.Mostrar(new ConversionesPage(endpoint, sesion));
        }

        private void AlCompletarUsuario(object? sender, EventArgs e)
        {
            clave.Focus();
        }

        private async void AlIngresar(object? sender, EventArgs e)
        {
            await controlador.IngresarAsync();
        }

        private void AlRecuperar(object? sender, EventArgs e)
        {
            controlador.SolicitarRecuperacion();
        }

        private void AlCancelarRecuperacion(object? sender, EventArgs e)
        {
            panelRecuperar.IsVisible = false;
        }

        private async void AlConfirmarRecuperacion(object? sender, EventArgs e)
        {
            panelRecuperar.IsVisible = false;
            await controlador.RecuperarClaveAsync(nuevaClave.Text ?? String.Empty,
                confirmarClave.Text ?? String.Empty);
        }

        /// <summary>Si el panel está abierto, Atrás lo cierra en lugar de salir de la app.</summary>
        protected override bool OnBackButtonPressed()
        {
            if (!panelRecuperar.IsVisible) return base.OnBackButtonPressed();
            panelRecuperar.IsVisible = false;
            return true;
        }

        private static Microsoft.Maui.Graphics.Color Color(string clave)
        {
            return (Microsoft.Maui.Graphics.Color)Application.Current!.Resources[clave];
        }
    }
}
