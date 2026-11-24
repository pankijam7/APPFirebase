package com.devst.appfirebase.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.devst.appfirebase.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// Pantalla de acceso que gestiona el registro e inicio de sesión con Firebase Authentication
public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private FirebaseAuth mAuth; // Herramienta principal de Firebase para autenticación

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializa la conexión con Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Persistencia de sesión: si el usuario ya inició sesión antes, entra directo a la app
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            irAMainActivity();
            return;
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> iniciarSesion());
        btnRegister.setOnClickListener(v -> registrarUsuario());
    }

    // Valida los datos antes de enviarlos a Firebase (campos vacíos, formato de correo y largo mínimo)
    private boolean validarCredenciales(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa correo y contraseña", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Ingresa un correo válido", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener mínimo 6 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Verifica el correo y contraseña contra las cuentas guardadas en Firebase Auth
    private void iniciarSesion() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validarCredenciales(email, password)) return;

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Bienvenido: " + email, Toast.LENGTH_SHORT).show();
                        irAMainActivity();
                    } else {
                        Toast.makeText(this, "Error al iniciar sesión: Verifica tus credenciales", Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Crea un usuario nuevo en Firebase Auth (encriptando la contraseña automáticamente)
    private void registrarUsuario() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validarCredenciales(email, password)) return;

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show();
                        irAMainActivity();
                    } else {
                        Toast.makeText(this, "Error al registrar: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Abre la pantalla principal y cierra el Login con finish() para no volver atrás con el botón del celular
    private void irAMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}