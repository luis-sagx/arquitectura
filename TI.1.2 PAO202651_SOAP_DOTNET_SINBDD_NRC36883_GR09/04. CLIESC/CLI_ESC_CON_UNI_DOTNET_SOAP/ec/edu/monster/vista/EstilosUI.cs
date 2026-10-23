using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.IO;
using System.Windows.Forms;

namespace ec.edu.monster.vista
{
    internal static class EstilosUI
    {
        internal static readonly Color Fondo = Color.FromArgb(241, 244, 248);
        internal static readonly Color Tinta = Color.FromArgb(29, 41, 57);
        internal static readonly Color TextoSecundario = Color.FromArgb(91, 105, 122);
        internal static readonly Color Acento = Color.FromArgb(24, 50, 87);
        internal static readonly Color AcentoHover = Color.FromArgb(35, 68, 112);
        internal static readonly Color Suave = Color.FromArgb(232, 237, 244);
        internal static readonly Color Borde = Color.FromArgb(211, 219, 229);
        internal static readonly Color Blanco = Color.FromArgb(252, 253, 254);
        private const int RadioEsquina = 6;

        internal static void Configurar(Form form, string titulo)
        {
            form.Text = titulo;
            form.AutoScaleMode = AutoScaleMode.None;
            form.Font = new Font("Segoe UI", 10F, FontStyle.Regular);
            form.StartPosition = FormStartPosition.CenterScreen;
            form.FormBorderStyle = FormBorderStyle.FixedSingle;
            form.MaximizeBox = false;
            form.MinimizeBox = false;
            form.BackColor = Fondo;
            form.Size = new Size(1150, 700);
        }

        internal static Control CrearImagenLateral()
        {
            return new PanelImagenLateral();
        }

        internal static Label Etiqueta(string texto, int x, int y, int ancho, int alto, float tamano, bool negrita)
        {
            return new Label
            {
                Text = texto,
                Location = new Point(x, y),
                Size = new Size(ancho, alto),
                ForeColor = Tinta,
                Font = new Font("Segoe UI", tamano, negrita ? FontStyle.Bold : FontStyle.Regular),
                AutoEllipsis = true,
                BackColor = Color.Transparent
            };
        }

        internal static Panel Tarjeta(Point ubicacion, Size tamano)
        {
            return new PanelRedondeado
            {
                Location = ubicacion,
                Size = tamano,
                BackColor = Blanco,
                Padding = new Padding(RadioEsquina)
            };
        }

        internal static Button Boton(string texto, int x, int y, int ancho, int alto, bool primario)
        {
            var boton = new BotonRedondeado(primario)
            {
                Text = texto,
                Location = new Point(x, y),
                Size = new Size(ancho, alto),
                FlatStyle = FlatStyle.Flat,
                BackColor = primario ? Acento : Blanco,
                ForeColor = primario ? Blanco : Acento,
                Font = new Font("Segoe UI", 10F, FontStyle.Bold),
                Cursor = Cursors.Hand,
                TabStop = true
            };
            boton.FlatAppearance.BorderSize = 0;
            boton.FlatAppearance.BorderColor = primario ? Acento : Borde;
            return boton;
        }

        internal static EntradaRedondeada Entrada(int x, int y, int ancho, int alto, bool secreto)
        {
            return new EntradaRedondeada
            {
                Location = new Point(x, y),
                Size = new Size(ancho, alto),
                UseSystemPasswordChar = secreto,
                TabStop = true
            };
        }

        internal static GraphicsPath Ruta(Rectangle limites, int radio)
        {
            int diametro = radio * 2;
            var ruta = new GraphicsPath();
            if (limites.Width <= 0 || limites.Height <= 0) return ruta;
            ruta.AddArc(limites.Left, limites.Top, diametro, diametro, 180, 90);
            ruta.AddArc(limites.Right - diametro - 1, limites.Top, diametro, diametro, 270, 90);
            ruta.AddArc(limites.Right - diametro - 1, limites.Bottom - diametro - 1, diametro, diametro, 0, 90);
            ruta.AddArc(limites.Left, limites.Bottom - diametro - 1, diametro, diametro, 90, 90);
            ruta.CloseFigure();
            return ruta;
        }

        private sealed class PanelImagenLateral : Panel
        {
            private readonly Image imagen;

            internal PanelImagenLateral()
            {
                Dock = DockStyle.Right;
                Width = 380;
                BackColor = Acento;
                DoubleBuffered = true;
                AccessibleDescription = "Monster usando una computadora";
                string ruta = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "Recursos", "monster.jpg");
                using (Image original = Image.FromFile(ruta))
                    imagen = new Bitmap(original);
            }

            protected override void OnPaint(PaintEventArgs evento)
            {
                base.OnPaint(evento);
                if (imagen == null || Width <= 0 || Height <= 0) return;
                float escala = Math.Max((float)Width / imagen.Width, (float)Height / imagen.Height);
                int ancho = (int)Math.Ceiling(imagen.Width * escala);
                int alto = (int)Math.Ceiling(imagen.Height * escala);
                var destino = new Rectangle((Width - ancho) / 2, (Height - alto) / 2, ancho, alto);
                evento.Graphics.InterpolationMode = InterpolationMode.HighQualityBicubic;
                evento.Graphics.PixelOffsetMode = PixelOffsetMode.HighQuality;
                evento.Graphics.DrawImage(imagen, destino);
            }

            protected override void Dispose(bool disposing)
            {
                if (disposing) imagen.Dispose();
                base.Dispose(disposing);
            }
        }

        private sealed class PanelRedondeado : Panel
        {
            private Region regionRedondeada;

            internal PanelRedondeado()
            {
                SetStyle(ControlStyles.UserPaint | ControlStyles.AllPaintingInWmPaint | ControlStyles.OptimizedDoubleBuffer, true);
                BorderStyle = BorderStyle.None;
            }

            protected override void OnSizeChanged(EventArgs evento)
            {
                base.OnSizeChanged(evento);
                if (regionRedondeada != null) regionRedondeada.Dispose();
                using (GraphicsPath ruta = Ruta(ClientRectangle, RadioEsquina))
                {
                    regionRedondeada = new Region(ruta);
                    Region = regionRedondeada;
                }
                Invalidate();
            }

            protected override void OnPaint(PaintEventArgs evento)
            {
                base.OnPaint(evento);
                evento.Graphics.SmoothingMode = SmoothingMode.AntiAlias;
                Rectangle limites = ClientRectangle;
                limites.Width--;
                limites.Height--;
                using (GraphicsPath ruta = Ruta(limites, RadioEsquina))
                using (var borde = new Pen(Borde, 1F))
                    evento.Graphics.DrawPath(borde, ruta);
            }

            protected override void Dispose(bool disposing)
            {
                if (disposing && regionRedondeada != null) regionRedondeada.Dispose();
                base.Dispose(disposing);
            }
        }

        private sealed class BotonRedondeado : Button
        {
            private readonly bool primario;
            private Region regionRedondeada;

            internal BotonRedondeado(bool primario)
            {
                this.primario = primario;
                FlatAppearance.BorderSize = 0;
                FlatAppearance.MouseDownBackColor = AcentoHover;
                UseVisualStyleBackColor = false;
                SetStyle(ControlStyles.UserPaint | ControlStyles.AllPaintingInWmPaint | ControlStyles.OptimizedDoubleBuffer | ControlStyles.ResizeRedraw, true);
                FlatStyle = FlatStyle.Flat;
            }

            protected override void OnSizeChanged(EventArgs evento)
            {
                base.OnSizeChanged(evento);
                if (regionRedondeada != null) regionRedondeada.Dispose();
                using (GraphicsPath ruta = Ruta(ClientRectangle, RadioEsquina))
                {
                    regionRedondeada = new Region(ruta);
                    Region = regionRedondeada;
                }
            }

            protected override void OnMouseEnter(EventArgs evento)
            {
                base.OnMouseEnter(evento);
                Invalidate();
            }

            protected override void OnMouseLeave(EventArgs evento)
            {
                base.OnMouseLeave(evento);
                Invalidate();
            }

            protected override void OnEnabledChanged(EventArgs evento)
            {
                base.OnEnabledChanged(evento);
                Invalidate();
            }

            protected override void OnPaint(PaintEventArgs evento)
            {
                Graphics grafico = evento.Graphics;
                grafico.SmoothingMode = SmoothingMode.AntiAlias;
                Rectangle limites = ClientRectangle;
                limites.Width--;
                limites.Height--;
                using (GraphicsPath ruta = Ruta(limites, RadioEsquina))
                {
                    bool sobre = ClientRectangle.Contains(PointToClient(Cursor.Position));
                    Color fondo = !Enabled ? Color.FromArgb(218, 223, 231)
                        : (sobre && primario ? AcentoHover : (sobre && !primario ? Suave : BackColor));
                    using (var pincel = new SolidBrush(fondo)) grafico.FillPath(pincel, ruta);
                    if (!primario)
                        using (var borde = new Pen(Borde, 1F)) grafico.DrawPath(borde, ruta);

                    Color texto = Enabled ? ForeColor : TextoSecundario;
                    TextRenderer.DrawText(grafico, Text, Font, ClientRectangle, texto,
                        TextFormatFlags.HorizontalCenter | TextFormatFlags.VerticalCenter | TextFormatFlags.EndEllipsis | TextFormatFlags.SingleLine);
                    if (Focused && ShowFocusCues)
                    {
                        Rectangle foco = ClientRectangle;
                        foco.Inflate(-10, -7);
                        ControlPaint.DrawFocusRectangle(grafico, foco, texto, fondo);
                    }
                }
            }

            protected override void Dispose(bool disposing)
            {
                if (disposing && regionRedondeada != null) regionRedondeada.Dispose();
                base.Dispose(disposing);
            }
        }
    }

    internal sealed class EntradaRedondeada : UserControl
    {
        private readonly TextBox editor;
        private bool enfocada;

        internal EntradaRedondeada()
        {
            SetStyle(ControlStyles.UserPaint | ControlStyles.AllPaintingInWmPaint | ControlStyles.OptimizedDoubleBuffer, true);
            BackColor = EstilosUI.Blanco;
            ForeColor = EstilosUI.Tinta;
            MinimumSize = new Size(80, 36);
            editor = new TextBox
            {
                BorderStyle = BorderStyle.None,
                BackColor = EstilosUI.Blanco,
                ForeColor = EstilosUI.Tinta,
                Font = new Font("Segoe UI", 11F, FontStyle.Regular),
                Location = new Point(12, 9),
                Anchor = AnchorStyles.Left | AnchorStyles.Right | AnchorStyles.Top
            };
            editor.Enter += (sender, evento) => { enfocada = true; Invalidate(); };
            editor.Leave += (sender, evento) => { enfocada = false; Invalidate(); };
            editor.TextChanged += (sender, evento) => OnTextChanged(evento);
            Controls.Add(editor);
            TabStop = true;
        }

        public override string Text
        {
            get { return editor == null ? base.Text : editor.Text; }
            set
            {
                if (editor == null) base.Text = value;
                else editor.Text = value;
            }
        }

        public bool UseSystemPasswordChar
        {
            get { return editor.UseSystemPasswordChar; }
            set { editor.UseSystemPasswordChar = value; }
        }

        public void Clear()
        {
            editor.Clear();
        }

        protected override void OnResize(EventArgs evento)
        {
            base.OnResize(evento);
            if (editor != null)
            {
                editor.Location = new Point(12, Math.Max(1, (Height - editor.PreferredHeight) / 2));
                editor.Width = Math.Max(0, Width - 24);
            }
            Invalidate();
        }

        protected override void OnEnter(EventArgs evento)
        {
            base.OnEnter(evento);
            editor.Focus();
        }

        protected override void OnClick(EventArgs evento)
        {
            base.OnClick(evento);
            if (Enabled) editor.Focus();
        }

        protected override void OnPaint(PaintEventArgs evento)
        {
            base.OnPaint(evento);
            evento.Graphics.SmoothingMode = SmoothingMode.AntiAlias;
            Rectangle limites = ClientRectangle;
            limites.Width--;
            limites.Height--;
            using (GraphicsPath ruta = EstilosUI.Ruta(limites, 6))
            using (var borde = new Pen(enfocada ? EstilosUI.Acento : EstilosUI.Borde, enfocada ? 1.4F : 1F))
                evento.Graphics.DrawPath(borde, ruta);
        }
    }
}
