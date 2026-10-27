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
        private static async Task VerificarAutenticacionAsync()
        {
            var handler = new Handler();
            using (var cliente = new ClienteRest(new Uri("http://localhost:5111/api/ws-con-uni/"), handler))
            {
handler.Configurar(HttpStatusCode.OK, "{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"Sesion iniciada\",\"token\":\"token-prueba\",\"expiraEn\":\"2026-10-21T12:00:00Z\"}");
                RespuestaRest login = await cliente.AutenticarAsync("Monster", "Monster9");
                Check(login.IsExito && login.Token == "token-prueba", "Autenticación debe leer token y estado de éxito");
                Check(handler.Ultima.Metodo == HttpMethod.Post && handler.Ultima.Uri.AbsolutePath == "/api/ws-con-uni/autenticar", "Autenticación debe enviar POST a la ruta correcta");
                Check(handler.Ultima.Cuerpo.Contains("\"usuario\":\"Monster\"") && handler.Ultima.Cuerpo.Contains("\"clave\":\"Monster9\""), "Autenticación debe enviar los nombres JSON requeridos");
                Check(handler.Ultima.Authorization == null, "Autenticación no debe enviar Bearer");
            }
        }

    }
}