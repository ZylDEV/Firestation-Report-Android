package com.example.halomerah;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class loginnomor extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnSimpan;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginnomor);

        // Inisialisasi FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        // Inisialisasi DatabaseReference untuk simpul "users" di Firebase Database dengan URL khusus
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPasword);
        btnSimpan = findViewById(R.id.btnSimpan);

        // Menambahkan OnClickListener untuk btnSimpan
        btnSimpan.setOnClickListener(v -> {
            // Mendapatkan input dari pengguna
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Memeriksa apakah kedua field tidak kosong
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(loginnomor.this, "Harap masukkan alamat email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(loginnomor.this, "Harap masukkan password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Mendaftarkan pengguna dengan email dan password
            registerWithEmailAndPassword(email, password);
        });
    }

    private void registerWithEmailAndPassword(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String[] emailParts = email.split("@");
                            String displayName = emailParts[0];

                            // Update displayName ke FirebaseAuth user
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(displayName)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            // Setelah displayName berhasil di-set, simpan data ke database
                                            saveUserDataToDatabase(email, password);
                                            Toast.makeText(loginnomor.this, "Registrasi berhasil", Toast.LENGTH_SHORT).show();
                                            onBackPressed();
                                        } else {
                                            Toast.makeText(loginnomor.this, "Gagal menyimpan displayName", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(loginnomor.this, "Registrasi gagal: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void saveUserDataToDatabase(String email, String password) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            if (!TextUtils.isEmpty(userId)) {
                String displayName = user.getDisplayName();

                Map<String, Object> userData = new HashMap<>();
                userData.put("email", email);
                userData.put("displayName", displayName);
                userData.put("password", password);

                usersRef.child(userId).setValue(userData)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(loginnomor.this, "Data pengguna berhasil disimpan", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(loginnomor.this, "Gagal menyimpan data pengguna", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(loginnomor.this, "UserID kosong", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(loginnomor.this, "User kosong", Toast.LENGTH_SHORT).show();
        }
    }

}
