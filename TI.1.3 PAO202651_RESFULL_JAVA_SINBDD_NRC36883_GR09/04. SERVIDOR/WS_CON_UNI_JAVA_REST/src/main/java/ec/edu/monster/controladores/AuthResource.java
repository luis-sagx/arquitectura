package ec.edu.monster.controladores;

import ec.edu.monster.modelos.LoginRequest;
import ec.edu.monster.modelos.RecoverRequest;
import ec.edu.monster.servicios.ConversorUnidadesServices;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** Controlador REST para autenticación y recuperación de clave. */
@Path("auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {
    @Inject
    private ConversorUnidadesServices servicio;

    @POST
    @Path("login")
    public Response autenticar(LoginRequest solicitud) {
        return RestResponseMapper.respuesta(servicio.autenticar(
            solicitud == null ? null : solicitud.getUsuario(),
            solicitud == null ? null : solicitud.getClave()));
    }

    @POST
    @Path("recover")
    public Response recuperarClave(RecoverRequest solicitud) {
        return RestResponseMapper.respuesta(servicio.recuperarClave(
            solicitud == null ? null : solicitud.getUsuario(),
            solicitud == null ? null : solicitud.getNuevaClave()));
    }
}
