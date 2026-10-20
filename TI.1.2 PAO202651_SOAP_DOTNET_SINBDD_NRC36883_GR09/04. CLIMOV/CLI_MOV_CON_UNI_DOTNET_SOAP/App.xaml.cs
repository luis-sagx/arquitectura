using ec.edu.monster.vista;
using Microsoft.Maui;
using Microsoft.Maui.ApplicationModel;
using Microsoft.Maui.Controls;

namespace ec.edu.monster
{
    public partial class App : Application
    {
        public App()
        {
            InitializeComponent();
            // La paleta es clara; se fija el tema para que el modo oscuro del celular no la mezcle.
            UserAppTheme = AppTheme.Light;
        }

        protected override Window CreateWindow(IActivationState? activationState)
        {
            return new Window(new LoginPage());
        }
    }
}
