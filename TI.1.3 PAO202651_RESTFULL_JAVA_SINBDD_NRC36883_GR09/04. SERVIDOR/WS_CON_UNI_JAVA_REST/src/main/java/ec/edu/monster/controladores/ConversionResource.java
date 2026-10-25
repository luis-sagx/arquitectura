package ec.edu.monster.controladores;

import ec.edu.monster.modelos.ConversionRequest;
import ec.edu.monster.servicios.ConversorUnidadesServices;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** Controlador REST para conversiones de unidades. */
@Path("conversions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ConversionResource {
    @Inject
    private ConversorUnidadesServices servicio;

    @POST
    public Response convertir(@HeaderParam("Authorization") String autorizacion, ConversionRequest solicitud) {
        return RestResponseMapper.respuesta(servicio.convertir(
            RestResponseMapper.tokenBearer(autorizacion),
            solicitud == null ? null : solicitud.getCategoria(),
            solicitud == null ? null : solicitud.getUnidadOrigen(),
            solicitud == null ? null : solicitud.getUnidadDestino(),
            solicitud == null ? null : solicitud.getValor()));
    }
}
