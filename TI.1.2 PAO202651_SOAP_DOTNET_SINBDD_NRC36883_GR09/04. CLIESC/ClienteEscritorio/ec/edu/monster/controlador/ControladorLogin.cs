using System;
using System.IO;
using System.Threading.Tasks;
using System.Windows.Forms;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;
using ec.edu.monster.vista;

namespace ec.edu.monster.controlador
{
    public sealed class ControladorLogin
    {
        private readonly IClienteSoap cliente;
        private readonly LoginForm vista;

        public ControladorLogin(IClienteSoap cliente, LoginForm vista)
        {
            this.cliente = cliente;
            this.vista = vista;
            vista.IngresarSolicitado += Ingresar;
            vista.RecuperarSolicitado += RecuperarClave;
        }

        private async void Ingresar(object sender, EventArgs evento)
        {
            string usuario = vista.UsuarioIngresado;
            char[] claveTemporal = vista.ClaveIngresada;
            if (String.IsNullOrWhiteSpace(usuario) || claveTemporal.Length == 0)
            {
                Array.Clear(claveTemporal, 0, claveTemporal.Length);
                vista.MostrarMensaje("Escribe el usuario y la contraseña.", true);
                return;
            }

            string clave = new string(claveTemporal);
            Array.Clear(claveTemporal, 0, claveTemporal.Length);
            vista.EstablecerOcupado(true);
            vista.MostrarMensaje("Conectando con el servicio…", false);
            vista.LimpiarClave();
            try
            {
                RespuestaSoap respuesta = await Task.Run(() => cliente.Autenticar(usuario, clave));
                if (!respuesta.IsExito || String.IsNullOrWhiteSpace(respuesta.Token))
                {
                    vista.MostrarMensaje(respuesta.Mensaje, true);
                    return;
                }
                AbrirConversiones(new Sesion(respuesta.Token, respuesta.ExpiraEn));
            }
            catch (IOException error)
            {
                vista.MostrarMensaje(error.Message, true);
            }
            catch (Exception)
            {
                vista.MostrarMensaje("No se pudo completar el inicio de sesión.", true);
            }
            finally
            {
                vista.EstablecerOcupado(false);
            }
        }

        private async void RecuperarClave(object sender, EventArgs evento)
        {
            string usuario = vista.UsuarioIngresado;
            if (String.IsNullOrWhiteSpace(usuario))
            {
                vista.MostrarMensaje("Escribe tu usuario antes de recuperar la contraseña.", true);
                return;
            }

            string nuevaClave = vista.PedirNuevaClave();
            if (nuevaClave == null) return;
            if (String.IsNullOrWhiteSpace(nuevaClave))
            {
                vista.MostrarMensaje("La nueva contraseña no puede estar vacía.", true);
                return;
            }

            vista.EstablecerOcupado(true);
            vista.MostrarMensaje("Actualizando contraseña…", false);
            try
            {
                RespuestaSoap respuesta = await Task.Run(() => cliente.RecuperarClave(usuario, nuevaClave));
                vista.MostrarMensaje(respuesta.Mensaje, !respuesta.IsExito);
            }
            catch (IOException error)
            {
                vista.MostrarMensaje(error.Message, true);
            }
            catch (Exception)
            {
                vista.MostrarMensaje("No se pudo completar la recuperación.", true);
            }
            finally
            {
                vista.EstablecerOcupado(false);
            }
        }

        private void AbrirConversiones(Sesion sesion)
        {
            GestorVentanas gestor = GestorVentanas.Instancia;
            ControladorConversiones controlador = null;
            gestor.MostrarConversiones(() =>
            {
                var ventana = new ConversionForm();
                controlador = new ControladorConversiones(cliente, sesion, ventana, mensaje =>
                    gestor.VolverAlAcceso(mensaje, !String.IsNullOrWhiteSpace(mensaje)));
                return ventana;
            });
            if (controlador != null) controlador.Iniciar();
        }
    }
}
