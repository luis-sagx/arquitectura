namespace ec.edu.monster
{
    /// <summary>
    /// URL base de la API REST (ASP.NET Core) que termina en /api/ws-con-uni.
    /// Emulador de Android: 10.0.2.2 apunta al localhost del computador.
    /// Simulador de iOS en una Mac: localhost.
    /// Celular físico u otra red: la IP del computador (ipconfig) o la URL pública de ngrok seguida de /api/ws-con-uni.
    /// </summary>
    public static class Configuracion
    {
        public const string EndpointRest = "http://10.0.2.2:5111/api/ws-con-uni";
    }
}
