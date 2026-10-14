package ec.edu.monster.modelos;

/** Datos JSON para recuperar la clave. */
public class RecoverRequest {
    private String usuario;
    private String nuevaClave;

    public RecoverRequest() {
    }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getNuevaClave() { return nuevaClave; }
    public void setNuevaClave(String nuevaClave) { this.nuevaClave = nuevaClave; }
}
