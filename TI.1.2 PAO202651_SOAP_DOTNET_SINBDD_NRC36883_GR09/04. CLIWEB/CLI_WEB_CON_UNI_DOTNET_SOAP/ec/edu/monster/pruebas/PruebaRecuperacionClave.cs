using System.Threading.Tasks;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;

namespace ec.edu.monster.pruebas;

internal static class PruebaRecuperacionClave
{
    internal static async Task Ejecutar()
    {
        using var servidor = new CapturaWcf("recuperarClave", "<estado>EXITO</estado><codigo>OK</codigo><mensaje>ok</mensaje>");
        using var cliente = new ClienteSoap(servidor.Endpoint);
        RespuestaSoap respuesta = await cliente.RecuperarClaveAsync("Monster", "nueva-clave");
        var captura = await servidor.CapturaAsync();
        SoportePruebasWcf.Exigir(respuesta.IsExito && captura.solicitud.Contains("nueva-clave"), "Recuperacion debe enviar clave y procesar respuesta");
    }
}

