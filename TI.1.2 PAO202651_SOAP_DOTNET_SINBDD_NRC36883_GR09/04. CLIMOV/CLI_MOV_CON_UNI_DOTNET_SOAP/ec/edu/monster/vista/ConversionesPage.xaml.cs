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
        private View vistaConversor;
        private bool catalogoVisible;

        public ConversionesPage(Uri endpoint, Sesion sesion)
        {
            InitializeComponent();
            vistaConversor = Content;
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
            if (catalogoVisible) { Content = vistaConversor; catalogoVisible = false; return true; }
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
            verCatalogo.IsEnabled = !ocupado;
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

        public void MostrarCatalogo(string texto)
        {
            var raiz = new Grid();
            raiz.RowDefinitions.Add(new RowDefinition(GridLength.Auto));
            raiz.RowDefinitions.Add(new RowDefinition(new GridLength(1, GridUnitType.Star)));
            raiz.BackgroundColor = Color.FromArgb("#F1F4F8");

            var encabezado = new Grid { Padding = new Thickness(20, 18) };
            encabezado.ColumnDefinitions.Add(new ColumnDefinition(new GridLength(1, GridUnitType.Star)));
            encabezado.ColumnDefinitions.Add(new ColumnDefinition(GridLength.Auto));
            encabezado.Children.Add(new Label { Text = "Catálogo de unidades", FontSize = 22, FontAttributes = FontAttributes.Bold, TextColor = Color.FromArgb("#183257"), VerticalTextAlignment = TextAlignment.Center });
            var volver = new Button { Text = "Volver al conversor", BackgroundColor = Color.FromArgb("#E8EDF4"), TextColor = Color.FromArgb("#183257"), CornerRadius = 10 };
            volver.Clicked += (sender, args) => { Content = vistaConversor; catalogoVisible = false; };
            encabezado.Children.Add(volver, 1, 0);

            var cuadrícula = new Grid { ColumnSpacing = 16, RowSpacing = 16, Padding = new Thickness(20, 8) };
            cuadrícula.ColumnDefinitions.Add(new ColumnDefinition(new GridLength(1, GridUnitType.Star)));
            cuadrícula.ColumnDefinitions.Add(new ColumnDefinition(new GridLength(1, GridUnitType.Star)));
            VerticalStackLayout lista = null;
            int indice = 0;
            foreach (string linea in texto.Split(new[] { '\r', '\n' }, StringSplitOptions.RemoveEmptyEntries))
            {
                string limpio = linea.Trim();
                if (limpio.StartsWith("Conversiones disponibles")) continue;
                bool esUnidad = linea.StartsWith(" ") || limpio.StartsWith("•");
                if (!esUnidad)
                {
                    int fila = indice / 2;
                    if (cuadrícula.RowDefinitions.Count <= fila) cuadrícula.RowDefinitions.Add(new RowDefinition(GridLength.Auto));
                    var tarjeta = new Border { BackgroundColor = Colors.White, Stroke = Color.FromArgb("#D3DBE5"), StrokeShape = new RoundRectangle { CornerRadius = 18 }, Padding = 18 };
                    lista = new VerticalStackLayout { Spacing = 8 };
                    lista.Children.Add(new Label { Text = limpio, FontSize = 16, FontAttributes = FontAttributes.Bold, TextColor = Color.FromArgb("#183257") });
                    tarjeta.Content = lista;
                    Grid.SetColumn(tarjeta, indice % 2);
                    Grid.SetRow(tarjeta, fila);
                    cuadrícula.Children.Add(tarjeta);
                    indice++;
                }
                else if (lista != null)
                {
                    if (limpio.StartsWith("•")) limpio = limpio.Substring(1).Trim();
                    lista.Children.Add(new Label { Text = limpio, FontSize = 14, TextColor = Color.FromArgb("#374352") });
                }
            }

            var scroll = new ScrollView { Content = cuadrícula };
            raiz.Children.Add(encabezado);
            Grid.SetRow(scroll, 1);
            raiz.Children.Add(scroll);
            Content = raiz;
            catalogoVisible = true;
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

        private async void AlConsultarCatalogo(object? sender, EventArgs e)
        {
            await controlador.ConsultarCatalogoAsync();
        }

        private void AlCerrarSesion(object? sender, EventArgs e)
        {
            controlador.CerrarSesion();
        }
    }
}
