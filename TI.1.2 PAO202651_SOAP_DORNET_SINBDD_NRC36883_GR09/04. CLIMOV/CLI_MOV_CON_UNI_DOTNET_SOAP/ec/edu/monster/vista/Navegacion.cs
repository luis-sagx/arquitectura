using Microsoft.Maui.Controls;

namespace ec.edu.monster.vista
{
    /// <summary>
    /// Cambia la pantalla raíz de la ventana: acceso y conversiones no se apilan,
    /// así el botón Atrás no regresa a una sesión ya cerrada.
    /// </summary>
    internal static class Navegacion
    {
        public static void Mostrar(Page pagina)
        {
            Window? ventana = Application.Current?.Windows.Count > 0 ? Application.Current.Windows[0] : null;
            if (ventana != null) ventana.Page = pagina;
        }
    }
}
