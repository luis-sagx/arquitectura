var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();
builder.Services.AddOpenApi();

string usuario = builder.Configuration["conversor.usuario"] ?? "Monster";
string clave = builder.Configuration["conversor.clave"] ?? "Monster9";
string minutosConfigurados = builder.Configuration["conversor.sesion.minutos"] ?? "15";
if (!long.TryParse(minutosConfigurados, System.Globalization.NumberStyles.Integer, System.Globalization.CultureInfo.InvariantCulture, out long minutos) || minutos <= 0)
    throw new InvalidOperationException("La duracion de la sesion debe ser un entero positivo");
builder.Services.AddSingleton(new ec.edu.monster.servicios.ConversorUnidadesServices(usuario, clave, TimeSpan.FromMinutes(minutos)));

var app = builder.Build();

if (app.Environment.IsDevelopment())
    app.MapOpenApi();

app.UseHttpsRedirection();
app.UseAuthorization();
app.MapControllers();
app.Run();
