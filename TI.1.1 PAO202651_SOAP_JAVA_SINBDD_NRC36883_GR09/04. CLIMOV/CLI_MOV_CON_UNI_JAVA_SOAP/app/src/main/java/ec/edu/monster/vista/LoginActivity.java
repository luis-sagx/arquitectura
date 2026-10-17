package ec.edu.monster.vista;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import ec.edu.monster.R;
import ec.edu.monster.controlador.ControladorLogin;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.servicio.EjecutorAndroid;

/** Pantalla de acceso y recuperación de clave. */
public final class LoginActivity extends AppCompatActivity implements VistaLogin {
    static final String EXTRA_MENSAJE = "ec.edu.monster.MENSAJE";
    static final String EXTRA_ERROR = "ec.edu.monster.ERROR";

    private TextInputEditText usuario;
    private TextInputEditText clave;
    private MaterialButton ingresar;
    private MaterialButton recuperar;
    private LinearProgressIndicator progreso;
    private TextView mensaje;
    private ControladorLogin controlador;

    @Override
    protected void onCreate(Bundle estadoGuardado) {
        EdgeToEdge.enable(this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        super.onCreate(estadoGuardado);
        setContentView(R.layout.activity_login);
        AjustesVentana.aplicarMargenes(findViewById(R.id.raiz), null);

        usuario = findViewById(R.id.usuario);
        clave = findViewById(R.id.clave);
        ingresar = findViewById(R.id.ingresar);
        recuperar = findViewById(R.id.recuperar);
        progreso = findViewById(R.id.progreso);
        mensaje = findViewById(R.id.mensaje);

        controlador = new ControladorLogin(this, new EjecutorAndroid(), getString(R.string.endpoint_soap));
        ingresar.setOnClickListener(v -> controlador.ingresar());
        recuperar.setOnClickListener(v -> controlador.solicitarRecuperacion());
        clave.setOnEditorActionListener((v, accion, evento) -> {
            if (accion == EditorInfo.IME_ACTION_DONE) {
                controlador.ingresar();
                return true;
            }
            return false;
        });

        String aviso = getIntent().getStringExtra(EXTRA_MENSAJE);
        if (aviso != null && estadoGuardado == null) {
            mostrarMensaje(aviso, getIntent().getBooleanExtra(EXTRA_ERROR, false));
        }
    }

    @Override
    protected void onDestroy() {
        controlador.liberar();
        super.onDestroy();
    }

    @Override
    public String usuarioIngresado() {
        return texto(usuario);
    }

    @Override
    public String claveIngresada() {
        return texto(clave);
    }

    @Override
    public void limpiarClave() {
        clave.setText("");
    }

    @Override
    public void setOcupado(boolean ocupado) {
        progreso.setVisibility(ocupado ? View.VISIBLE : View.GONE);
        ingresar.setEnabled(!ocupado);
        recuperar.setEnabled(!ocupado);
        usuario.setEnabled(!ocupado);
        clave.setEnabled(!ocupado);
    }

    @Override
    public void mostrarMensaje(String texto, boolean error) {
        mensaje.setText(texto);
        mensaje.setTextColor(ContextCompat.getColor(this, error ? R.color.error : R.color.gris));
    }

    @Override
    public void pedirNuevaClave(String nombreUsuario) {
        View contenido = getLayoutInflater().inflate(R.layout.dialogo_recuperar, null);
        TextInputEditText nueva = contenido.findViewById(R.id.nuevaClave);
        TextInputEditText confirmacion = contenido.findViewById(R.id.confirmarClave);
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.recuperar_titulo)
                .setMessage(getString(R.string.recuperar_mensaje, nombreUsuario))
                .setView(contenido)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.actualizar,
                        (dialogo, boton) -> controlador.recuperarClave(texto(nueva), texto(confirmacion)))
                .show();
    }

    @Override
    public void abrirConversiones(String endpoint, Sesion sesion) {
        startActivity(ConversionesActivity.crearIntent(this, endpoint, sesion));
        finish();
    }

    private static String texto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString();
    }
}
