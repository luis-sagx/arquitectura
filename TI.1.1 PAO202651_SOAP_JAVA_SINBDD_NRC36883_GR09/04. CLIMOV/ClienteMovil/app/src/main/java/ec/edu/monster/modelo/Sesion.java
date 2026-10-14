package ec.edu.monster.modelo;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Sesión temporal del cliente; el token solo vive en memoria y puede borrarse localmente. */
public final class Sesion {
    private String token;
    private final String expiraEn;

    public Sesion(String token, String expiraEn) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("El token de sesión es obligatorio");
        }
        this.token = token;
        this.expiraEn = expiraEn == null ? "" : expiraEn;
    }

    public String token() {
        if (token == null) {
            throw new IllegalStateException("La sesión ya fue cerrada");
        }
        return token;
    }

    public String expiraEn() {
        return expiraEn;
    }

    /** Hora local de vencimiento; si el servidor envía otro formato se muestra tal cual. */
    public String horaExpiracion() {
        try {
            return DateTimeFormatter.ofPattern("HH:mm")
                    .withZone(ZoneId.systemDefault())
                    .format(Instant.parse(expiraEn));
        } catch (DateTimeParseException ex) {
            return expiraEn;
        }
    }

    public boolean isActiva() {
        return token != null;
    }

    public void limpiarToken() {
        token = null;
    }
}
