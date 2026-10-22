using System.Collections.Generic;
using System.Runtime.Serialization;

namespace ec.edu.monster.modelo
{
    /// <summary>Cuerpo JSON de POST /api/ws-con-uni/autenticar.</summary>
    [DataContract]
    internal sealed class LoginRequest
    {
        [DataMember(Name = "usuario")] public string Usuario { get; set; }
        [DataMember(Name = "clave")] public string Clave { get; set; }
    }

    /// <summary>Cuerpo JSON de POST /api/ws-con-uni/recuperar-clave.</summary>
    [DataContract]
    internal sealed class RecoverRequest
    {
        [DataMember(Name = "usuario")] public string Usuario { get; set; }
        [DataMember(Name = "nuevaClave")] public string NuevaClave { get; set; }
    }

    /// <summary>Cuerpo JSON de POST /api/ws-con-uni/convertir.</summary>
    [DataContract]
    internal sealed class ConversionRequest
    {
        [DataMember(Name = "categoria")] public string Categoria { get; set; }
        [DataMember(Name = "unidadOrigen")] public string UnidadOrigen { get; set; }
        [DataMember(Name = "unidadDestino")] public string UnidadDestino { get; set; }
        [DataMember(Name = "valor")] public double Valor { get; set; }
    }

    /// <summary>Respuesta JSON del servidor tal como llega; ClienteRest la valida y la convierte en RespuestaRest.</summary>
    [DataContract]
    internal sealed class RespuestaJson
    {
        [DataMember(Name = "estado")] public string Estado { get; set; }
        [DataMember(Name = "codigo")] public string Codigo { get; set; }
        [DataMember(Name = "mensaje")] public string Mensaje { get; set; }
        [DataMember(Name = "token")] public string Token { get; set; }
        [DataMember(Name = "expiraEn")] public string ExpiraEn { get; set; }
        [DataMember(Name = "resultado")] public double? Resultado { get; set; }
        [DataMember(Name = "categoria")] public string Categoria { get; set; }
        [DataMember(Name = "unidadOrigen")] public string UnidadOrigen { get; set; }
        [DataMember(Name = "unidadDestino")] public string UnidadDestino { get; set; }
        [DataMember(Name = "categorias")] public List<string> Categorias { get; set; }
        [DataMember(Name = "unidades")] public List<string> Unidades { get; set; }
    }
}
