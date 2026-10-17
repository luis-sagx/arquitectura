package ec.edu.monster.modelo;

import java.util.List;

/** Respuesta de negocio normalizada para que el MVC no dependa de JAXB. */
public record RespuestaSoap(
        String estado,
        String codigo,
        String mensaje,
        String token,
        String expiraEn,
        Double resultado,
        String unidadOrigen,
        String unidadDestino,
        List<String> categorias,
        List<String> unidades) {

    public RespuestaSoap {
        estado = texto(estado);
        codigo = texto(codigo);
        mensaje = texto(mensaje);
        token = texto(token);
        expiraEn = texto(expiraEn);
        unidadOrigen = texto(unidadOrigen);
        unidadDestino = texto(unidadDestino);
        categorias = categorias == null ? List.of() : List.copyOf(categorias);
        unidades = unidades == null ? List.of() : List.copyOf(unidades);
    }

    public boolean isExito() {
        return "EXITO".equalsIgnoreCase(estado) && "OK".equalsIgnoreCase(codigo);
    }

    public boolean isSesionInvalida() {
        return "AUTH-002".equals(codigo) || "AUTH-003".equals(codigo);
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor;
    }
}
