package ec.edu.monster.controladores;

import ec.edu.monster.servicios.ConversorUnidadesServices;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** Controlador REST para consultar el catálogo de magnitudes y unidades. */
@Path("catalog")
@Produces(MediaType.APPLICATION_JSON)
public class CatalogResource {
    @Inject
    private ConversorUnidadesServices servicio;

    @GET
    @Path("categories")
    public Response obtenerCategorias(@HeaderParam("Authorization") String autorizacion) {
        return RestResponseMapper.respuesta(
            servicio.obtenerCategorias(RestResponseMapper.tokenBearer(autorizacion)));
    }

    @GET
    @Path("categories/{categoria}/units")
    public Response obtenerUnidades(@HeaderParam("Authorization") String autorizacion,
            @PathParam("categoria") String categoria) {
        return RestResponseMapper.respuesta(
            servicio.obtenerUnidades(RestResponseMapper.tokenBearer(autorizacion), categoria));
    }
}
