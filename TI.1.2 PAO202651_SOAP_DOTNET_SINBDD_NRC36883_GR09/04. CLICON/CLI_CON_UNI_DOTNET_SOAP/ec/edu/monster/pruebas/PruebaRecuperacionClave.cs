using System;
using ec.edu.monster.cliente.ws;
using ec.edu.monster.modelo;

namespace ec.edu.monster.pruebas;

internal static class PruebaRecuperacionClave
{
    internal static void Ejecutar()
    {
        using (var servidor = new CapturaWcf("recuperarClave", "<estado>EXITO</estado><codigo>OK</codigo><mensaje>ok</mensaje>"))
        {
            var cliente = new ClienteSoap(new Uri(servidor.Endpoint));
            RespuestaSoap respuesta = cliente.RecuperarClave("Monster", "nueva-clave");
            SoportePruebasWcf.Exigir(respuesta.Codigo == "OK", "Recuperacion debe interpretar respuesta exitosa");
            SoportePruebasWcf.Exigir(servidor.Solicitud.GetAwaiter().GetResult().Contains("nueva-clave"), "Debe enviar la nueva clave");
        }
    }
}

