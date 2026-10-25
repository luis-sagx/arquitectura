package ec.edu.monster.modelo;

import java.util.ArrayList;
import java.util.List;

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

    public int getEstadoHttp() { return estadoHttp; }
    public void setEstadoHttp(int valor) { estadoHttp = valor; }
    public String getEstado() { return estado; }
    public void setEstado(String valor) { estado = valor; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String valor) { codigo = valor; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String valor) { mensaje = valor; }
    public String getToken() { return token; }
    public void setToken(String valor) { token = valor; }
    public String getExpiraEn() { return expiraEn; }
    public void setExpiraEn(String valor) { expiraEn = valor; }
    public Double getResultado() { return resultado; }
    public void setResultado(Double valor) { resultado = valor; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String valor) { categoria = valor; }
    public String getUnidadOrigen() { return unidadOrigen; }
    public void setUnidadOrigen(String valor) { unidadOrigen = valor; }
    public String getUnidadDestino() { return unidadDestino; }
    public void setUnidadDestino(String valor) { unidadDestino = valor; }
    public List<String> getCategorias() { return categorias == null ? List.of() : categorias; }
    public void setCategorias(List<String> valor) { categorias = valor == null ? new ArrayList<>() : new ArrayList<>(valor); }
    public List<String> getUnidades() { return unidades == null ? List.of() : unidades; }
    public void setUnidades(List<String> valor) { unidades = valor == null ? new ArrayList<>() : new ArrayList<>(valor); }

    public boolean isExito() { return estadoHttp >= 200 && estadoHttp < 300 && "OK".equals(codigo); }
    public boolean isSesionInvalida() { return "AUTH-002".equals(codigo) || "AUTH-003".equals(codigo); }
}
