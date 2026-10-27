using System;
using ec.edu.monster.cliente.ws;
using ec.edu.monster.modelo;

namespace ec.edu.monster.pruebas;

internal static class PruebaAutenticacion
{
    internal static void Ejecutar()
    {
        using (var servidor = new CapturaWcf("autenticar", "<estado>EXITO</estado><codigo>OK</codigo><mensaje>ok</mensaje><token>sesion</token>"))
        {
            var cliente = new ClienteSoap(new Uri(servidor.Endpoint));
            RespuestaSoap respuesta = cliente.Autenticar("Monster", "Monster9");
            SoportePruebasWcf.Exigir(respuesta.Codigo == "OK" && respuesta.Token == "sesion", "Autenticacion debe devolver token");
            SoportePruebasWcf.Exigir(servidor.Solicitud.GetAwaiter().GetResult().Contains("Monster9"), "Debe enviar la clave en la solicitud SOAP");
            SoportePruebasWcf.Exigir(servidor.Accion.GetAwaiter().GetResult().Contains("autenticar"), "SOAPAction debe identificar autenticacion");
        }
    }
}

