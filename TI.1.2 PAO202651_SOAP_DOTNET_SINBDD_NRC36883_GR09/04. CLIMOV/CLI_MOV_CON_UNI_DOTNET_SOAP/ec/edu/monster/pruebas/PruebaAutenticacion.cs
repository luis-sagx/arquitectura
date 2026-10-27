using System.Threading.Tasks;
using ec.edu.monster.servicio;

namespace ec.edu.monster.pruebas;

internal static class PruebaAutenticacion
{
    internal static async Task EjecutarAsync()
    {
        using var servidor = new CapturaSoap("autenticar", "<estado>EXITO</estado><codigo>OK</codigo><mensaje>ok</mensaje><token>sesion</token>");
        var respuesta = await new ClienteSoap(servidor.Endpoint).AutenticarAsync("Monster", "Monster9");
        string solicitud = await servidor.SolicitudAsync();
        SoportePruebasSoap.Exigir(respuesta.IsExito && respuesta.Token == "sesion", "Autenticacion debe devolver el token recibido");
        SoportePruebasSoap.Exigir(solicitud.Contains("autenticar") && solicitud.Contains("Monster9"), "Debe enviar credenciales en SOAP");
        SoportePruebasSoap.Exigir((await servidor.AccionAsync()).Contains("autenticar"), "SOAPAction debe identificar autenticacion");
    }
}
