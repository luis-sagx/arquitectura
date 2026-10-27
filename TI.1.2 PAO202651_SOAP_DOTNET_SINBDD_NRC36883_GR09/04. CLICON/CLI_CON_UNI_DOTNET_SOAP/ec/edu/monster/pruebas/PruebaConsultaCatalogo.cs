using System;
using ec.edu.monster.cliente.ws;
using ec.edu.monster.modelo;

namespace ec.edu.monster.pruebas;

internal static class PruebaConsultaCatalogo
{
    internal static void Ejecutar()
    {
        using (var servidor = new CapturaWcf("obtenerCategorias", "<estado>EXITO</estado><codigo>OK</codigo><mensaje>ok</mensaje><categorias>LONGITUD</categorias><categorias>MASA</categorias>"))
        {
            var cliente = new ClienteSoap(new Uri(servidor.Endpoint));
            RespuestaSoap respuesta = cliente.ObtenerCategorias("token-test");
            SoportePruebasWcf.Exigir(respuesta.Categorias.Contains("LONGITUD") && respuesta.Categorias.Contains("MASA"), "Debe deserializar las categorias");
            SoportePruebasWcf.Exigir(servidor.Solicitud.GetAwaiter().GetResult().Contains("token-test"), "Debe enviar token para consultar categorias");
        }
    }
}

