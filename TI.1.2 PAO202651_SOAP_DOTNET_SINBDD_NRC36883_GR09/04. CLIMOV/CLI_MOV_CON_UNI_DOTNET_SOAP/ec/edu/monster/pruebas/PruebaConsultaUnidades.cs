using System.Threading.Tasks;
using ec.edu.monster.servicio;

namespace ec.edu.monster.pruebas;

internal static class PruebaConsultaUnidades
{
    internal static async Task EjecutarAsync()
    {
        using var servidor = new CapturaSoap("obtenerUnidades", "<estado>EXITO</estado><codigo>OK</codigo><mensaje>ok</mensaje><unidades>METRO</unidades><unidades>CENTIMETRO</unidades>");
        var respuesta = await new ClienteSoap(servidor.Endpoint).ObtenerUnidadesAsync("token-test", "LONGITUD");
        SoportePruebasSoap.Exigir(respuesta.Unidades.Contains("METRO") && respuesta.Unidades.Contains("CENTIMETRO"), "Debe leer las unidades SOAP");
        SoportePruebasSoap.Exigir((await servidor.SolicitudAsync()).Contains("LONGITUD"), "La consulta debe enviar la categoria");
    }
}
