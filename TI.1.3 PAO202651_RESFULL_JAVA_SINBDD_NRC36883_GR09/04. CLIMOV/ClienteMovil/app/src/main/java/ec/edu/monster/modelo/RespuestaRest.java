package ec.edu.monster.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Respuesta de negocio normalizada para que el MVC no dependa del JSON del API REST. */
public final class RespuestaRest {
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

    public RespuestaRest(String estado, String codigo, String mensaje, String token, String expiraEn,
            Double resultado, String unidadOrigen, String unidadDestino,
            List<String> categorias, List<String> unidades) {
        this.estado = texto(estado);
        this.codigo = texto(codigo);
        this.mensaje = texto(mensaje);
        this.token = texto(token);
        this.expiraEn = texto(expiraEn);
        this.resultado = resultado;
        this.unidadOrigen = texto(unidadOrigen);
        this.unidadDestino = texto(unidadDestino);
        this.categorias = copia(categorias);
        this.unidades = copia(unidades);
    }

    public String estado() { return estado; }
    public String codigo() { return codigo; }
    public String mensaje() { return mensaje; }
    public String token() { return token; }
    public String expiraEn() { return expiraEn; }
    public Double resultado() { return resultado; }
    public String unidadOrigen() { return unidadOrigen; }
    public String unidadDestino() { return unidadDestino; }
    public List<String> categorias() { return categorias; }
    public List<String> unidades() { return unidades; }

    public boolean isExito() {
        return "EXITO".equalsIgnoreCase(estado) && "OK".equalsIgnoreCase(codigo);
    }

    public boolean isSesionInvalida() {
        return "AUTH-002".equals(codigo) || "AUTH-003".equals(codigo);
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor;
    }

    private static List<String> copia(List<String> valores) {
        return valores == null ? Collections.<String>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(valores));
    }
}
