package ec.edu.monster.modelos;

/** Datos JSON para iniciar sesión. */
public class LoginRequest {
    private String usuario;
    private String clave;

    public LoginRequest() {
    }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
}
