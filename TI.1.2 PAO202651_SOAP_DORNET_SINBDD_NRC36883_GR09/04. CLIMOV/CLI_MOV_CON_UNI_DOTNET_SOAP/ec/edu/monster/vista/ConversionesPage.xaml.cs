using System;
using System.Collections.Generic;
using ec.edu.monster.controlador;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;
using Microsoft.Maui.Controls;

namespace ec.edu.monster.vista
{
    /// <summary>Pantalla de catálogo y conversión.</summary>
    public partial class ConversionesPage : ContentPage, IVistaConversiones
    {
        private readonly ControladorConversiones controlador;
        private bool iniciado;

        public ConversionesPage(Uri endpoint, Sesion sesion)
        {
            InitializeComponent();
            controlador = new ControladorConversiones(this, new ClienteSoap(endpoint.ToString()), sesion);
        }

        protected override async void OnAppearing()
        {
            base.OnAppearing();
            if (iniciado) return;
            iniciado = true;
            await controlador.IniciarAsync();
        }

        /// <summary>El botón Atrás de Android cierra la sesión local, igual que Salir.</summary>
        protected override bool OnBackButtonPressed()
        {
            controlador.CerrarSesion();
            return true;
        }

        public OpcionCatalogo? CategoriaSeleccionada
        {
            get { return categoria.SelectedItem as OpcionCatalogo; }
        }

        public OpcionCatalogo? OrigenSeleccionado
        {
            get { return origen.SelectedItem as OpcionCatalogo; }
        }

        public OpcionCatalogo? DestinoSeleccionado
        {
            get { return destino.SelectedItem as OpcionCatalogo; }
        }

        public string ValorIngresado
        {
            get { return valor.Text ?? String.Empty; }
        }

        public void EstablecerOcupado(bool ocupado)
        {
            progreso.IsVisible = ocupado;
            progreso.IsRunning = ocupado;
            categoria.IsEnabled = !ocupado;
            origen.IsEnabled = !ocupado;
            destino.IsEnabled = !ocupado;
            convertir.IsEnabled = !ocupado;
            intercambiar.IsEnabled = !ocupado;
        }

        public void MostrarEstado(string mensaje)
        {
            estado.Text = mensaje;
        }

        public async void MostrarAviso(string titulo, string mensaje)
        {
            await DisplayAlertAsync(titulo, mensaje, "Aceptar");
        }

        public void EstablecerCategorias(IList<OpcionCatalogo> categorias)
        {
            categoria.ItemsSource = new List<OpcionCatalogo>(categorias);
            if (categorias.Count > 0) categoria.SelectedIndex = 0;
        }

        public void EstablecerUnidades(IList<OpcionCatalogo> unidades)
        {
            origen.ItemsSource = new List<OpcionCatalogo>(unidades);
            destino.ItemsSource = new List<OpcionCatalogo>(unidades);
            if (unidades.Count > 0) origen.SelectedIndex = 0;
            if (unidades.Count > 1) destino.SelectedIndex = 1;
        }

        public void LimpiarUnidades()
        {
            origen.ItemsSource = null;
            destino.ItemsSource = null;
        }

        public void MostrarResultado(string texto)
        {
            resultado.Text = texto;
        }

        public void LimpiarResultado()
        {
            resultado.Text = "—";
        }

        public void VolverAlAcceso(string mensaje, bool error)
        {
            controlador.Liberar();
            Navegacion.Mostrar(new LoginPage(mensaje, error));
        }

        private async void AlCambiarCategoria(object? sender, EventArgs e)
        {
            await controlador.CambiarCategoriaAsync(CategoriaSeleccionada);
        }

        private void AlCambiarUnidad(object? sender, EventArgs e)
        {
            LimpiarResultado();
        }

        private void AlIntercambiar(object? sender, EventArgs e)
        {
            int posicionOrigen = origen.SelectedIndex;
            origen.SelectedIndex = destino.SelectedIndex;
            destino.SelectedIndex = posicionOrigen;
        }

        private async void AlConvertir(object? sender, EventArgs e)
        {
            valor.Unfocus();
            await controlador.ConvertirAsync();
        }

        private void AlCerrarSesion(object? sender, EventArgs e)
        {
            controlador.CerrarSesion();
        }
    }
}
