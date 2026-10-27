using System;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;
using ec.edu.monster.modelo;

namespace ec.edu.monster.pruebas;

internal sealed class CapturaHttp
{
    internal string Metodo { get; init; } = "";
    internal string Ruta { get; init; } = "";
    internal string Autorizacion { get; init; } = "";
    internal string Cuerpo { get; init; } = "";
    internal RespuestaRest Respuesta { get; init; } = null!;
}

internal static class SoportePruebasRest
{
    internal static void Exigir(bool condicion, string mensaje)
    {
        if (!condicion) throw new InvalidOperationException(mensaje);
    }

    internal static async Task<CapturaHttp> EjecutarAsync(string json,
        Func<Uri, Task<RespuestaRest>> llamada)
    {
        var asignador = new TcpListener(IPAddress.Loopback, 0);
        asignador.Start();
        int puerto = ((IPEndPoint)asignador.LocalEndpoint).Port;
        asignador.Stop();

        using var servidor = new HttpListener();
        string prefijo = $"http://127.0.0.1:{puerto}/";
        servidor.Prefixes.Add(prefijo);
        servidor.Start();
        var respuestaServidor = Task.Run(async () =>
        {
            var contexto = await servidor.GetContextAsync();
            string cuerpo;
            using (var lector = new StreamReader(contexto.Request.InputStream, contexto.Request.ContentEncoding))
                cuerpo = await lector.ReadToEndAsync();
            contexto.Response.StatusCode = 200;
            contexto.Response.ContentType = "application/json; charset=utf-8";
            byte[] bytes = Encoding.UTF8.GetBytes(json);
            contexto.Response.ContentLength64 = bytes.Length;
            await contexto.Response.OutputStream.WriteAsync(bytes);
            contexto.Response.Close();
            return new CapturaHttp
            {
                Metodo = contexto.Request.HttpMethod,
                Ruta = contexto.Request.RawUrl ?? "",
                Autorizacion = contexto.Request.Headers["Authorization"] ?? "",
                Cuerpo = cuerpo
            };
        });

        try
        {
            var respuesta = await llamada(new Uri(prefijo + "api/ws-con-uni/"));
            var captura = await respuestaServidor;
            return new CapturaHttp
            {
                Metodo = captura.Metodo,
                Ruta = captura.Ruta,
                Autorizacion = captura.Autorizacion,
                Cuerpo = captura.Cuerpo,
                Respuesta = respuesta
            };
        }
        finally
        {
            servidor.Stop();
        }
    }
}

