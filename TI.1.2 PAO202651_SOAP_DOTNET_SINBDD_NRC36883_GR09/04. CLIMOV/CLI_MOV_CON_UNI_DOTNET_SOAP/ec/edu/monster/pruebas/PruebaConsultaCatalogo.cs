using System.Threading.Tasks;
using ec.edu.monster.servicio;

namespace ec.edu.monster.pruebas;

internal static class PruebaConsultaCatalogo
{
    internal static async Task EjecutarAsync()
    {
        using var servidor = new CapturaSoap("obtenerCategorias", "<estado>EXITO</estado><codigo>OK</codigo><mensaje>ok</mensaje><categorias>LONGITUD</categorias><categorias>MASA</categorias>");
        var respuesta = await new ClienteSoap(servidor.Endpoint).ObtenerCategoriasAsync("token-test");
        SoportePruebasSoap.Exigir(respuesta.Categorias.Contains("LONGITUD") && respuesta.Categorias.Contains("MASA"), "Debe leer categorias SOAP");
        SoportePruebasSoap.Exigir((await servidor.SolicitudAsync()).Contains("token-test"), "La consulta debe enviar el token");
    }
}
