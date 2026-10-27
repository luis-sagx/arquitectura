using System;
using System.Collections.Generic;
using System.Reflection;
using System.ServiceModel;
using System.ServiceModel.Description;
using System.Web.Services.Description;
using System.Xml.Schema;
using ec.edu.monster.controladores.ws;
using ec.edu.monster.modelos;
using ec.edu.monster.servicios;

namespace ec.edu.monster.pruebas
{
internal static partial class PruebaServicioUnidades
{
    private static void ContratoSoap()
    {
        ServiceContractAttribute contrato = (ServiceContractAttribute)Attribute.GetCustomAttribute(typeof(IWSConUni), typeof(ServiceContractAttribute));
        Exigir(contrato != null && contrato.Name == "WSConUni" && contrato.Namespace == "http://ws.controladores.monster.edu.ec/",
            "Nombre o namespace del contrato SOAP incorrecto");

        string[] operaciones = { "Autenticar", "RecuperarClave", "ObtenerCategorias", "ObtenerUnidades", "Convertir" };
        string[] nombresSoap = { "autenticar", "recuperarClave", "obtenerCategorias", "obtenerUnidades", "convertir" };
        string[][] parametros = {
            new string[] { "usuario", "clave" },
            new string[] { "usuario", "nuevaClave" },
            new string[] { "token" },
            new string[] { "token", "categoria" },
            new string[] { "token", "categoria", "unidadOrigen", "unidadDestino", "valor" }
        };

        for (int i = 0; i < operaciones.Length; i++)
        {
            MethodInfo metodo = typeof(IWSConUni).GetMethod(operaciones[i]);
            Exigir(metodo != null, "Falta operacion SOAP " + operaciones[i]);
            OperationContractAttribute operacion = (OperationContractAttribute)Attribute.GetCustomAttribute(metodo, typeof(OperationContractAttribute));
            Exigir(operacion != null && operacion.Name == nombresSoap[i], "Nombre SOAP incorrecto para " + operaciones[i]);
            ParameterInfo[] argumentos = metodo.GetParameters();
            Exigir(argumentos.Length == parametros[i].Length, "Cantidad de parametros incorrecta para " + operaciones[i]);
            for (int j = 0; j < argumentos.Length; j++)
            {
                MessageParameterAttribute parametro = (MessageParameterAttribute)Attribute.GetCustomAttribute(argumentos[j], typeof(MessageParameterAttribute));
                Exigir(parametro != null && parametro.Name == parametros[i][j], "Nombre XML de parametro incorrecto en " + operaciones[i]);
            }
        }

        ServiceBehaviorAttribute comportamiento = (ServiceBehaviorAttribute)Attribute.GetCustomAttribute(typeof(WSConUni), typeof(ServiceBehaviorAttribute));
        Exigir(comportamiento != null && comportamiento.InstanceContextMode == InstanceContextMode.Single &&
            comportamiento.ConcurrencyMode == ConcurrencyMode.Multiple, "El servicio SOAP debe compartir estado y admitir llamadas sincronizadas");

        var exporter = new WsdlExporter();
        var endpoint = new ServiceEndpoint(ContractDescription.GetContract(typeof(IWSConUni)),
            new BasicHttpBinding(), new EndpointAddress("http://localhost/WSConUni.svc"));
        exporter.ExportEndpoint(endpoint);
        Exigir(exporter.Errors.Count == 0, "WCF no pudo exportar el contrato a WSDL");
        var operacionesWSDL = new List<string>();
        foreach (System.Web.Services.Description.ServiceDescription documento in exporter.GeneratedWsdlDocuments)
            foreach (System.Web.Services.Description.PortType portType in documento.PortTypes)
                foreach (System.Web.Services.Description.Operation operacion in portType.Operations)
                    operacionesWSDL.Add(operacion.Name);
        Exigir(String.Join(",", operacionesWSDL) == "autenticar,recuperarClave,obtenerCategorias,obtenerUnidades,convertir",
            "El WSDL generado no publica exactamente las cinco operaciones esperadas: " + String.Join(",", operacionesWSDL));

        var camposRespuesta = new List<string>();
        bool categoriasComoElementosRepetidos = false;
        bool unidadesComoElementosRepetidos = false;
        foreach (XmlSchema esquema in exporter.GeneratedXmlSchemas.Schemas())
            foreach (XmlSchemaObject elemento in esquema.Items)
            {
                XmlSchemaComplexType tipo = elemento as XmlSchemaComplexType;
                if (tipo == null || tipo.Name != "Respuesta") continue;
                XmlSchemaSequence secuencia = tipo.Particle as XmlSchemaSequence;
                if (secuencia == null) continue;
                foreach (XmlSchemaObject campo in secuencia.Items)
                {
                    XmlSchemaElement dato = campo as XmlSchemaElement;
                    if (dato != null)
                    {
                        camposRespuesta.Add(dato.Name);
                        bool esString = dato.SchemaTypeName.Name == "string" &&
                            dato.SchemaTypeName.Namespace == "http://www.w3.org/2001/XMLSchema";
                        bool esListaDirecta = esString && dato.MaxOccursString == "unbounded";
                        if (dato.Name == "categorias") categoriasComoElementosRepetidos = esListaDirecta;
                        if (dato.Name == "unidades") unidadesComoElementosRepetidos = esListaDirecta;
                    }
                }
            }
        Exigir(String.Join(",", camposRespuesta) ==
            "estado,codigo,mensaje,token,expiraEn,resultado,categoria,unidadOrigen,unidadDestino,categorias,unidades",
            "Los miembros de Respuesta del WSDL no coinciden con el contrato Java: " + String.Join(",", camposRespuesta));
        Exigir(categoriasComoElementosRepetidos && unidadesComoElementosRepetidos,
            "El WSDL debe representar categorias y unidades como elementos repetidos, no como listas envueltas");
    }
}
}