using System;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

namespace ec.edu.monster.pruebas;

internal sealed class CapturaSoap : IDisposable
{
    private readonly HttpListener servidor = new();
    private readonly Task<(string solicitud, string accion)> captura;
    internal string Endpoint { get; }

    internal CapturaSoap(string operacion, string contenido)
    {
        var asignador = new TcpListener(IPAddress.Loopback, 0);
        asignador.Start();
        int puerto = ((IPEndPoint)asignador.LocalEndpoint).Port;
        asignador.Stop();
        Endpoint = $"http://127.0.0.1:{puerto}/WSConUni.svc";
        servidor.Prefixes.Add($"http://127.0.0.1:{puerto}/");
        servidor.Start();
        captura = Task.Run(async () =>
        {
            var contexto = await servidor.GetContextAsync();
            using var lector = new StreamReader(contexto.Request.InputStream, contexto.Request.ContentEncoding);
            string solicitud = await lector.ReadToEndAsync();
            string cuerpo = $"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body><m:{operacion}Response xmlns:m=\"http://ws.controladores.monster.edu.ec/\"><m:{operacion}Result xmlns=\"http://ws.controladores.monster.edu.ec/\">{contenido}</m:{operacion}Result></m:{operacion}Response></s:Body></s:Envelope>";
            byte[] bytes = Encoding.UTF8.GetBytes(cuerpo);
            contexto.Response.StatusCode = 200;
            contexto.Response.ContentType = "text/xml; charset=utf-8";
            contexto.Response.ContentLength64 = bytes.Length;
            await contexto.Response.OutputStream.WriteAsync(bytes);
            contexto.Response.Close();
            return (solicitud, contexto.Request.Headers["SOAPAction"] ?? "");
        });
    }

    internal async Task<string> SolicitudAsync() => (await captura).solicitud;
    internal async Task<string> AccionAsync() => (await captura).accion;
    public void Dispose() { servidor.Stop(); servidor.Close(); }
}
