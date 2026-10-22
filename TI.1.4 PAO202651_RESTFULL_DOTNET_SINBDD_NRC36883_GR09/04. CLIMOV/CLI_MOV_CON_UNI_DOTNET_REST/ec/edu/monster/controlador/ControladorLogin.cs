using System;
using System.IO;
using System.Threading.Tasks;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;
using ec.edu.monster.vista;

namespace ec.edu.monster.controlador
{
    /// <summary>Controla el acceso y la recuperación sin mezclar reglas con la vista.</summary>
    public sealed class ControladorLogin
    {
        private readonly IVistaLogin vista;
        private readonly string endpoint;

        public ControladorLogin(IVistaLogin vista, string endpoint)
        {
            this.vista = vista;
            this.endpoint = endpoint;
        }

        public async Task IngresarAsync()
        {
            string usuario = vista.UsuarioIngresado;
            string clave = vista.ClaveIngresada;
            if (String.IsNullOrWhiteSpace(usuario) || clave.Length == 0)
            {
                vista.MostrarMensaje("Escribe el usuario y la contraseña.", true);
                return;
            }
            ClienteRest? cliente = CrearCliente();
            if (cliente == null) return;

            vista.EstablecerOcupado(true);
            vista.MostrarMensaje("Conectando con el servicio…", false);
            try
            {
                RespuestaRest respuesta = await cliente.AutenticarAsync(usuario, clave);
                vista.EstablecerOcupado(false);
                vista.LimpiarClave();
                if (!respuesta.IsExito || String.IsNullOrWhiteSpace(respuesta.Token))
                {
                    vista.MostrarMensaje(MensajeRespuesta(respuesta), true);
                    return;
                }
                vista.MostrarMensaje(" ", false);
                vista.AbrirConversiones(cliente.Endpoint, new Sesion(respuesta.Token, respuesta.ExpiraEn));
            }
            catch (IOException ex)
            {
                vista.EstablecerOcupado(false);
                vista.LimpiarClave();
                vista.MostrarMensaje(ex.Message, true);
            }
        }

        public void SolicitarRecuperacion()
        {
            string usuario = vista.UsuarioIngresado;
            if (String.IsNullOrWhiteSpace(usuario))
            {
                vista.MostrarMensaje("Escribe tu usuario antes de recuperar la contraseña.", true);
                return;
            }
            vista.PedirNuevaClave(usuario);
        }

        public async Task RecuperarClaveAsync(string nuevaClave, string confirmacion)
        {
            string usuario = vista.UsuarioIngresado;
            if (String.IsNullOrWhiteSpace(usuario))
            {
                vista.MostrarMensaje("Escribe tu usuario antes de recuperar la contraseña.", true);
                return;
            }
            if (String.IsNullOrEmpty(nuevaClave))
            {
                vista.MostrarMensaje("La nueva contraseña no puede estar vacía.", true);
                return;
            }
            if (nuevaClave != confirmacion)
            {
                vista.MostrarMensaje("Las contraseñas no coinciden.", true);
                return;
            }
            ClienteRest? cliente = CrearCliente();
            if (cliente == null) return;

            vista.EstablecerOcupado(true);
            vista.MostrarMensaje("Actualizando contraseña…", false);
            try
            {
                RespuestaRest respuesta = await cliente.RecuperarClaveAsync(usuario, nuevaClave);
                vista.EstablecerOcupado(false);
                vista.MostrarMensaje(respuesta.IsExito ? respuesta.Mensaje : MensajeRespuesta(respuesta),
                    !respuesta.IsExito);
            }
            catch (IOException ex)
            {
                vista.EstablecerOcupado(false);
                vista.MostrarMensaje(ex.Message, true);
            }
        }

        private ClienteRest? CrearCliente()
        {
            try
            {
                return new ClienteRest(endpoint);
            }
            catch (ArgumentException ex)
            {
                vista.MostrarMensaje(ex.Message, true);
                return null;
            }
        }

        private static string MensajeRespuesta(RespuestaRest respuesta)
        {
            return respuesta.Codigo + " · " + respuesta.Mensaje;
        }
    }
}
