package ec.edu.monster.modelos;

/** Datos JSON para convertir una magnitud entre dos unidades. */
public class ConversionRequest {
    private String categoria;
    private String unidadOrigen;
    private String unidadDestino;
    private Double valor;

    public ConversionRequest() {
    }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getUnidadOrigen() { return unidadOrigen; }
    public void setUnidadOrigen(String unidadOrigen) { this.unidadOrigen = unidadOrigen; }
    public String getUnidadDestino() { return unidadDestino; }
    public void setUnidadDestino(String unidadDestino) { this.unidadDestino = unidadDestino; }
    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }
}
