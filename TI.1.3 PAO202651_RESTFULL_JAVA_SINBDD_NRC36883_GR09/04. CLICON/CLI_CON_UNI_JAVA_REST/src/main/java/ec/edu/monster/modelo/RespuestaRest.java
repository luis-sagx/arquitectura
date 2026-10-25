package ec.edu.monster.modelo;

import java.util.ArrayList;
import java.util.List;

/** Respuesta JSON devuelta por el API REST de conversión de unidades. */
public final class RespuestaRest {
    private int estadoHttp;
    private String estado;
    private String codigo;
    private String mensaje;
    private String token;
    private String expiraEn;
    private Double resultado;
    private String categoria;
    private String unidadOrigen;
    private String unidadDestino;
    private List<String> categorias = new ArrayList<>();
    private List<String> unidades = new ArrayList<>();

    public RespuestaRest() {
    }

    public int getEstadoHttp() { return estadoHttp; }
    public void setEstadoHttp(int estadoHttp) { this.estadoHttp = estadoHttp; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getExpiraEn() { return expiraEn; }
    public void setExpiraEn(String expiraEn) { this.expiraEn = expiraEn; }
    public Double getResultado() { return resultado; }
    public void setResultado(Double resultado) { this.resultado = resultado; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getUnidadOrigen() { return unidadOrigen; }
    public void setUnidadOrigen(String unidadOrigen) { this.unidadOrigen = unidadOrigen; }
    public String getUnidadDestino() { return unidadDestino; }
    public void setUnidadDestino(String unidadDestino) { this.unidadDestino = unidadDestino; }
    public List<String> getCategorias() { return categorias == null ? List.of() : categorias; }
    public void setCategorias(List<String> categorias) {
        this.categorias = categorias == null ? new ArrayList<>() : new ArrayList<>(categorias);
    }
    public List<String> getUnidades() { return unidades == null ? List.of() : unidades; }
    public void setUnidades(List<String> unidades) {
        this.unidades = unidades == null ? new ArrayList<>() : new ArrayList<>(unidades);
    }

    public boolean isExito() {
        return estadoHttp >= 200 && estadoHttp < 300 && "OK".equals(codigo);
    }

    public boolean isSesionInvalida() {
        return "AUTH-002".equals(codigo) || "AUTH-003".equals(codigo);
    }
}


