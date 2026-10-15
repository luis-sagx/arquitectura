using System.Collections.Generic;
using System.Xml.Serialization;

namespace ec.edu.monster.modelos
{
    [XmlType(TypeName = "Respuesta", Namespace = "http://ws.controladores.monster.edu.ec/")]
    [XmlRoot(ElementName = "Respuesta", Namespace = "http://ws.controladores.monster.edu.ec/")]
    public class Respuesta
    {
        public Respuesta()
        {
            Categorias = new List<string>();
            Unidades = new List<string>();
        }

        [XmlElement(ElementName = "estado", Order = 1)]
        public string Estado { get; set; }

        [XmlElement(ElementName = "codigo", Order = 2)]
        public string Codigo { get; set; }

        [XmlElement(ElementName = "mensaje", Order = 3)]
        public string Mensaje { get; set; }

        [XmlElement(ElementName = "token", Order = 4)]
        public string Token { get; set; }

        [XmlElement(ElementName = "expiraEn", Order = 5)]
        public string ExpiraEn { get; set; }

        [XmlElement(ElementName = "resultado", Order = 6)]
        public double? Resultado { get; set; }

        [XmlElement(ElementName = "categoria", Order = 7)]
        public string Categoria { get; set; }

        [XmlElement(ElementName = "unidadOrigen", Order = 8)]
        public string UnidadOrigen { get; set; }

        [XmlElement(ElementName = "unidadDestino", Order = 9)]
        public string UnidadDestino { get; set; }

        [XmlElement(ElementName = "categorias", Order = 10)]
        public List<string> Categorias { get; set; }

        [XmlElement(ElementName = "unidades", Order = 11)]
        public List<string> Unidades { get; set; }

        public static Respuesta Exito(string mensaje)
        {
            return new Respuesta { Estado = "EXITO", Codigo = "OK", Mensaje = mensaje };
        }

        public static Respuesta Error(string codigo, string mensaje)
        {
            return new Respuesta { Estado = "ERROR", Codigo = codigo, Mensaje = mensaje };
        }
    }
}
