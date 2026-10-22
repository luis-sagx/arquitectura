using System.Text.Encodings.Web;
using System.Text.Unicode;
using ec.edu.monster.servicio;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Razor;
using Microsoft.Extensions.WebEncoders;

WebApplicationBuilder builder = WebApplication.CreateBuilder(args);

// Controladores MVC con validación antifalsificación (CSRF) en todos los POST.
builder.Services.AddControllersWithViews(opciones =>
    opciones.Filters.Add(new AutoValidateAntiforgeryTokenAttribute()));

// Tildes y "→" se escriben como texto UTF-8 en el HTML (siguen escapándose <, >, & y comillas).
builder.Services.Configure<WebEncoderOptions>(opciones =>
    opciones.TextEncoderSettings = new TextEncoderSettings(UnicodeRanges.All));

// Las vistas Razor viven en ec/edu/monster/vista, igual que los paquetes de los demás clientes.
builder.Services.Configure<RazorViewEngineOptions>(opciones =>
{
    opciones.ViewLocationFormats.Clear();
    opciones.ViewLocationFormats.Add("/ec/edu/monster/vista/{1}/{0}.cshtml");
    opciones.ViewLocationFormats.Add("/ec/edu/monster/vista/Compartido/{0}.cshtml");
});

// El token del servicio se guarda en la sesión HTTP (15 minutos, como la sesión del servidor).
builder.Services.AddDistributedMemoryCache();
builder.Services.AddSession(opciones =>
{
    opciones.IdleTimeout = TimeSpan.FromMinutes(15);
    opciones.Cookie.HttpOnly = true;
    opciones.Cookie.IsEssential = true;
    opciones.Cookie.SameSite = SameSiteMode.Strict;
});

// Un ClienteRest por aplicación: comparte un HttpClient, que es seguro entre hilos.
builder.Services.AddSingleton(proveedor =>
{
    string? endpoint = builder.Configuration["Conversor:EndpointRest"];
    return new ClienteRest(endpoint);
});

WebApplication app = builder.Build();

if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/error");
}

app.UseStaticFiles();
app.UseRouting();
app.UseSession();
app.MapControllerRoute(name: "default", pattern: "{controller=Inicio}/{action=Index}");

app.Run();
