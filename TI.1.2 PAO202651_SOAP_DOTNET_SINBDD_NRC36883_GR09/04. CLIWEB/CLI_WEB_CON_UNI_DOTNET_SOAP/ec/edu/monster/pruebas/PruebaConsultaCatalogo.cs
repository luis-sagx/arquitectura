using System.Threading.Tasks;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;

namespace ec.edu.monster.pruebas;

internal static class PruebaConsultaCatalogo
{
    internal static async Task Ejecutar()
    {
        using var servidor = new CapturaWcf("obtenerCategorias", "<estado>EXITO</estado><codigo>OK</codigo><mensaje>ok</mensaje><categorias>LONGITUD</categorias><categorias>MASA</categorias>");
        using var cliente = new ClienteSoap(servidor.Endpoint);
        RespuestaSoap respuesta = await cliente.ObtenerCategoriasAsync("token-test");
        var captura = await servidor.CapturaAsync();
        SoportePruebasWcf.Exigir(respuesta.Categorias.Contains("LONGITUD") && respuesta.Categorias.Contains("MASA"), "Debe leer categorias");
        SoportePruebasWcf.Exigir(captura.solicitud.Contains("token-test"), "Debe enviar token para consultar el catalogo");
    }
}

