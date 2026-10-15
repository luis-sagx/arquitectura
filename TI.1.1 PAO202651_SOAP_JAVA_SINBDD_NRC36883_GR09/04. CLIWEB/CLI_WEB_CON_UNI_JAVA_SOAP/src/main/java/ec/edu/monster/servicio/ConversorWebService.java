package ec.edu.monster.servicio;
import ec.edu.monster.modelo.RespuestaSoap;import jakarta.servlet.ServletContext;import java.io.IOException;import java.net.URI;
public final class ConversorWebService{
 private final ServletContext ctx;public ConversorWebService(ServletContext ctx){this.ctx=ctx;}
 private ClienteSoap cliente(){String url=System.getProperty("conversor.soap.endpoint");if(url==null||url.isBlank())url=ctx.getInitParameter("soap.endpoint");return new ClienteSoap(URI.create(url));}
 public RespuestaSoap autenticar(String u,String c)throws IOException{return cliente().autenticar(u,c);}public RespuestaSoap recuperarClave(String u,String c)throws IOException{return cliente().recuperarClave(u,c);}public RespuestaSoap obtenerCategorias(String t)throws IOException{return cliente().obtenerCategorias(t);}public RespuestaSoap obtenerUnidades(String t,String c)throws IOException{return cliente().obtenerUnidades(t,c);}public RespuestaSoap convertir(String t,String c,String o,String d,double v)throws IOException{return cliente().convertir(t,c,o,d,v);}
}
