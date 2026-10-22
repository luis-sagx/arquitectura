using System.Runtime.Serialization;

namespace ec.edu.monster.modelo
{
    [DataContract]
    public sealed class SolicitudAutenticacionRest
    {
        [DataMember(Name = "usuario", IsRequired = true)]
        public string Usuario { get; set; }

        [DataMember(Name = "clave", IsRequired = true)]
        public string Clave { get; set; }
    }

    [DataContract]
    public sealed class SolicitudRecuperacionRest
    {
        [DataMember(Name = "usuario", IsRequired = true)]
        public string Usuario { get; set; }

        [DataMember(Name = "nuevaClave", IsRequired = true)]
        public string NuevaClave { get; set; }
    }

    [DataContract]
    public sealed class SolicitudConversionRest
    {
        [DataMember(Name = "categoria", IsRequired = true)]
        public string Categoria { get; set; }

        [DataMember(Name = "unidadOrigen", IsRequired = true)]
        public string UnidadOrigen { get; set; }

        [DataMember(Name = "unidadDestino", IsRequired = true)]
        public string UnidadDestino { get; set; }

        [DataMember(Name = "valor", IsRequired = true)]
        public double Valor { get; set; }
    }
}
