using System;
using ec.edu.monster.cliente.ws;
using ec.edu.monster.modelo;

namespace ec.edu.monster.pruebas;

internal static class PruebaConversion
{
    internal static void Ejecutar()
    {
        using (var servidor = new CapturaWcf("convertir", "<estado>EXITO</estado><codigo>OK</codigo><mensaje>ok</mensaje><resultado>200</resultado>"))
        {
            var cliente = new ClienteSoap(new Uri(servidor.Endpoint));
            RespuestaSoap respuesta = cliente.Convertir("token-test", "LONGITUD", "METRO", "CENTIMETRO", 2.0);
            SoportePruebasWcf.Exigir(respuesta.Resultado == 200, "Conversion debe leer el resultado");
            SoportePruebasWcf.Exigir(servidor.Solicitud.GetAwaiter().GetResult().Contains("CENTIMETRO"), "Debe enviar la unidad destino");
        }
    }
}

