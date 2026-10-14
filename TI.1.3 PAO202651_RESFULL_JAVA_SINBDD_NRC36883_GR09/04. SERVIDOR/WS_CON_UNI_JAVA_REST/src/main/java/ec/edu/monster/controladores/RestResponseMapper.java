package ec.edu.monster.controladores;

import ec.edu.monster.modelos.Respuesta;
import jakarta.ws.rs.core.Response;

/** Adapta las respuestas del modelo de negocio al protocolo HTTP. */
public final class RestResponseMapper {
    private RestResponseMapper() {
    }

    public static Response respuesta(Respuesta respuesta) {
        int estadoHttp = "OK".equals(respuesta.getCodigo())
            ? Response.Status.OK.getStatusCode()
            : respuesta.getCodigo() != null && respuesta.getCodigo().startsWith("AUTH-")
                ? Response.Status.UNAUTHORIZED.getStatusCode()
                : Response.Status.BAD_REQUEST.getStatusCode();
        return Response.status(estadoHttp).entity(respuesta).build();
    }

    public static String tokenBearer(String autorizacion) {
        if (autorizacion == null) return null;
        String encabezado = autorizacion.trim();
        if (encabezado.length() < 7 || !encabezado.regionMatches(true, 0, "Bearer ", 0, 7))
            return null;
        String token = encabezado.substring(7).trim();
        return token.isEmpty() ? null : token;
    }
}
