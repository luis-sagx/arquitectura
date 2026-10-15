namespace ec.edu.monster.modelo
{
    public sealed class Sesion
    {
        public Sesion(string token, string expiraEn)
        {
            Token = token;
            ExpiraEn = expiraEn;
        }

        public string Token { get; private set; }
        public string ExpiraEn { get; private set; }
    }
}
