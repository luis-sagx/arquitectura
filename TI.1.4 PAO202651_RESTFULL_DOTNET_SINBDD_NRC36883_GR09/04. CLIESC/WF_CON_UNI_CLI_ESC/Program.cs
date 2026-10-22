using System;
using System.Windows.Forms;
using ec.edu.monster.controlador;
using ec.edu.monster.servicio;
using ec.edu.monster.vista;

namespace ec.edu.monster
{
    internal static class Program
    {
        [STAThread]
        private static void Main()
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            using (var cliente = ClienteRest.DesdeConfiguracion())
            {
                var login = new LoginForm();
                GestorVentanas.Instancia.RegistrarAcceso(login);
                new ControladorLogin(cliente, login);
                Application.Run(login);
            }
        }
    }
}
