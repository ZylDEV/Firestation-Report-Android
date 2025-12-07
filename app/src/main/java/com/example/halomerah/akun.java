package com.example.halomerah;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;



public class akun extends AppCompatActivity {

    private static final String TAG = "akunActivity";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_GALLERY = 2;

    // Misalnya imgProfil kamu di layout
    private ImageView imgProfil;


    private TextView txtNama, txtId, txtTelepon, txtEmail;
    private Button btnUtama, btnMomen, btnHapus, btnKeluar, btnSimpan;

    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private FirebaseAuth auth;
    private Uri imageUri;
    private FirebaseUser currentUser;

    private StorageReference storageReference;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_akun);

        // Inisialisasi komponen UI
        txtNama = findViewById(R.id.namaterisi);
        txtId = findViewById(R.id.idterisi);
        txtTelepon = findViewById(R.id.teleponterisi);
        txtEmail = findViewById(R.id.emailterisi);
        imgProfil = findViewById(R.id.profill);
        btnUtama = findViewById(R.id.btnutama);
        btnMomen = findViewById(R.id.btnmomen);
        btnHapus = findViewById(R.id.btnhapus);
        btnKeluar = findViewById(R.id.btnkeluar);
        btnSimpan = findViewById(R.id.btnsimpan);

        // Inisialisasi Firebase
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        auth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();



        // Mendapatkan user yang saat ini login
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            txtEmail.setText(user.getEmail());
            Log.d(TAG, "User ID: " + user.getUid());
            loadUserProfile(user.getUid());
        } else {
            Log.d(TAG, "No user currently logged in");
        }

        // Menambahkan onClickListener ke tombol utama
        btnUtama.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(akun.this, home.class);
                startActivity(intent);
            }
        });




        // Menambahkan onClickListener ke tombol momen
        btnMomen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(akun.this, kejadian.class);
                startActivity(intent);
            }
        });

        // Menambahkan onClickListener ke tombol hapus
        btnHapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hapusAkun();
            }
        });

        // Menambahkan onClickListener ke tombol keluar
        btnKeluar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tandaiAkunSebagaiTidakAktif();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(akun.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


        // Menambahkan onClickListener untuk memilih gambar profil
        imgProfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickerDialog();
            }
        });

        String photoUriString = getIntent().getStringExtra("photoUri");
        if (photoUriString != null) {
            Uri photoUri = Uri.parse(photoUriString);
            imgProfil.setImageURI(photoUri); // Tampilkan foto
        } else {
            Log.d("akun", "Tidak ada URI yang diterima");
        }

        // Menambahkan onClickListener ke tombol simpan
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpanData();
            }
        });
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }

    }
    private void showImagePickerDialog() {
        String[] options = {"Ambil Foto", "Pilih dari Galeri"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Gambar Profil");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    openCamera();  // Jika pilih kamera
                } else {
                    openGallery();  // Jika pilih galeri
                }
            }
        });
        builder.show();
    }

    private void openCamera() {
        Intent intent = new Intent(this, camera2.class);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Menangani gambar yang dipilih dari galeri
        if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();  // Dapatkan URI gambar dari galeri
            imgProfil.setImageURI(filePath);  // Set URI langsung ke ImageView
            unggahGambarProfil(filePath);     // Unggah gambar menggunakan URI
        }
        // Menangani gambar yang diambil dari kamera
        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            String photoUriString = data.getStringExtra("photoUri");  // Terima URI foto dari CameraActivity
            if (photoUriString != null) {
                Uri photoUri = Uri.parse(photoUriString);  // Parse string URI menjadi URI
                imgProfil.setImageURI(photoUri);  // Set URI langsung ke ImageView
                unggahGambarProfil(photoUri);     // Unggah gambar menggunakan URI
            }
        }
    }


    // Method untuk mengunggah gambar ke Firebase Storage
    private void unggahGambarProfil(Uri imageUri) {
        try {
            // Konversi URI menjadi Bitmap
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

            bitmap = rotateImageIfRequired(bitmap, imageUri);

            bitmap = cropToSquare(bitmap);

            // Konversi Bitmap menjadi byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            // Mendapatkan pengguna saat ini dari Firebase
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "User tidak ditemukan", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = currentUser.getUid();
            // Menentukan lokasi penyimpanan di Firebase Storage
            StorageReference imageRef = FirebaseStorage.getInstance()
                    .getReference("profile_images")
                    .child(userId + ".jpg");

            // Mengunggah gambar ke Firebase Storage
            UploadTask uploadTask = imageRef.putBytes(data);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Setelah berhasil mengunggah, ambil URL gambar
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();

                    // Simpan URL gambar ke Realtime Database
                    DatabaseReference userRef = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(userId);
                    userRef.child("profileImageUrl").setValue(imageUrl);

                    Toast.makeText(this, "Gambar berhasil diunggah", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Gagal mengunggah gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Terjadi kesalahan saat memuat gambar", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap rotateImageIfRequired(Bitmap bitmap, Uri imageUri) throws IOException {
        InputStream input = getContentResolver().openInputStream(imageUri);
        ExifInterface exif = new ExifInterface(input);
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        input.close();

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(bitmap, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(bitmap, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(bitmap, 270);
            default:
                return bitmap;
        }
    }

    private Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    // Method untuk memuat profil pengguna
    private void loadUserProfile(String userId) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String displayName = dataSnapshot.child("displayName").getValue(String.class);
                    txtNama.setText(displayName != null ? displayName : "No name found");
                    txtId.setText(userId);String email = dataSnapshot.child("email").getValue(String.class);
                    txtEmail.setText(email != null ? email : "No email found");
                    String telepon = dataSnapshot.child("telepon").getValue(String.class);
                    txtTelepon.setText(telepon != null ? telepon : "N/A");



                    String profilUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);
                    if (profilUrl != null) {
                        Glide.with(akun.this)
                                .load(profilUrl)
                                .into(imgProfil);                        // pastikan imgProfil adalah ImageView yang benar
                    }

                    Log.d(TAG, "Profile data successfully retrieved");
                } else {
                    Log.d(TAG, "Document does not exist");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error retrieving profile data", databaseError.toException());
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Izin kamera diberikan", Toast.LENGTH_SHORT).show();
                openCamera(); // Panggil openCamera lagi setelah izin diberikan
            } else {
                Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Bitmap cropToSquare(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = (height > width) ? width : height;
        int newHeight = (height > width) ? width : height;

        int cropW = (width - newWidth) / 2;
        int cropH = (height - newHeight) / 2;

        return Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);
    }

    // Method untuk menghapus akun
    private void hapusAkun() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Tandai akun sebagai dihapus di database
            usersRef.child(user.getUid()).child("status").setValue("deleted");

            user.delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Hapus informasi akun dari database
                            usersRef.child(user.getUid()).removeValue();
                            Toast.makeText(akun.this, "Akun berhasil dihapus", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(akun.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(akun.this, "Gagal menghapus akun", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Gagal menghapus akun", e);
                        }
                    });
        }
    }

    // Method untuk menandai akun sebagai tidak aktif
    private void tandaiAkunSebagaiTidakAktif() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Tandai akun sebagai tidak aktif di database
            usersRef.child(user.getUid()).child("status").setValue("inactive");
        }
    }

    // Method untuk menyimpan data profil
    private void simpanData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            String displayName = txtNama.getText().toString();
            String email = txtEmail.getText().toString();
            String telepon = txtTelepon.getText().toString();


            Map<String, Object> userUpdates = new HashMap<>();
            userUpdates.put("displayName", displayName);
            userUpdates.put("email", email);
            userUpdates.put("telepon", telepon);
            // Tandai status akun sebagai aktif
            userUpdates.put("status", "active");


            usersRef.child(userId).updateChildren(userUpdates)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(akun.this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Data berhasil disimpan: " + telepon);
                            // Memuat ulang profil setelah menyimpan data
                            loadUserProfile(userId);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(akun.this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Gagal menyimpan data", e);
                        }
                    });
        }
    }
}
