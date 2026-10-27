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
        private static async Task VerificarRecuperacionClaveAsync()
        {
            var handler = new Handler();
            using (var cliente = new ClienteRest(new Uri("http://localhost:5111/api/ws-con-uni/"), handler))
            {
handler.Configurar(HttpStatusCode.OK, "{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"Clave actualizada\"}");
                RespuestaRest recuperacion = await cliente.RecuperarClaveAsync("Monster", "nueva-clave");
                Check(recuperacion.IsExito && handler.Ultima.Uri.AbsolutePath.EndsWith("/recuperar-clave"), "Recuperación debe invocar la ruta REST correspondiente");
                Check(handler.Ultima.Cuerpo.Contains("\"nuevaClave\":\"nueva-clave\"") && handler.Ultima.Authorization == null, "Recuperación debe enviar JSON sin token");
            }
        }

    }
}