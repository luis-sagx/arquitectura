using ec.edu.monster.modelo;

namespace ec.edu.monster.servicio
{
    public interface IClienteSoap
    {
        RespuestaSoap Autenticar(string usuario, string clave);
        RespuestaSoap RecuperarClave(string usuario, string nuevaClave);
        RespuestaSoap ObtenerCategorias(string token);
        RespuestaSoap ObtenerUnidades(string token, string categoria);
        RespuestaSoap Convertir(string token, string categoria, string unidadOrigen,
            string unidadDestino, double valor);
    }
}
