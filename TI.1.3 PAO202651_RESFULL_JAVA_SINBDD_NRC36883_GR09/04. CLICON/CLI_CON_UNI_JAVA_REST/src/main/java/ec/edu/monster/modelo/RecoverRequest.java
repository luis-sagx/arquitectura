package ec.edu.monster.modelo;

/** Solicitud JSON para recuperar la clave. */
public final class RecoverRequest {
    private String usuario;
    private String nuevaClave;

    public RecoverRequest() {
    }

    public RecoverRequest(String usuario, String nuevaClave) {
        this.usuario = usuario;
        this.nuevaClave = nuevaClave;
    }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getNuevaClave() { return nuevaClave; }
    public void setNuevaClave(String nuevaClave) { this.nuevaClave = nuevaClave; }
}
