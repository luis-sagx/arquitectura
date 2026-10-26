namespace ec.edu.monster
{
    /// <summary>
    /// Dirección del servicio WCF (WSConUni.svc) publicado por IIS Express.
    /// Emulador de Android: localhost, después de ejecutar "adb reverse tcp:14028 tcp:14028"
    /// (IIS Express rechaza el host 10.0.2.2 con "Invalid Hostname").
    /// Simulador de iOS en una Mac: localhost.
    /// Celular u otra red: la URL pública de ngrok seguida de /WSConUni.svc.
    /// </summary>
    public static class Configuracion
    {
        public const string EndpointSoap = "http://localhost:14028/WSConUni.svc";
    }
}
