using System;
using ec.edu.monster.controlador;
using ec.edu.monster.servicio;
using ec.edu.monster.vista;
using System.Windows.Forms;

namespace ec.edu.monster
{
    internal static class Program
    {
        [STAThread]
        private static void Main()
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            var login = new LoginForm();
            var cliente = ClienteSoap.DesdeConfiguracion();
            GestorVentanas.Instancia.RegistrarAcceso(login);
            new ControladorLogin(cliente, login);
            Application.Run(login);
        }
    }
}
