using System;
using System.Net;
using System.Net.Http;
using System.Threading.Tasks;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;

namespace ec.edu.monster.pruebas
{
    internal static partial class PruebasClienteRest
    {
        private static async Task VerificarConversionesAsync()
        {
            var handler = new Handler();
            using (var cliente = new ClienteRest(new Uri("http://localhost:5111/api/ws-con-uni/"), handler))
            {
handler.Configurar(HttpStatusCode.OK, "{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"Conversion realizada\",\"resultado\":200,\"categoria\":\"LONGITUD\",\"unidadOrigen\":\"METRO\",\"unidadDestino\":\"CENTIMETRO\"}");
                RespuestaRest conversion = await cliente.ConvertirAsync("token-prueba", "LONGITUD", "METRO", "CENTIMETRO", 2);
                Check(conversion.Resultado == 200 && conversion.UnidadOrigen == "METRO", "Conversión debe deserializar resultado y unidades");
                Check(handler.Ultima.Metodo == HttpMethod.Post && handler.Ultima.Uri.AbsolutePath.EndsWith("/convertir"), "Conversión debe usar POST");
                Check(handler.Ultima.Cuerpo.Contains("\"categoria\":\"LONGITUD\"") && handler.Ultima.Cuerpo.Contains("\"valor\":2"), "Conversión debe enviar los campos JSON y el valor numérico");
                Check(handler.Ultima.Authorization == "Bearer token-prueba", "Conversión debe enviar Bearer");
            }
        }

    }
}