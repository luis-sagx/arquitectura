package ec.edu.monster.modelo;

import java.util.List;

/** Datos normalizados que devuelve el servicio de conversión. */
public record RespuestaSoap(String estado, String codigo, String mensaje, String token,
        String expiraEn, Double resultado, String unidadOrigen, String unidadDestino,
        List<String> categorias, List<String> unidades) {
    public RespuestaSoap {
        categorias = List.copyOf(categorias);
        unidades = List.copyOf(unidades);
    }

    public boolean exito() { return "EXITO".equals(estado) && "OK".equals(codigo); }
    public boolean sesionInvalida() { return "AUTH-002".equals(codigo) || "AUTH-003".equals(codigo); }
}
