using ec.edu.monster.modelos;
using ec.edu.monster.modelos.requests;
using ec.edu.monster.servicios;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.ModelBinding;

namespace ec.edu.monster.controladores.rest;

[ApiController]
[Route("api/ws-con-uni")]
public sealed class WSConUniController(ConversorUnidadesServices servicio) : ControllerBase
{
    [HttpPost("autenticar")]
    public ActionResult<Respuesta> Autenticar([FromBody(EmptyBodyBehavior = EmptyBodyBehavior.Allow)] AutenticarRequest? request) =>
        Responder(servicio.Autenticar(request?.Usuario, request?.Clave));

    [HttpPost("recuperar-clave")]
    public ActionResult<Respuesta> RecuperarClave([FromBody(EmptyBodyBehavior = EmptyBodyBehavior.Allow)] RecuperarClaveRequest? request) =>
        Responder(servicio.RecuperarClave(request?.Usuario, request?.NuevaClave));

    [HttpGet("categorias")]
    public ActionResult<Respuesta> ObtenerCategorias() => Responder(servicio.ObtenerCategorias(LeerToken()));

    [HttpGet("unidades")]
    public ActionResult<Respuesta> ObtenerUnidades([FromQuery] string? categoria) => Responder(servicio.ObtenerUnidades(LeerToken(), categoria));

    [HttpPost("convertir")]
    public ActionResult<Respuesta> Convertir([FromBody(EmptyBodyBehavior = EmptyBodyBehavior.Allow)] ConvertirRequest? request) =>
        Responder(servicio.Convertir(LeerToken(), request?.Categoria, request?.UnidadOrigen, request?.UnidadDestino, request?.Valor));

    private string? LeerToken()
    {
        string? authorization = Request.Headers.Authorization.FirstOrDefault();
        if (authorization is null) return null;
        string[] partes = authorization.Split(' ', 2, StringSplitOptions.RemoveEmptyEntries);
        return partes.Length == 2 && partes[0].Equals("Bearer", StringComparison.OrdinalIgnoreCase) ? partes[1].Trim() : null;
    }

    private ActionResult<Respuesta> Responder(Respuesta respuesta)
    {
        if (respuesta.Codigo?.StartsWith("AUTH-", StringComparison.Ordinal) == true)
            Response.Headers.WWWAuthenticate = "Bearer";

        return respuesta.Codigo switch
        {
            "OK" => Ok(respuesta),
            "AUTH-001" or "AUTH-002" or "AUTH-003" => Unauthorized(respuesta),
            "DATA-001" or "UNIT-001" => BadRequest(respuesta),
            _ => StatusCode(StatusCodes.Status500InternalServerError, respuesta)
        };
    }
}
