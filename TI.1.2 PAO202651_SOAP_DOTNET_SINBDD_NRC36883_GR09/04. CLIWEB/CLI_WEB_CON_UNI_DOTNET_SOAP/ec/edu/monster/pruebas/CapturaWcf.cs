using System;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

namespace ec.edu.monster.pruebas;

internal sealed class CapturaWcf : IDisposable
{
    private readonly HttpListener servidor = new();
    private readonly Task<(string solicitud, string accion)> captura;
    internal string Endpoint { get; }

    internal CapturaWcf(string operacion, string miembros)
    {
        var asignador = new TcpListener(IPAddress.Loopback, 0);
        asignador.Start();
        int puerto = ((IPEndPoint)asignador.LocalEndpoint).Port;
        asignador.Stop();
        Endpoint = "http://127.0.0.1:" + puerto + "/WSConUni.svc";
        servidor.Prefixes.Add("http://127.0.0.1:" + puerto + "/");
        servidor.Start();
        captura = Task.Run(async () =>
        {
            var contexto = await servidor.GetContextAsync();
            using var lector = new StreamReader(contexto.Request.InputStream, contexto.Request.ContentEncoding);
            string solicitud = await lector.ReadToEndAsync();
            string respuesta = string.Format(
                "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body><m:{0}Response xmlns:m=\"http://ws.controladores.monster.edu.ec/\"><m:{0}Result xmlns=\"http://ws.controladores.monster.edu.ec/\">{1}</m:{0}Result></m:{0}Response></s:Body></s:Envelope>",
                operacion, miembros);
            byte[] bytes = Encoding.UTF8.GetBytes(respuesta);
            contexto.Response.StatusCode = 200;
            contexto.Response.ContentType = "text/xml; charset=utf-8";
            contexto.Response.ContentLength64 = bytes.Length;
            await contexto.Response.OutputStream.WriteAsync(bytes);
            contexto.Response.Close();
            return (solicitud, contexto.Request.Headers["SOAPAction"] ?? "");
        });
    }

    internal Task<(string solicitud, string accion)> CapturaAsync() => captura;
    public void Dispose() { servidor.Stop(); servidor.Close(); }
}

internal static class SoportePruebasWcf
{
    internal static void Exigir(bool condicion, string mensaje)
    {
        if (!condicion) throw new InvalidOperationException(mensaje);
    }
}

