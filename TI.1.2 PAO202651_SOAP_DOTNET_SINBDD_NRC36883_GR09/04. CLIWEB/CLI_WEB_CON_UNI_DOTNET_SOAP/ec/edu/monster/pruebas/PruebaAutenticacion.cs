using System.Threading.Tasks;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;

namespace ec.edu.monster.pruebas;

internal static class PruebaAutenticacion
{
    internal static async Task Ejecutar()
    {
        using var servidor = new CapturaWcf("autenticar", "<estado>EXITO</estado><codigo>OK</codigo><mensaje>ok</mensaje><token>sesion</token>");
        using var cliente = new ClienteSoap(servidor.Endpoint);
        RespuestaSoap respuesta = await cliente.AutenticarAsync("Monster", "Monster9");
        var captura = await servidor.CapturaAsync();
        SoportePruebasWcf.Exigir(respuesta.IsExito && respuesta.Token == "sesion", "Autenticacion debe devolver token");
        SoportePruebasWcf.Exigir(captura.solicitud.Contains("Monster9") && captura.accion.Contains("autenticar"), "Debe enviar credenciales y SOAPAction");
    }
}

