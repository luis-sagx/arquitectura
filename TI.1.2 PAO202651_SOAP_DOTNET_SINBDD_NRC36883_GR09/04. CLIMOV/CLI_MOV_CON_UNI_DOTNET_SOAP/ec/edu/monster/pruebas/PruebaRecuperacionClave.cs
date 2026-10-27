using System.Threading.Tasks;
using ec.edu.monster.servicio;

namespace ec.edu.monster.pruebas;

internal static class PruebaRecuperacionClave
{
    internal static async Task EjecutarAsync()
    {
        using var servidor = new CapturaSoap("recuperarClave", "<estado>EXITO</estado><codigo>OK</codigo><mensaje>ok</mensaje>");
        var respuesta = await new ClienteSoap(servidor.Endpoint).RecuperarClaveAsync("Monster", "nueva-clave");
        SoportePruebasSoap.Exigir(respuesta.IsExito, "Recuperacion debe interpretar respuesta exitosa");
        string solicitud = await servidor.SolicitudAsync();
        SoportePruebasSoap.Exigir(solicitud.Contains("nuevaClave") && solicitud.Contains("nueva-clave"), "Debe enviar la nueva clave");
    }
}
