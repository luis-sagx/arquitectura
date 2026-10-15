package ec.edu.monster.modelo;
import java.util.List;
public record RespuestaSoap(String estado,String codigo,String mensaje,String token,String expiraEn,Double resultado,String unidadOrigen,String unidadDestino,List<String> categorias,List<String> unidades) {
 public RespuestaSoap { estado=t(estado);codigo=t(codigo);mensaje=t(mensaje);token=t(token);expiraEn=t(expiraEn);unidadOrigen=t(unidadOrigen);unidadDestino=t(unidadDestino);categorias=categorias==null?List.of():List.copyOf(categorias);unidades=unidades==null?List.of():List.copyOf(unidades); }
 public boolean isExito(){return "EXITO".equalsIgnoreCase(estado)&&"OK".equalsIgnoreCase(codigo);}
 public boolean isSesionInvalida(){return "AUTH-002".equals(codigo)||"AUTH-003".equals(codigo);}
 private static String t(String s){return s==null?"":s;}
}
