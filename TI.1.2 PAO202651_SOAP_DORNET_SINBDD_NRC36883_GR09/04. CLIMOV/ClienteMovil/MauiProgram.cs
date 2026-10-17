using Microsoft.Maui.Handlers;
using Microsoft.Maui.Hosting;

namespace ec.edu.monster
{
    public static class MauiProgram
    {
        public static MauiApp CreateMauiApp()
        {
            MauiAppBuilder builder = MauiApp.CreateBuilder();
            builder.UseMauiApp<App>();
            QuitarSubrayado();
            return builder.Build();
        }

        /// <summary>Los campos ya van dentro de un borde redondeado; en Android se quita la línea inferior nativa.</summary>
        private static void QuitarSubrayado()
        {
#if ANDROID
            var transparente = Android.Content.Res.ColorStateList.ValueOf(Android.Graphics.Color.Transparent);
            EntryHandler.Mapper.AppendToMapping("SinSubrayado",
                (manejador, vista) => manejador.PlatformView.BackgroundTintList = transparente);
            PickerHandler.Mapper.AppendToMapping("SinSubrayado",
                (manejador, vista) => manejador.PlatformView.BackgroundTintList = transparente);
#endif
        }
    }
}
