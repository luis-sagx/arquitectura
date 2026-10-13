package ec.edu.monster.modelo;

import java.util.List;

/** Datos normalizados que devuelve el servicio de conversión. */
public final class RespuestaSoap {
    private final String estado;
    private final String codigo;
    private final String mensaje;
    private final String token;
    private final String expiraEn;
    private final Double resultado;
    private final String unidadOrigen;
    private final String unidadDestino;
    private final List<String> categorias;
    private final List<String> unidades;

    public RespuestaSoap(String estado, String codigo, String mensaje, String token,
            String expiraEn, Double resultado, String unidadOrigen, String unidadDestino,
            List<String> categorias, List<String> unidades) {
        this.estado = estado;
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.token = token;
        this.expiraEn = expiraEn;
        this.resultado = resultado;
        this.unidadOrigen = unidadOrigen;
        this.unidadDestino = unidadDestino;
        this.categorias = List.copyOf(categorias);
        this.unidades = List.copyOf(unidades);
    }

    public String getEstado() { return estado; }
    public String getCodigo() { return codigo; }
    public String getMensaje() { return mensaje; }
    public String getToken() { return token; }
    public String getExpiraEn() { return expiraEn; }
    public Double getResultado() { return resultado; }
    public String getUnidadOrigen() { return unidadOrigen; }
    public String getUnidadDestino() { return unidadDestino; }
    public List<String> getCategorias() { return categorias; }
    public List<String> getUnidades() { return unidades; }

    public boolean isExito() { return "EXITO".equals(estado) && "OK".equals(codigo); }
    public boolean isSesionInvalida() { return "AUTH-002".equals(codigo) || "AUTH-003".equals(codigo); }
}
