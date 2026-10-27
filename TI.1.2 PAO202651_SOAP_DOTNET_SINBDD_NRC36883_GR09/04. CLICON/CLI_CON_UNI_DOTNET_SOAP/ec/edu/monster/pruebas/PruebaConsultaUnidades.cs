using System;
using ec.edu.monster.cliente.ws;
using ec.edu.monster.modelo;

namespace ec.edu.monster.pruebas;

internal static class PruebaConsultaUnidades
{
    internal static void Ejecutar()
    {
        using (var servidor = new CapturaWcf("obtenerUnidades", "<estado>EXITO</estado><codigo>OK</codigo><mensaje>ok</mensaje><unidades>METRO</unidades><unidades>CENTIMETRO</unidades>"))
        {
            var cliente = new ClienteSoap(new Uri(servidor.Endpoint));
            RespuestaSoap respuesta = cliente.ObtenerUnidades("token-test", "LONGITUD");
            SoportePruebasWcf.Exigir(respuesta.Unidades.Contains("METRO") && respuesta.Unidades.Contains("CENTIMETRO"), "Debe deserializar unidades");
            SoportePruebasWcf.Exigir(servidor.Solicitud.GetAwaiter().GetResult().Contains("LONGITUD"), "Debe enviar categoria solicitada");
        }
    }
}

