using System.Threading.Tasks;
using ec.edu.monster.servicio;

namespace ec.edu.monster.pruebas;

internal static class PruebaConversion
{
    internal static async Task EjecutarAsync()
    {
        using var servidor = new CapturaSoap("convertir", "<estado>EXITO</estado><codigo>OK</codigo><mensaje>ok</mensaje><resultado>200</resultado>");
        var respuesta = await new ClienteSoap(servidor.Endpoint).ConvertirAsync("token-test", "LONGITUD", "METRO", "CENTIMETRO", 2);
        string solicitud = await servidor.SolicitudAsync();
        SoportePruebasSoap.Exigir(respuesta.Resultado == 200, "Conversion debe leer el resultado numerico");
        SoportePruebasSoap.Exigir(solicitud.Contains("token-test") && solicitud.Contains("CENTIMETRO") && solicitud.Contains("<ns:valor>2</ns:valor>"),
            "Conversion debe enviar token, unidades y valor");
    }
}
