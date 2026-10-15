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

internal static class PruebaServicioUnidades
{
    private static int comprobaciones;

    private static void Exigir(bool condicion, string mensaje)
    {
        comprobaciones++;
        if (!condicion) throw new InvalidOperationException(mensaje);
    }

    private static void Codigo(Respuesta respuesta, string esperado)
    {
        Exigir(respuesta != null && respuesta.Codigo == esperado,
            "Se esperaba codigo " + esperado + ", se obtuvo " + (respuesta == null ? "null" : respuesta.Codigo));
    }

    private static void Resultado(Respuesta respuesta, double esperado)
    {
        Codigo(respuesta, "OK");
        Exigir(respuesta.Resultado.HasValue && Math.Abs(respuesta.Resultado.Value - esperado) <= Math.Abs(esperado) * 1e-12 + 1e-12,
            "Resultado esperado " + esperado + ", obtenido " + (respuesta.Resultado.HasValue ? respuesta.Resultado.Value.ToString("R") : "null"));
    }

    private static string Token(ConversorUnidadesServices servicio)
    {
        Respuesta respuesta = servicio.Autenticar("monster", "monster123");
        Codigo(respuesta, "OK");
        Exigir(!String.IsNullOrWhiteSpace(respuesta.Token), "Autenticacion debe emitir token");
        return respuesta.Token;
    }

    private static void Main()
    {
        try
        {
            AutenticacionYCatalogo();
            ConversionesYValidaciones();
            SesionYRecuperacion();
            ContratoSoap();
            Console.WriteLine("PruebaServicioUnidades: OK (" + comprobaciones + " comprobaciones)");
        }
        catch (Exception error)
        {
            Console.Error.WriteLine("PruebaServicioUnidades: ERROR: " + error.Message);
            Environment.ExitCode = 1;
        }
    }

    private static void AutenticacionYCatalogo()
    {
        var servicio = new ConversorUnidadesServices();
        string token1 = Token(servicio);
        string token2 = Token(servicio);
        Exigir(token1 != token2, "Cada autenticacion debe generar un token distinto");
        Codigo(servicio.Autenticar("monster", "incorrecta"), "AUTH-001");
        Codigo(servicio.Autenticar("desconocido", "monster123"), "AUTH-001");
        Codigo(servicio.Autenticar(null, null), "AUTH-001");
        Codigo(servicio.Autenticar("", ""), "AUTH-001");
        Codigo(servicio.ObtenerCategorias(null), "AUTH-002");

        Respuesta categorias = servicio.ObtenerCategorias(token1);
        Codigo(categorias, "OK");
        Exigir(String.Join(",", categorias.Categorias) == "LONGITUD,MASA,TEMPERATURA", "Catalogo de categorias incorrecto");
        categorias.Categorias.Clear();
        Exigir(servicio.ObtenerCategorias(token1).Categorias.Count == 3, "El cliente no debe modificar el catalogo interno");
        Exigir(String.Join(",", servicio.ObtenerUnidades(token1, " longitud ").Unidades) ==
            "KILOMETRO,METRO,CENTIMETRO,MILIMETRO,MILLA,PIE", "Catalogo de longitud incorrecto");
        Exigir(String.Join(",", servicio.ObtenerUnidades(token1, "MASA").Unidades) ==
            "KILOGRAMO,GRAMO,MILIGRAMO,LIBRA", "Catalogo de masa incorrecto");
        Exigir(String.Join(",", servicio.ObtenerUnidades(token1, "TEMPERATURA").Unidades) ==
            "CELSIUS,FAHRENHEIT,KELVIN", "Catalogo de temperatura incorrecto");
        Codigo(servicio.ObtenerUnidades(token1, "INEXISTENTE"), "DATA-001");
    }

    private static void ConversionesYValidaciones()
    {
        var servicio = new ConversorUnidadesServices();
        string token = Token(servicio);
        Respuesta longitud = servicio.Convertir(token, " longitud ", " metro ", "centimetro", 2.0);
        Resultado(longitud, 200.0);
        Exigir(longitud.Categoria == "LONGITUD" && longitud.UnidadOrigen == "METRO" && longitud.UnidadDestino == "CENTIMETRO",
            "La respuesta debe identificar magnitud y unidades normalizadas");
        string[] unidadesLongitud = { "KILOMETRO", "METRO", "CENTIMETRO", "MILIMETRO", "MILLA", "PIE" };
        double[] metros = { 1000.0, 1.0, 0.01, 0.001, 1609.344, 0.3048 };
        for (int i = 0; i < unidadesLongitud.Length; i++)
        {
            Resultado(servicio.Convertir(token, "LONGITUD", unidadesLongitud[i], "METRO", 1.0), metros[i]);
            Resultado(servicio.Convertir(token, "LONGITUD", "METRO", unidadesLongitud[i], metros[i]), 1.0);
            Resultado(servicio.Convertir(token, "LONGITUD", unidadesLongitud[i], unidadesLongitud[i], 7.5), 7.5);
        }
        Resultado(servicio.Convertir(token, "LONGITUD", "METRO", "PIE", 0.0), 0.0);
        Resultado(servicio.Convertir(token, "MASA", "KILOGRAMO", "GRAMO", 0.0), 0.0);
        Resultado(servicio.Convertir(token, "MASA", "KILOGRAMO", "LIBRA", 0.45359237), 1.0);
        Resultado(servicio.Convertir(token, "MASA", "KILOGRAMO", "GRAMO", 1.0), 1000.0);
        Resultado(servicio.Convertir(token, "MASA", "KILOGRAMO", "MILIGRAMO", 1.0), 1000000.0);
        Resultado(servicio.Convertir(token, "TEMPERATURA", "CELSIUS", "FAHRENHEIT", -40.0), -40.0);
        Resultado(servicio.Convertir(token, "TEMPERATURA", "CELSIUS", "FAHRENHEIT", 100.0), 212.0);
        Resultado(servicio.Convertir(token, "TEMPERATURA", "CELSIUS", "KELVIN", 100.0), 373.15);

        Resultado(servicio.Convertir(token, "TEMPERATURA", "CELSIUS", "KELVIN", -273.15), 0.0);
        Resultado(servicio.Convertir(token, "TEMPERATURA", "FAHRENHEIT", "KELVIN", -459.67), 0.0);
        Resultado(servicio.Convertir(token, "TEMPERATURA", "KELVIN", "CELSIUS", 0.0), -273.15);
        Codigo(servicio.Convertir(token, "TEMPERATURA", "CELSIUS", "KELVIN", -273.150001), "DATA-001");
        Codigo(servicio.Convertir(token, "TEMPERATURA", "FAHRENHEIT", "KELVIN", -459.670001), "DATA-001");

        Resultado(servicio.Convertir(token, "TEMPERATURA", "FAHRENHEIT", "CELSIUS", 1e308), 5.555555555555556e307);
        Resultado(servicio.Convertir(token, "TEMPERATURA", "FAHRENHEIT", "FAHRENHEIT", 1e308), 1e308);
        Resultado(servicio.Convertir(token, "TEMPERATURA", "CELSIUS", "KELVIN", 1e308), 1e308);

        Codigo(servicio.Convertir(token, "LONGITUD", "METRO", "PIE", null), "DATA-001");
        Codigo(servicio.Convertir(token, "LONGITUD", "METRO", "PIE", Double.NaN), "DATA-001");
        Codigo(servicio.Convertir(token, "LONGITUD", "METRO", "PIE", Double.PositiveInfinity), "DATA-001");
        Codigo(servicio.Convertir(token, "LONGITUD", "METRO", "PIE", Double.NegativeInfinity), "DATA-001");
        Codigo(servicio.Convertir(token, "LONGITUD", "METRO", "PIE", -1.0), "DATA-001");
        Codigo(servicio.Convertir(token, "MASA", "KILOGRAMO", "GRAMO", -1.0), "DATA-001");
        Codigo(servicio.Convertir(token, null, "METRO", "PIE", 1.0), "DATA-001");
        Codigo(servicio.Convertir(token, "LONGITUD", "", "PIE", 1.0), "DATA-001");
        Codigo(servicio.Convertir(token, "LONGITUD", "METRO", "LIBRA", 1.0), "DATA-001");
        Codigo(servicio.Convertir(token, "LONGITUD", "DESCONOCIDA", "METRO", 1.0), "UNIT-001");
        Codigo(servicio.Convertir(token, "LONGITUD", "MILLA", "CENTIMETRO", Double.MaxValue), "DATA-001");
        Codigo(servicio.Convertir("inventado", null, null, null, null), "AUTH-002");
    }

    private static void SesionYRecuperacion()
    {
        DateTimeOffset ahora = new DateTimeOffset(2026, 10, 9, 0, 0, 0, TimeSpan.Zero);
        var servicio = new ConversorUnidadesServices("monster", "monster123", () => ahora, TimeSpan.FromMinutes(15));
        string token = Token(servicio);
        ahora = ahora.AddSeconds(899);
        Resultado(servicio.Convertir(token, "LONGITUD", "METRO", "CENTIMETRO", 1.0), 100.0);
        ahora = ahora.AddSeconds(1);
        Codigo(servicio.Convertir(token, "LONGITUD", "METRO", "CENTIMETRO", 1.0), "AUTH-003");
        Codigo(servicio.ObtenerCategorias(token), "AUTH-003");
        Codigo(servicio.ObtenerUnidades(token, "MASA"), "AUTH-003");

        Codigo(servicio.RecuperarClave("desconocido", "nueva123"), "AUTH-001");
        Codigo(servicio.RecuperarClave("monster", "  "), "DATA-001");
        string tokenAnterior = Token(servicio);
        Codigo(servicio.RecuperarClave("monster", "nueva123"), "OK");
        Codigo(servicio.Autenticar("monster", "monster123"), "AUTH-001");
        Respuesta nuevaSesion = servicio.Autenticar("monster", "nueva123");
        Codigo(nuevaSesion, "OK");
        Codigo(servicio.ObtenerCategorias(tokenAnterior), "AUTH-002");
    }

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
