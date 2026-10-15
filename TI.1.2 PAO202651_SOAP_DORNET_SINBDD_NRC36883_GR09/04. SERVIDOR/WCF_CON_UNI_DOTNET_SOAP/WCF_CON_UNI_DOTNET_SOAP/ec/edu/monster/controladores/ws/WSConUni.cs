using System.ServiceModel;
using System.ServiceModel.Activation;
using ec.edu.monster.modelos;
using ec.edu.monster.servicios;

namespace ec.edu.monster.controladores.ws
{
    [ServiceContract(Name = "WSConUni", Namespace = "http://ws.controladores.monster.edu.ec/")]
    [XmlSerializerFormat(Style = OperationFormatStyle.Document, Use = OperationFormatUse.Literal)]
    public interface IWSConUni
    {
        [OperationContract(Name = "autenticar")]
        Respuesta Autenticar([MessageParameter(Name = "usuario")] string usuario,
            [MessageParameter(Name = "clave")] string clave);

        [OperationContract(Name = "recuperarClave")]
        Respuesta RecuperarClave([MessageParameter(Name = "usuario")] string usuario,
            [MessageParameter(Name = "nuevaClave")] string nuevaClave);

        [OperationContract(Name = "obtenerCategorias")]
        Respuesta ObtenerCategorias([MessageParameter(Name = "token")] string token);

        [OperationContract(Name = "obtenerUnidades")]
        Respuesta ObtenerUnidades([MessageParameter(Name = "token")] string token,
            [MessageParameter(Name = "categoria")] string categoria);

        [OperationContract(Name = "convertir")]
        Respuesta Convertir([MessageParameter(Name = "token")] string token,
            [MessageParameter(Name = "categoria")] string categoria,
            [MessageParameter(Name = "unidadOrigen")] string unidadOrigen,
            [MessageParameter(Name = "unidadDestino")] string unidadDestino,
            [MessageParameter(Name = "valor")] double? valor);
    }

    [ServiceBehavior(InstanceContextMode = InstanceContextMode.Single, ConcurrencyMode = ConcurrencyMode.Multiple)]
    [AspNetCompatibilityRequirements(RequirementsMode = AspNetCompatibilityRequirementsMode.Allowed)]
    public class WSConUni : IWSConUni
    {
        private static readonly ConversorUnidadesServices Servicio = new ConversorUnidadesServices();

        public Respuesta Autenticar(string usuario, string clave)
        {
            return Servicio.Autenticar(usuario, clave);
        }

        public Respuesta RecuperarClave(string usuario, string nuevaClave)
        {
            return Servicio.RecuperarClave(usuario, nuevaClave);
        }

        public Respuesta ObtenerCategorias(string token)
        {
            return Servicio.ObtenerCategorias(token);
        }

        public Respuesta ObtenerUnidades(string token, string categoria)
        {
            return Servicio.ObtenerUnidades(token, categoria);
        }

        public Respuesta Convertir(string token, string categoria, string unidadOrigen, string unidadDestino, double? valor)
        {
            return Servicio.Convertir(token, categoria, unidadOrigen, unidadDestino, valor);
        }
    }
}
