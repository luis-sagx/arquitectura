package ec.edu.monster.modelo;

import java.util.List;

/** Modelo JSON de las respuestas publicadas por el servidor REST. */
public class RespuestaRest {
    private String estado;
    private String codigo;
    private String mensaje;
    private String token;
    private String expiraEn;
    private Double resultado;
    private String categoria;
    private String unidadOrigen;
    private String unidadDestino;
    private List<String> categorias;
    private List<String> unidades;

    public RespuestaRest() {
    }

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
    public List<String> getCategorias() { return categorias; }
    public void setCategorias(List<String> categorias) { this.categorias = categorias; }
    public List<String> getUnidades() { return unidades; }
    public void setUnidades(List<String> unidades) { this.unidades = unidades; }

    public boolean esExito() {
        return "EXITO".equals(estado) && "OK".equals(codigo);
    }

    public boolean esSesionInvalida() {
        return "AUTH-002".equals(codigo) || "AUTH-003".equals(codigo);
    }

    public String mensajeSeguro() {
        return mensaje == null || mensaje.isBlank() ? "El servidor devolvió una respuesta incompleta." : mensaje;
    }
}
