package ec.edu.monster.servicio;
import com.sun.xml.ws.developer.JAXWSProperties;
import ec.edu.monster.modelo.RespuestaSoap;
import ec.edu.monster.clientesws.Respuesta;
import ec.edu.monster.clientesws.WSConUni;
import ec.edu.monster.clientesws.WSConUni_Service;
import jakarta.xml.ws.*;import jakarta.xml.ws.soap.SOAPFaultException;
import java.io.IOException;import java.net.URI;import java.util.List;
/** Adaptador JAX-WS, en la capa de servicio como en los otros clientes. */
public final class ClienteSoap {
 private final URI endpoint;
 public ClienteSoap(URI endpoint){if(endpoint==null||endpoint.getHost()==null||!List.of("http","https").contains(endpoint.getScheme()))throw new IllegalArgumentException("La URL SOAP debe usar HTTP o HTTPS.");this.endpoint=endpoint;}
 public RespuestaSoap autenticar(String u,String c)throws IOException{return invocar(p->p.autenticar(u,c));}
 public RespuestaSoap recuperarClave(String u,String c)throws IOException{return invocar(p->p.recuperarClave(u,c));}
 public RespuestaSoap obtenerCategorias(String t)throws IOException{return invocar(p->p.obtenerCategorias(t));}
 public RespuestaSoap obtenerUnidades(String t,String c)throws IOException{return invocar(p->p.obtenerUnidades(t,c));}
 public RespuestaSoap convertir(String t,String c,String o,String d,double v)throws IOException{return invocar(p->p.convertir(t,c,o,d,v));}
 private RespuestaSoap invocar(Operacion op)throws IOException{try{
  var wsdl=ClienteSoap.class.getResource("/wsdl/WSConUni.wsdl");if(wsdl==null)throw new IOException("No se encontró el WSDL local.");
  WSConUni p=new WSConUni_Service(wsdl).getWSConUniPort();var ctx=((BindingProvider)p).getRequestContext();ctx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,endpoint.toString());ctx.put(JAXWSProperties.CONNECT_TIMEOUT,10000);ctx.put(JAXWSProperties.REQUEST_TIMEOUT,30000);
  Respuesta r=op.run(p);if(r==null||r.getCodigo()==null||r.getMensaje()==null)throw new IOException("Respuesta SOAP incompleta.");if(r.getResultado()!=null&&!Double.isFinite(r.getResultado()))throw new IOException("Resultado numérico inválido.");
  return new RespuestaSoap(r.getEstado(),r.getCodigo(),r.getMensaje(),r.getToken(),r.getExpiraEn(),r.getResultado(),r.getUnidadOrigen(),r.getUnidadDestino(),r.getCategorias(),r.getUnidades());
 }catch(SOAPFaultException e){throw new IOException("El servidor SOAP devolvió un error.",e);}catch(WebServiceException e){throw new IOException("No se pudo conectar con el servidor SOAP. Comprueba su URL y despliegue.",e);}}
 @FunctionalInterface private interface Operacion{Respuesta run(WSConUni p);}
}
