using System.Threading.Tasks;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;

namespace ec.edu.monster.pruebas;

internal static class PruebaConversion
{
    internal static async Task Ejecutar()
    {
        using var servidor = new CapturaWcf("convertir", "<estado>EXITO</estado><codigo>OK</codigo><mensaje>ok</mensaje><resultado>200</resultado>");
        using var cliente = new ClienteSoap(servidor.Endpoint);
        RespuestaSoap respuesta = await cliente.ConvertirAsync("token-test", "LONGITUD", "METRO", "CENTIMETRO", 2.0);
        var captura = await servidor.CapturaAsync();
        SoportePruebasWcf.Exigir(respuesta.Resultado == 200 && captura.solicitud.Contains("CENTIMETRO"), "Conversion debe enviar unidades y leer resultado");
        SoportePruebasWcf.Exigir(captura.accion.Contains("convertir"), "SOAPAction debe indicar conversion");
    }
}

