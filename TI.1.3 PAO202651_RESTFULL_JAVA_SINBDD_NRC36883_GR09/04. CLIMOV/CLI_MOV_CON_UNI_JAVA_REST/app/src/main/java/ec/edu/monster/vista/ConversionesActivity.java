package ec.edu.monster.vista;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import ec.edu.monster.R;
import ec.edu.monster.controlador.ControladorConversiones;
import ec.edu.monster.modelo.OpcionCatalogo;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.servicio.ClienteRest;
import ec.edu.monster.servicio.EjecutorAndroid;
import java.util.ArrayList;
import java.util.List;

/** Pantalla de catálogo y conversión. */
public final class ConversionesActivity extends AppCompatActivity implements VistaConversiones {
    private static final String EXTRA_ENDPOINT = "ec.edu.monster.ENDPOINT";
    private static final String EXTRA_TOKEN = "ec.edu.monster.TOKEN";
    private static final String EXTRA_EXPIRA = "ec.edu.monster.EXPIRA";

    private Spinner categoria;
    private Spinner origen;
    private Spinner destino;
    private TextInputEditText valor;
    private MaterialButton convertir;
    private MaterialButton verCatalogo;
    private MaterialButton intercambiar;
    private MaterialButton cerrarSesion;
    private LinearProgressIndicator progreso;
    private TextView resultado;
    private TextView estado;
    private ControladorConversiones controlador;

    static Intent crearIntent(Context contexto, String endpoint, Sesion sesion) {
        return new Intent(contexto, ConversionesActivity.class)
                .putExtra(EXTRA_ENDPOINT, endpoint)
                .putExtra(EXTRA_TOKEN, sesion.token())
                .putExtra(EXTRA_EXPIRA, sesion.expiraEn());
    }

    @Override
    protected void onCreate(Bundle estadoGuardado) {
        EdgeToEdge.enable(this,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        super.onCreate(estadoGuardado);

        String endpoint = getIntent().getStringExtra(EXTRA_ENDPOINT);
        String token = getIntent().getStringExtra(EXTRA_TOKEN);
        if (endpoint == null || token == null || token.trim().isEmpty()) {
            volverAlAcceso("Inicia sesión para continuar.", true);
            return;
        }

        setContentView(R.layout.activity_conversiones);
        AjustesVentana.aplicarMargenes(findViewById(R.id.raiz), findViewById(R.id.cabecera));

        categoria = findViewById(R.id.categoria);
        origen = findViewById(R.id.origen);
        destino = findViewById(R.id.destino);
        valor = findViewById(R.id.valor);
        convertir = findViewById(R.id.convertir);
        verCatalogo = findViewById(R.id.verCatalogo);
        intercambiar = findViewById(R.id.intercambiar);
        cerrarSesion = findViewById(R.id.cerrarSesion);
        progreso = findViewById(R.id.progreso);
        resultado = findViewById(R.id.resultado);
        estado = findViewById(R.id.estado);

        controlador = new ControladorConversiones(this, new ClienteRest(endpoint),
                new Sesion(token, getIntent().getStringExtra(EXTRA_EXPIRA)), new EjecutorAndroid());

        categoria.setOnItemSelectedListener(new SeleccionSimple() {
            @Override
            public void onItemSelected(AdapterView<?> padre, View vista, int posicion, long id) {
                controlador.cambiarCategoria((OpcionCatalogo) padre.getItemAtPosition(posicion));
            }
        });
        SeleccionSimple limpiar = new SeleccionSimple() {
            @Override
            public void onItemSelected(AdapterView<?> padre, View vista, int posicion, long id) {
                limpiarResultado();
            }
        };
        origen.setOnItemSelectedListener(limpiar);
        destino.setOnItemSelectedListener(limpiar);

        convertir.setOnClickListener(v -> enviarConversion());
        verCatalogo.setOnClickListener(v -> controlador.consultarCatalogo());
        valor.setOnEditorActionListener((v, accion, evento) -> {
            if (accion == EditorInfo.IME_ACTION_DONE) {
                enviarConversion();
                return true;
            }
            return false;
        });
        intercambiar.setOnClickListener(v -> {
            int posicionOrigen = origen.getSelectedItemPosition();
            origen.setSelection(destino.getSelectedItemPosition());
            destino.setSelection(posicionOrigen);
        });
        cerrarSesion.setOnClickListener(v -> controlador.cerrarSesion());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                controlador.cerrarSesion();
            }
        });

        controlador.iniciar();
    }

    @Override
    protected void onDestroy() {
        if (controlador != null) {
            controlador.liberar();
        }
        super.onDestroy();
    }

    @Override
    public void setOcupado(boolean ocupado) {
        progreso.setVisibility(ocupado ? View.VISIBLE : View.GONE);
        categoria.setEnabled(!ocupado);
        origen.setEnabled(!ocupado);
        destino.setEnabled(!ocupado);
        convertir.setEnabled(!ocupado);
        verCatalogo.setEnabled(!ocupado);
        intercambiar.setEnabled(!ocupado);
    }

    @Override
    public void mostrarEstado(String mensaje) {
        estado.setText(mensaje);
    }

    @Override
    public void mostrarAviso(String titulo, String mensaje) {
        if (isFinishing()) return;
        new MaterialAlertDialogBuilder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton(R.string.aceptar, null)
                .show();
    }

    @Override
        public void mostrarCatalogo(String mensaje) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        android.widget.LinearLayout raiz = new android.widget.LinearLayout(this);
        raiz.setOrientation(android.widget.LinearLayout.VERTICAL);
        raiz.setBackgroundColor(Color.rgb(241, 244, 248));
        raiz.setPadding(20, 24, 20, 16);
        android.widget.LinearLayout encabezado = new android.widget.LinearLayout(this);
        encabezado.setGravity(android.view.Gravity.CENTER_VERTICAL);
        encabezado.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        TextView titulo = new TextView(this);
        titulo.setText("Catálogo de unidades");
        titulo.setTextSize(22);
        titulo.setTypeface(null, android.graphics.Typeface.BOLD);
        titulo.setTextColor(Color.rgb(24, 50, 87));
        encabezado.addView(titulo, new android.widget.LinearLayout.LayoutParams(0, -2, 1));
        MaterialButton volver = new MaterialButton(this);
        volver.setText("Volver");
        volver.setOnClickListener(v -> dialog.dismiss());
        encabezado.addView(volver);
        raiz.addView(encabezado);
        android.widget.ScrollView scroll = new android.widget.ScrollView(this);
        android.widget.GridLayout grid = new android.widget.GridLayout(this);
        grid.setColumnCount(2);
        grid.setUseDefaultMargins(false);
        grid.setPadding(0, 18, 0, 8);
        android.widget.LinearLayout lista = null;
        int indice = 0;
        for (String linea : mensaje.split("\\R")) {
            String limpio = linea.trim();
            if (limpio.isEmpty() || limpio.startsWith("Conversiones disponibles")) continue;
            if (!linea.startsWith(" ") && !linea.startsWith("\t")) {
                com.google.android.material.card.MaterialCardView card = new com.google.android.material.card.MaterialCardView(this);
                card.setCardBackgroundColor(Color.WHITE);
                card.setRadius(20);
                card.setStrokeColor(Color.rgb(211, 219, 229));
                card.setStrokeWidth(1);
                card.setContentPadding(20, 16, 20, 16);
                lista = new android.widget.LinearLayout(this);
                lista.setOrientation(android.widget.LinearLayout.VERTICAL);
                TextView categoria = new TextView(this);
                categoria.setText(limpio);
                categoria.setTextSize(16);
                categoria.setTypeface(null, android.graphics.Typeface.BOLD);
                categoria.setTextColor(Color.rgb(24, 50, 87));
                lista.addView(categoria);
                card.addView(lista);
                android.widget.GridLayout.LayoutParams parametros = new android.widget.GridLayout.LayoutParams(android.widget.GridLayout.spec(indice / 2), android.widget.GridLayout.spec(indice % 2, 1, 1f));
                parametros.width = 0;
                parametros.setMargins(0, 0, 10, 14);
                grid.addView(card, parametros);
                indice++;
            } else if (lista != null) {
                TextView unidad = new TextView(this);
                unidad.setText(limpio.replaceFirst("^[•*-]\\s*", ""));
                unidad.setTextSize(14);
                unidad.setTextColor(Color.rgb(55, 67, 82));
                unidad.setPadding(0, 8, 0, 0);
                lista.addView(unidad);
            }
        }
        scroll.addView(grid);
        raiz.addView(scroll, new android.widget.LinearLayout.LayoutParams(-1, 0, 1));
        dialog.setContentView(raiz);
        dialog.show();
        if (dialog.getWindow() != null) dialog.getWindow().setLayout(-1, -1);
    }

    @Override
    public void setCategorias(List<OpcionCatalogo> categorias) {
        categoria.setAdapter(adaptador(categorias));
    }

    @Override
    public void setUnidades(List<OpcionCatalogo> unidades) {
        origen.setAdapter(adaptador(unidades));
        destino.setAdapter(adaptador(unidades));
        if (unidades.size() > 1) {
            destino.setSelection(1);
        }
    }

    @Override
    public void limpiarUnidades() {
        origen.setAdapter(adaptador(new ArrayList<>()));
        destino.setAdapter(adaptador(new ArrayList<>()));
    }

    @Override
    public OpcionCatalogo categoriaSeleccionada() {
        return (OpcionCatalogo) categoria.getSelectedItem();
    }

    @Override
    public OpcionCatalogo origenSeleccionado() {
        return (OpcionCatalogo) origen.getSelectedItem();
    }

    @Override
    public OpcionCatalogo destinoSeleccionado() {
        return (OpcionCatalogo) destino.getSelectedItem();
    }

    @Override
    public String valorIngresado() {
        return valor.getText() == null ? "" : valor.getText().toString();
    }

    @Override
    public void mostrarResultado(String texto) {
        resultado.setText(texto);
    }

    @Override
    public void limpiarResultado() {
        resultado.setText(R.string.sin_resultado);
    }

    @Override
    public void volverAlAcceso(String mensaje, boolean error) {
        Intent acceso = new Intent(this, LoginActivity.class)
                .putExtra(LoginActivity.EXTRA_MENSAJE, mensaje)
                .putExtra(LoginActivity.EXTRA_ERROR, error)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(acceso);
        finish();
    }

    private void enviarConversion() {
        InputMethodManager teclado = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (teclado != null) {
            teclado.hideSoftInputFromWindow(valor.getWindowToken(), 0);
        }
        controlador.convertir();
    }

    private ArrayAdapter<OpcionCatalogo> adaptador(List<OpcionCatalogo> opciones) {
        ArrayAdapter<OpcionCatalogo> adaptador = new ArrayAdapter<>(this, R.layout.item_selector, opciones);
        adaptador.setDropDownViewResource(R.layout.item_selector_desplegable);
        return adaptador;
    }

    private abstract static class SeleccionSimple implements AdapterView.OnItemSelectedListener {
        @Override
        public void onNothingSelected(AdapterView<?> padre) { }
    }
}
