using System.Collections.Generic;
using System.ServiceModel;
using System.Threading.Tasks;
using System.Xml.Serialization;

namespace ec.edu.monster.ws
{
    /// <summary>
    /// Contrato SOAP 1.1 publicado por WSConUni.svc (mismo contrato que el cliente de consola .NET).
    /// Las operaciones son asíncronas para no bloquear los hilos de ASP.NET Core mientras responde el servidor.
    /// </summary>
    [ServiceContract(Name = "WSConUni", Namespace = "http://ws.controladores.monster.edu.ec/")]
    [XmlSerializerFormat(Style = OperationFormatStyle.Document, Use = OperationFormatUse.Literal)]
    public interface IWSConUni
    {
        [OperationContract(Name = "autenticar")]
        [return: MessageParameter(Name = "autenticarResult")]
        Task<RespuestaWcf> AutenticarAsync([MessageParameter(Name = "usuario")] string usuario,
            [MessageParameter(Name = "clave")] string clave);

        [OperationContract(Name = "recuperarClave")]
        [return: MessageParameter(Name = "recuperarClaveResult")]
        Task<RespuestaWcf> RecuperarClaveAsync([MessageParameter(Name = "usuario")] string usuario,
            [MessageParameter(Name = "nuevaClave")] string nuevaClave);

        [OperationContract(Name = "obtenerCategorias")]
        [return: MessageParameter(Name = "obtenerCategoriasResult")]
        Task<RespuestaWcf> ObtenerCategoriasAsync([MessageParameter(Name = "token")] string token);

        [OperationContract(Name = "obtenerUnidades")]
        [return: MessageParameter(Name = "obtenerUnidadesResult")]
        Task<RespuestaWcf> ObtenerUnidadesAsync([MessageParameter(Name = "token")] string token,
            [MessageParameter(Name = "categoria")] string categoria);

        [OperationContract(Name = "convertir")]
        [return: MessageParameter(Name = "convertirResult")]
        Task<RespuestaWcf> ConvertirAsync([MessageParameter(Name = "token")] string token,
            [MessageParameter(Name = "categoria")] string categoria,
            [MessageParameter(Name = "unidadOrigen")] string unidadOrigen,
            [MessageParameter(Name = "unidadDestino")] string unidadDestino,
            [MessageParameter(Name = "valor")] double? valor);
    }

    [XmlType(TypeName = "Respuesta", Namespace = "http://ws.controladores.monster.edu.ec/")]
    [XmlRoot(ElementName = "Respuesta", Namespace = "http://ws.controladores.monster.edu.ec/")]
    public sealed class RespuestaWcf
    {
        [XmlElement(ElementName = "estado", Order = 1)]
        public string? Estado { get; set; }
        [XmlElement(ElementName = "codigo", Order = 2)]
        public string? Codigo { get; set; }
        [XmlElement(ElementName = "mensaje", Order = 3)]
        public string? Mensaje { get; set; }
        [XmlElement(ElementName = "token", Order = 4)]
        public string? Token { get; set; }
        [XmlElement(ElementName = "expiraEn", Order = 5)]
        public string? ExpiraEn { get; set; }
        [XmlElement(ElementName = "resultado", Order = 6, IsNullable = true)]
        public double? Resultado { get; set; }
        [XmlElement(ElementName = "categoria", Order = 7)]
        public string? Categoria { get; set; }
        [XmlElement(ElementName = "unidadOrigen", Order = 8)]
        public string? UnidadOrigen { get; set; }
        [XmlElement(ElementName = "unidadDestino", Order = 9)]
        public string? UnidadDestino { get; set; }
        [XmlElement(ElementName = "categorias", Order = 10)]
        public List<string>? Categorias { get; set; }
        [XmlElement(ElementName = "unidades", Order = 11)]
        public List<string>? Unidades { get; set; }
    }
}
