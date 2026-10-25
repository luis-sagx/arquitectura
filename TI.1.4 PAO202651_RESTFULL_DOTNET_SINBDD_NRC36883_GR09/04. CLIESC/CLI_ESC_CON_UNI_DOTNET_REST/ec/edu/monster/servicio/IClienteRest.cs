using System.Threading.Tasks;
using ec.edu.monster.modelo;

namespace ec.edu.monster.servicio
{
    public interface IClienteRest
    {
        string ErrorConfiguracion { get; }
        Task<RespuestaRest> AutenticarAsync(string usuario, string clave);
        Task<RespuestaRest> RecuperarClaveAsync(string usuario, string nuevaClave);
        Task<RespuestaRest> ObtenerCategoriasAsync(string token);
        Task<RespuestaRest> ObtenerUnidadesAsync(string token, string categoria);
        Task<RespuestaRest> ConvertirAsync(string token, string categoria, string unidadOrigen, string unidadDestino, double valor);
    }
}
