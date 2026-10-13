package ec.edu.monster.controladores.ws;

import ec.edu.monster.modelos.Respuesta;
import ec.edu.monster.servicios.ConversorUnidadesServices;
import jakarta.jws.WebService;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;

@WebService(serviceName = "WSConUni", targetNamespace = "http://ws.controladores.monster.edu.ec/")
public class WSConUni {
    // Una instancia compartida conserva tokens aunque JAX-WS cree varios controladores.
    private static final ConversorUnidadesServices SERVICIO = new ConversorUnidadesServices();

    @WebMethod(operationName = "autenticar")
    public Respuesta autenticar(@WebParam(name = "usuario") String usuario,
            @WebParam(name = "clave") String clave) {
        return SERVICIO.autenticar(usuario, clave);
    }

    @WebMethod(operationName = "recuperarClave")
    public Respuesta recuperarClave(@WebParam(name = "usuario") String usuario,
            @WebParam(name = "nuevaClave") String nuevaClave) {
        return SERVICIO.recuperarClave(usuario, nuevaClave);
    }

    @WebMethod(operationName = "obtenerCategorias")
    public Respuesta obtenerCategorias(@WebParam(name = "token") String token) {
        return SERVICIO.obtenerCategorias(token);
    }

    @WebMethod(operationName = "obtenerUnidades")
    public Respuesta obtenerUnidades(@WebParam(name = "token") String token,
            @WebParam(name = "categoria") String categoria) {
        return SERVICIO.obtenerUnidades(token, categoria);
    }

    @WebMethod(operationName = "convertir")
    public Respuesta convertir(@WebParam(name = "token") String token,
            @WebParam(name = "categoria") String categoria,
            @WebParam(name = "unidadOrigen") String unidadOrigen,
            @WebParam(name = "unidadDestino") String unidadDestino,
            @WebParam(name = "valor") Double valor) {
        return SERVICIO.convertir(token, categoria, unidadOrigen, unidadDestino, valor);
    }
}
