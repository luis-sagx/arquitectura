package ec.edu.monster.modelo;

public final class Sesion {
    private String token;
    private final String expiraEn;

    public Sesion(String token, String expiraEn) {
        if (token == null || token.isBlank()) throw new IllegalArgumentException("El token de sesión es obligatorio");
        this.token = token;
        this.expiraEn = expiraEn == null ? "" : expiraEn;
    }

    public String token() {
        if (token == null) throw new IllegalStateException("La sesión ya fue cerrada");
        return token;
    }

    public String expiraEn() { return expiraEn; }
    public void limpiarToken() { token = null; }
}
