using System;
using System.Drawing;
using System.Windows.Forms;

namespace ec.edu.monster.vista
{
    public partial class LoginForm : Form
    {
        private readonly EntradaRedondeada usuario;
        private readonly EntradaRedondeada clave;
        private readonly Button ingresar;
        private readonly Button recuperar;
        private readonly Label mensaje;

        public LoginForm()
        {
            InitializeComponent();
            EstilosUI.Configurar(this, "Monster · Iniciar sesión");

            var tarjeta = EstilosUI.Tarjeta(new Point(150, 52), new Size(490, 548));

            var marca = EstilosUI.Etiqueta("MONSTER", 44, 40, 360, 32, 13F, true);
            marca.ForeColor = EstilosUI.Acento;
            var titulo = EstilosUI.Etiqueta("Conversor de unidades", 44, 93, 390, 42, 23F, true);
            var subtitulo = EstilosUI.Etiqueta("Inicia sesión para consultar el catálogo y convertir.", 44, 140, 390, 40, 10F, false);
            subtitulo.ForeColor = EstilosUI.TextoSecundario;
            tarjeta.Controls.Add(marca);
            tarjeta.Controls.Add(titulo);
            tarjeta.Controls.Add(subtitulo);

            tarjeta.Controls.Add(EstilosUI.Etiqueta("Usuario", 44, 206, 390, 24, 10F, true));
            usuario = EstilosUI.Entrada(44, 234, 390, 40, false);
            tarjeta.Controls.Add(usuario);
            tarjeta.Controls.Add(EstilosUI.Etiqueta("Contraseña", 44, 296, 390, 24, 10F, true));
            clave = EstilosUI.Entrada(44, 324, 390, 40, true);
            tarjeta.Controls.Add(clave);

            ingresar = EstilosUI.Boton("Iniciar sesión", 44, 390, 390, 46, true);
            ingresar.Click += (sender, args) => Disparar(IngresarSolicitado);
            tarjeta.Controls.Add(ingresar);
            recuperar = EstilosUI.Boton("Recuperar contraseña", 44, 448, 390, 42, false);
            recuperar.Click += (sender, args) => Disparar(RecuperarSolicitado);
            tarjeta.Controls.Add(recuperar);

            mensaje = EstilosUI.Etiqueta(String.Empty, 44, 496, 390, 38, 9F, false);
            mensaje.ForeColor = EstilosUI.TextoSecundario;
            tarjeta.Controls.Add(mensaje);
            Controls.Add(tarjeta);

            AcceptButton = ingresar;
        }

        public event EventHandler IngresarSolicitado;
        public event EventHandler RecuperarSolicitado;

        public string UsuarioIngresado { get { return usuario.Text.Trim(); } }
        public char[] ClaveIngresada { get { return clave.Text.ToCharArray(); } }

        public string PedirNuevaClave()
        {
            using (var dialogo = new Form())
            using (var entrada = EstilosUI.Entrada(22, 48, 330, 34, true))
            using (var aceptar = EstilosUI.Boton("Actualizar", 176, 102, 176, 38, true))
            using (var cancelar = EstilosUI.Boton("Cancelar", 22, 102, 142, 38, false))
            {
                EstilosUI.Configurar(dialogo, "Recuperar contraseña");
                dialogo.ClientSize = new Size(374, 166);
                dialogo.FormBorderStyle = FormBorderStyle.FixedDialog;
                dialogo.MaximizeBox = false;
                dialogo.MinimizeBox = false;
                dialogo.StartPosition = FormStartPosition.CenterParent;
                dialogo.Controls.Add(EstilosUI.Etiqueta("Escribe la nueva contraseña", 22, 18, 330, 24, 10F, true));
                dialogo.Controls.Add(entrada);
                aceptar.DialogResult = DialogResult.OK;
                cancelar.DialogResult = DialogResult.Cancel;
                dialogo.Controls.Add(aceptar);
                dialogo.Controls.Add(cancelar);
                dialogo.AcceptButton = aceptar;
                dialogo.CancelButton = cancelar;
                return dialogo.ShowDialog(this) == DialogResult.OK ? entrada.Text : null;
            }
        }

        public void MostrarMensaje(string texto, bool esError)
        {
            mensaje.Text = texto ?? String.Empty;
            mensaje.ForeColor = esError ? Color.FromArgb(190, 44, 58) : EstilosUI.TextoSecundario;
        }

        public void LimpiarClave()
        {
            clave.Clear();
        }

        public void EstablecerOcupado(bool ocupado)
        {
            ingresar.Enabled = !ocupado;
            recuperar.Enabled = !ocupado;
            usuario.Enabled = !ocupado;
            clave.Enabled = !ocupado;
            UseWaitCursor = ocupado;
        }

        private void Disparar(EventHandler evento)
        {
            if (evento != null) evento(this, EventArgs.Empty);
        }
    }
}
