package com.example.halomerah;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class login extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        EdgeToEdge.enable(this);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("users");

        // CEK apakah user sudah login
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Kalau user sudah login, langsung masuk ke home
            Intent intent = new Intent(login.this, home.class);
            startActivity(intent);
            finish(); // supaya login activity tidak bisa di-back
            return; // keluar dari onCreate biar ga lanjut render form login
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPasword);
        btnLogin = findViewById(R.id.btnLanjutkan);

        btnLogin.setOnClickListener(v -> {
            // Mendapatkan input dari pengguna
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Memeriksa apakah kedua field tidak kosong
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(login.this, "Harap masukkan alamat email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(login.this, "Harap masukkan password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lakukan proses login
            loginUser(email, password);
        });
    }


    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Login berhasil
                        Toast.makeText(login.this, "Login berhasil", Toast.LENGTH_SHORT).show();

                        // Set status pengguna ke Firebase Database
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            userRef.child(userId).child("status").setValue("active");

                            // Memuat ulang data pengguna dari Firebase Database
                            loadUserData(userId);
                        }

                        // Arahkan ke halaman berikutnya setelah login
                        Intent intent = new Intent(login.this, home.class);
                        startActivity(intent);
                        finish(); // Optional, untuk menutup activity saat ini agar tidak kembali lagi dengan tombol back
                    } else {
                        // Login gagal, tampilkan pesan kesalahan
                        Toast.makeText(login.this, "Login gagal: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadUserData(String userId) {
        userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Ambil data pengguna dari Firebase Database dan lakukan pembaruan di aplikasi
                    // Contoh: String displayName = dataSnapshot.child("displayName").getValue(String.class);
                    //         String email = dataSnapshot.child("email").getValue(String.class);
                    //         String status = dataSnapshot.child("status").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }
}
