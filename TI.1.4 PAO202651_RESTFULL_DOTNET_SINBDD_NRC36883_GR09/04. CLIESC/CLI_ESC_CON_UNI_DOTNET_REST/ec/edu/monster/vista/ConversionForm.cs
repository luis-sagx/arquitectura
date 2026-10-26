using System;
using System.Collections.Generic;
using System.Drawing;
using System.Globalization;
using System.Windows.Forms;
using ec.edu.monster.modelo;

namespace ec.edu.monster.vista
{
    public sealed class ConversionForm : Form
    {
        private readonly ComboBox categorias;
        private readonly ComboBox origen;
        private readonly ComboBox destino;
        private readonly EntradaRedondeada valor;
        private readonly Button convertir;
        private readonly Button catalogo;
        private readonly Button salir;
        private readonly Label resultado;
        private readonly Label mensaje;
        private readonly Label sesion;
        private readonly ToolTip ayudaMensajes;
        private bool actualizandoListas;
        private Control[] controlesConversor;
        private string tituloConversor;

        public ConversionForm()
        {
            EstilosUI.Configurar(this, "Monster · Conversión de unidades");
            Controls.Add(EstilosUI.Etiqueta("MONSTER", 40, 28, 180, 30, 12F, true));
            Controls[0].ForeColor = EstilosUI.Acento;
            Controls.Add(EstilosUI.Etiqueta("Conversor de unidades", 40, 66, 520, 42, 23F, true));
            var subtitulo = EstilosUI.Etiqueta("Selecciona una magnitud, las unidades y el valor.", 40, 112, 540, 28, 10F, false);
            subtitulo.ForeColor = EstilosUI.TextoSecundario;
            Controls.Add(subtitulo);

            salir = EstilosUI.Boton("Cerrar sesión", 610, 34, 140, 38, false);
            salir.Click += (sender, args) => Disparar(CerrarSesionSolicitado);
            Controls.Add(salir);

            var tarjeta = EstilosUI.Tarjeta(new Point(40, 166), new Size(710, 390));
            tarjeta.Controls.Add(EstilosUI.Etiqueta("Categoría", 28, 26, 300, 24, 10F, true));
            categorias = CrearCombo(28, 54, 654);
            categorias.SelectedIndexChanged += (sender, args) =>
            {
                if (!actualizandoListas) Disparar(CategoriaCambiadaSolicitado);
            };
            tarjeta.Controls.Add(categorias);

            tarjeta.Controls.Add(EstilosUI.Etiqueta("Unidad de origen", 28, 119, 300, 24, 10F, true));
            origen = CrearCombo(28, 147, 315);
            tarjeta.Controls.Add(origen);
            tarjeta.Controls.Add(EstilosUI.Etiqueta("Unidad de destino", 367, 119, 300, 24, 10F, true));
            destino = CrearCombo(367, 147, 315);
            tarjeta.Controls.Add(destino);

            tarjeta.Controls.Add(EstilosUI.Etiqueta("Valor", 28, 211, 300, 24, 10F, true));
            valor = EstilosUI.Entrada(28, 239, 654, 42, false);
            tarjeta.Controls.Add(valor);
            convertir = EstilosUI.Boton("Convertir", 28, 293, 315, 44, true);
            convertir.Click += (sender, args) => Disparar(ConvertirSolicitado);
            tarjeta.Controls.Add(convertir);

            catalogo = EstilosUI.Boton("Ver catalogo", 367, 293, 315, 44, false);
            catalogo.Click += (sender, args) => Disparar(CatalogoSolicitado);
            tarjeta.Controls.Add(catalogo);

            resultado = EstilosUI.Etiqueta("El resultado aparecerá aquí", 28, 338, 654, 34, 18F, true);
            tarjeta.Controls.Add(resultado);
            sesion = EstilosUI.Etiqueta(String.Empty, 500, 568, 250, 24, 9F, false);
            sesion.ForeColor = EstilosUI.TextoSecundario;
            sesion.TextAlign = ContentAlignment.MiddleRight;
            Controls.Add(tarjeta);
            Controls.Add(sesion);

            mensaje = EstilosUI.Etiqueta("Conectando con el servicio…", 40, 568, 440, 24, 9F, false);
            mensaje.AutoEllipsis = true;
            mensaje.ForeColor = EstilosUI.TextoSecundario;
            Controls.Add(mensaje);
            ayudaMensajes = new ToolTip
            {
                AutoPopDelay = 10000,
                InitialDelay = 500,
                ReshowDelay = 100,
                ShowAlways = true
            };
            ayudaMensajes.SetToolTip(mensaje, mensaje.Text);
            Controls.Add(EstilosUI.CrearImagenLateral());
            tituloConversor = Text;
            controlesConversor = new Control[Controls.Count];
            Controls.CopyTo(controlesConversor, 0);
        }

        public event EventHandler CategoriaCambiadaSolicitado;
        public event EventHandler ConvertirSolicitado;
        public event EventHandler CatalogoSolicitado;
        public event EventHandler CerrarSesionSolicitado;

        public OpcionCatalogo CategoriaSeleccionada { get { return categorias.SelectedItem as OpcionCatalogo; } }
        public OpcionCatalogo UnidadOrigenSeleccionada { get { return origen.SelectedItem as OpcionCatalogo; } }
        public OpcionCatalogo UnidadDestinoSeleccionada { get { return destino.SelectedItem as OpcionCatalogo; } }
        public string ValorIngresado { get { return valor.Text; } }

        public void MostrarSesion(Sesion actual)
        {
            if (actual == null || String.IsNullOrWhiteSpace(actual.ExpiraEn))
            {
                sesion.Text = "Sesión activa";
                return;
            }

            DateTimeOffset expira;
            if (DateTimeOffset.TryParse(actual.ExpiraEn, CultureInfo.InvariantCulture,
                DateTimeStyles.AssumeUniversal, out expira))
            {
                string fechaLocal = expira.ToLocalTime().ToString("dd/MM/yyyy HH:mm", CultureInfo.GetCultureInfo("es-EC"));
                sesion.Text = "Sesión · vence " + fechaLocal;
            }
            else
            {
                sesion.Text = "Sesión activa · vence " + actual.ExpiraEn;
            }
        }

        public void CargarCategorias(IEnumerable<string> lista)
        {
            actualizandoListas = true;
            try
            {
                categorias.Items.Clear();
                if (lista != null)
                    foreach (string item in lista)
                        if (!String.IsNullOrWhiteSpace(item)) categorias.Items.Add(new OpcionCatalogo(item, TextoAmigable(item)));
                if (categorias.Items.Count > 0) categorias.SelectedIndex = 0;
            }
            finally { actualizandoListas = false; }
            LimpiarUnidades();
        }

        public void CargarUnidades(IEnumerable<string> lista)
        {
            actualizandoListas = true;
            try
            {
                origen.Items.Clear();
                destino.Items.Clear();
                if (lista != null)
                    foreach (string item in lista)
                    {
                        if (String.IsNullOrWhiteSpace(item)) continue;
                        var opcion = new OpcionCatalogo(item, TextoAmigable(item));
                        origen.Items.Add(opcion);
                        destino.Items.Add(new OpcionCatalogo(item, TextoAmigable(item)));
                    }
                if (origen.Items.Count > 0) origen.SelectedIndex = 0;
                if (destino.Items.Count > 1) destino.SelectedIndex = 1;
                else if (destino.Items.Count == 1) destino.SelectedIndex = 0;
            }
            finally { actualizandoListas = false; }
            LimpiarResultado();
        }

        public void LimpiarUnidades()
        {
            actualizandoListas = true;
            try
            {
                origen.Items.Clear();
                destino.Items.Clear();
            }
            finally { actualizandoListas = false; }
            LimpiarResultado();
        }

        public void MostrarResultado(double valorIngresado, RespuestaRest respuesta)
        {
            if (respuesta == null || !respuesta.IsExito || !respuesta.Resultado.HasValue)
            {
                LimpiarResultado();
                return;
            }
            resultado.Text = valorIngresado.ToString("G", System.Globalization.CultureInfo.CurrentCulture)
                + " " + respuesta.UnidadOrigen + " = "
                + Math.Round(respuesta.Resultado.Value, 4, MidpointRounding.AwayFromZero)
                    .ToString("0.####", System.Globalization.CultureInfo.CurrentCulture)
                + " " + respuesta.UnidadDestino;
            MostrarMensaje(String.Empty, false);
        }

        public void MostrarCatalogo(string texto)
        {
            var encabezado = new Panel { Dock = DockStyle.Fill, Padding = new Padding(28, 16, 28, 12), BackColor = EstilosUI.Fondo };
            var titulo = new Label { Text = "Catálogo de unidades", Dock = DockStyle.Left, Width = 420, Font = new Font("Segoe UI", 22F, FontStyle.Bold), ForeColor = EstilosUI.Tinta, TextAlign = ContentAlignment.MiddleLeft };
            var volver = EstilosUI.Boton("Volver al conversor", 0, 0, 190, 42, false);
            volver.Dock = DockStyle.Right;
            volver.Click += (sender, args) =>
            {
                Controls.Clear();
                Controls.AddRange(controlesConversor);
                Text = tituloConversor;
            };
            encabezado.Controls.Add(titulo);
            encabezado.Controls.Add(volver);
            var cuadrícula = new FlowLayoutPanel { Dock = DockStyle.Fill, AutoScroll = false, WrapContents = true, FlowDirection = FlowDirection.LeftToRight, Padding = new Padding(28), BackColor = EstilosUI.Fondo };
            Panel tarjeta = null;
            FlowLayoutPanel lista = null;
            string conversionesHabilitadas = "Conversiones habilitadas: longitud, masa/peso y temperatura.";
            foreach (string linea in texto.Split(new[] { '\r', '\n' }, StringSplitOptions.RemoveEmptyEntries))
            {
                string limpio = linea.Trim();
                if (limpio.StartsWith("Conversiones habilitadas:"))
                {
                    conversionesHabilitadas = limpio;
                    continue;
                }
                if (limpio.StartsWith("Conversiones disponibles")) continue;
                bool esUnidad = linea.StartsWith(" ") || limpio.StartsWith("•");
                if (!esUnidad)
                {
                    tarjeta = new Panel { Width = 300, AutoSize = true, MinimumSize = new Size(280, 120), BackColor = EstilosUI.Blanco, Padding = new Padding(18), Margin = new Padding(0, 0, 18, 18) };
                    tarjeta.Controls.Add(new Label { Text = limpio, Dock = DockStyle.Top, Height = 30, Font = new Font("Segoe UI", 14F, FontStyle.Bold), ForeColor = EstilosUI.Acento });
                    lista = new FlowLayoutPanel { Dock = DockStyle.Top, AutoSize = true, FlowDirection = FlowDirection.TopDown, WrapContents = false, BackColor = EstilosUI.Blanco };
                    tarjeta.Controls.Add(lista);
                    cuadrícula.Controls.Add(tarjeta);
                }
                else if (lista != null)
                {
                    if (limpio.StartsWith("•")) limpio = limpio.Substring(1).Trim();
                    lista.Controls.Add(new Label { Text = limpio, AutoSize = true, Font = new Font("Segoe UI", 11F), ForeColor = EstilosUI.Tinta, Margin = new Padding(0, 4, 0, 4) });
                }
            }
            var diseño = new TableLayoutPanel
            {
                Dock = DockStyle.Fill,
                ColumnCount = 1,
                RowCount = 3,
                BackColor = EstilosUI.Fondo,
                Margin = Padding.Empty
            };
            diseño.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100F));
            diseño.RowStyles.Add(new RowStyle(SizeType.Absolute, 76F));
            diseño.RowStyles.Add(new RowStyle(SizeType.Percent, 100F));
            diseño.RowStyles.Add(new RowStyle(SizeType.Absolute, 44F));
            var resumen = new Label
            {
                Text = conversionesHabilitadas,
                Dock = DockStyle.Fill,
                AutoEllipsis = true,
                Padding = new Padding(28, 8, 28, 8),
                Font = new Font("Segoe UI", 10F),
                ForeColor = EstilosUI.TextoSecundario,
                TextAlign = ContentAlignment.MiddleLeft
            };
            diseño.Controls.Add(encabezado, 0, 0);
            diseño.Controls.Add(cuadrícula, 0, 1);
            diseño.Controls.Add(resumen, 0, 2);

            Controls.Clear();
            Controls.Add(diseño);
            Text = "Catálogo de unidades";
        }

        public void LimpiarResultado()
        {
            resultado.Text = "El resultado aparecerá aquí";
        }

        public void MostrarMensaje(string texto, bool esError)
        {
            mensaje.Text = texto ?? String.Empty;
            mensaje.ForeColor = esError ? Color.FromArgb(190, 44, 58) : EstilosUI.TextoSecundario;
            ayudaMensajes.SetToolTip(mensaje, mensaje.Text);
        }

        public void EstablecerOcupado(bool ocupado)
        {
            categorias.Enabled = !ocupado;
            origen.Enabled = !ocupado;
            destino.Enabled = !ocupado;
            valor.Enabled = !ocupado;
            convertir.Enabled = !ocupado;
            catalogo.Enabled = !ocupado;
            salir.Enabled = !ocupado;
            UseWaitCursor = ocupado;
        }

        private static ComboBox CrearCombo(int x, int y, int ancho)
        {
            var combo = new ComboBox
            {
                Location = new Point(x, y),
                Size = new Size(ancho, 42),
                DropDownStyle = ComboBoxStyle.DropDownList,
                DrawMode = DrawMode.OwnerDrawFixed,
                ItemHeight = 36,
                Font = new Font("Segoe UI", 10F),
                BackColor = EstilosUI.Blanco,
                ForeColor = EstilosUI.Tinta
            };
            combo.DrawItem += (sender, evento) =>
            {
                evento.DrawBackground();
                if (evento.Index < 0) return;
                string texto = Convert.ToString(combo.Items[evento.Index]);
                Color colorTexto = (evento.State & DrawItemState.Selected) == DrawItemState.Selected
                    ? SystemColors.HighlightText : EstilosUI.Tinta;
                using (var pincel = new SolidBrush(colorTexto))
                    evento.Graphics.DrawString(texto, combo.Font, pincel,
                        evento.Bounds.Left + 8, evento.Bounds.Top + (evento.Bounds.Height - combo.Font.Height) / 2F);
                evento.DrawFocusRectangle();
            };
            return combo;
        }

        private static string TextoAmigable(string codigo)
        {
            string texto = (codigo ?? String.Empty).ToLowerInvariant().Replace('_', ' ');
            return System.Globalization.CultureInfo.CurrentCulture.TextInfo.ToTitleCase(texto);
        }

        private void Disparar(EventHandler evento)
        {
            if (evento != null) evento(this, EventArgs.Empty);
        }
    }
}
