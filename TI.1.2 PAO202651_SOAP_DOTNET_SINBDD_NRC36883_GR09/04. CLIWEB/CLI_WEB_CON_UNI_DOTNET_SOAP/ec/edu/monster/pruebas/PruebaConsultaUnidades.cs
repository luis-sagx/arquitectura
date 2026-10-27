using System.Threading.Tasks;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;

namespace ec.edu.monster.pruebas;

internal static class PruebaConsultaUnidades
{
    internal static async Task Ejecutar()
    {
        using var servidor = new CapturaWcf("obtenerUnidades", "<estado>EXITO</estado><codigo>OK</codigo><mensaje>ok</mensaje><unidades>METRO</unidades><unidades>CENTIMETRO</unidades>");
        using var cliente = new ClienteSoap(servidor.Endpoint);
        RespuestaSoap respuesta = await cliente.ObtenerUnidadesAsync("token-test", "LONGITUD");
        var captura = await servidor.CapturaAsync();
        SoportePruebasWcf.Exigir(respuesta.Unidades.Contains("METRO") && respuesta.Unidades.Contains("CENTIMETRO"), "Debe leer unidades");
        SoportePruebasWcf.Exigir(captura.solicitud.Contains("LONGITUD"), "Debe enviar la categoria consultada");
    }
}

