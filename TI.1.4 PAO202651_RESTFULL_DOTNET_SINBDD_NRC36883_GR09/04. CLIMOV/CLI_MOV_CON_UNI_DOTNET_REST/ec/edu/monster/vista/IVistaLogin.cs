using System;
using ec.edu.monster.modelo;

namespace ec.edu.monster.vista
{
    /// <summary>Lo que el controlador de acceso necesita de la pantalla, sin depender de MAUI.</summary>
    public interface IVistaLogin
    {
        string UsuarioIngresado { get; }
        string ClaveIngresada { get; }

        void LimpiarClave();
        void EstablecerOcupado(bool ocupado);
        void MostrarMensaje(string mensaje, bool error);

        /// <summary>Muestra el panel de nueva clave; al confirmar llama a ControladorLogin.RecuperarClaveAsync.</summary>
        void PedirNuevaClave(string usuario);

        void AbrirConversiones(Uri endpoint, Sesion sesion);
    }
}
