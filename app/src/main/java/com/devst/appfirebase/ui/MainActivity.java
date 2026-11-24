package com.devst.appfirebase.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.devst.appfirebase.R;
import com.devst.appfirebase.model.Videojuego;
import com.devst.appfirebase.viewmodel.MainViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

// Pantalla principal del CRUD de videojuegos (solo accesible si hay un usuario autenticado)
public class MainActivity extends AppCompatActivity {

    private EditText etId, etTitulo, etPlataforma, etGenero;
    private Button btnSave, btnSearch, btnUpdate, btnDelete, btnLogout;
    private TextView tvUsuarioActual, tvListaJuegos;

    private MainViewModel viewModel;
    private FirebaseAuth mAuth;
    private String uidActual; // UID del usuario conectado para filtrar y proteger sus registros

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Protección de acceso: si nadie ha iniciado sesión, redirige inmediatamente al Login
        if (currentUser == null) {
            irALogin();
            return;
        }

        // Obtiene el identificador único (UID) del usuario autenticado
        uidActual = currentUser.getUid();

        tvUsuarioActual = findViewById(R.id.tvUsuarioActual);
        tvListaJuegos = findViewById(R.id.tvListaJuegos);
        etId = findViewById(R.id.etId);
        etTitulo = findViewById(R.id.etTitulo);
        etPlataforma = findViewById(R.id.etPlataforma);
        etGenero = findViewById(R.id.etGenero);

        btnSave = findViewById(R.id.btnSave);
        btnSearch = findViewById(R.id.btnSearch);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        btnLogout = findViewById(R.id.btnLogout);

        // Muestra en pantalla el correo del usuario conectado
        tvUsuarioActual.setText("Conectado: " + currentUser.getEmail());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Observador 1: Muestra alertas (Toast) con el resultado de cada operación CRUD
        viewModel.getMensaje().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        // Observador 2: Cuando se busca un juego, rellena automáticamente el formulario
        viewModel.getJuegoEncontrado().observe(this, juego -> {
            if (juego != null) {
                etId.setText(juego.getId());
                etTitulo.setText(juego.getTitulo());
                etPlataforma.setText(juego.getPlataforma());
                etGenero.setText(juego.getGenero());
            }
        });

        // Observador 3: Actualiza el listado inferior cada vez que cambian los registros en Firestore
        viewModel.getListaJuegos().observe(this, this::mostrarListaEnPantalla);

        btnSave.setOnClickListener(v -> guardar());
        btnSearch.setOnClickListener(v -> buscar());
        btnUpdate.setOnClickListener(v -> actualizar());
        btnDelete.setOnClickListener(v -> eliminar());

        // Cierra la sesión en Firebase Auth y devuelve al usuario a la pantalla de Login
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            irALogin();
        });

        // Carga los videojuegos del usuario apenas se abre la pantalla
        viewModel.cargarMisJuegos(uidActual);
    }

    // Valida los 4 campos obligatorios antes de guardar en Firestore
    private void guardar() {
        String id = etId.getText().toString().trim();
        String titulo = etTitulo.getText().toString().trim();
        String plataforma = etPlataforma.getText().toString().trim();
        String genero = etGenero.getText().toString().trim();

        if (id.isEmpty() || titulo.isEmpty() || plataforma.isEmpty() || genero.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.guardarJuego(id, titulo, plataforma, genero, uidActual);
        limpiarCampos();
    }

    // Valida que se haya ingresado un ID para consultar en la base de datos
    private void buscar() {
        String id = etId.getText().toString().trim();
        if (id.isEmpty()) {
            Toast.makeText(this, "Ingresa el ID del juego a buscar", Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.buscarJuego(id, uidActual);
    }

    // Valida los campos y actualiza el registro existente en Firestore
    private void actualizar() {
        String id = etId.getText().toString().trim();
        String titulo = etTitulo.getText().toString().trim();
        String plataforma = etPlataforma.getText().toString().trim();
        String genero = etGenero.getText().toString().trim();

        if (id.isEmpty() || titulo.isEmpty() || plataforma.isEmpty() || genero.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos para actualizar", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.actualizarJuego(id, titulo, plataforma, genero, uidActual);
        limpiarCampos();
    }

    // Valida el ID y solicita la eliminación del documento en Firestore
    private void eliminar() {
        String id = etId.getText().toString().trim();
        if (id.isEmpty()) {
            Toast.makeText(this, "Ingresa el ID del juego a eliminar", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.eliminarJuego(id, uidActual);
        limpiarCampos();
    }

    // Construye el texto ordenado con los registros obtenidos desde Cloud Firestore
    private void mostrarListaEnPantalla(List<Videojuego> lista) {
        if (lista == null || lista.isEmpty()) {
            tvListaJuegos.setText("No tienes videojuegos registrados aún.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        int contador = 1;
        for (Videojuego v : lista) {
            sb.append(contador++).append(". [ID: ").append(v.getId()).append("]\n")
                    .append("   • Título: ").append(v.getTitulo()).append("\n")
                    .append("   • Plataforma: ").append(v.getPlataforma()).append("\n")
                    .append("   • Género: ").append(v.getGenero()).append("\n\n");
        }
        tvListaJuegos.setText(sb.toString());
    }

    private void limpiarCampos() {
        etId.setText("");
        etTitulo.setText("");
        etPlataforma.setText("");
        etGenero.setText("");
    }

    private void irALogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}