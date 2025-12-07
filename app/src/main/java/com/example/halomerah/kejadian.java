package com.example.halomerah;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

public class kejadian extends AppCompatActivity {

    private LinearLayout linearLayout; // Layout untuk menampung ImageView dan TextView
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_kejadian);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inisialisasi database reference
        databaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        // Mendapatkan referensi ke LinearLayout
        linearLayout = findViewById(R.id.kejadian); // Pastikan ID ini sesuai dengan ID di layout

        // Mendapatkan referensi ke tombol-tombol
        Button btnUtama = findViewById(R.id.btnutama);
        Button btnMenu = findViewById(R.id.btnmenu);

        // Menambahkan pendengar acara untuk setiap tombol
        btnUtama.setOnClickListener(v -> {
            Intent intent = new Intent(kejadian.this, home.class);
            startActivity(intent);
        });


        btnMenu.setOnClickListener(v -> {
            Intent intent = new Intent(kejadian.this, akun.class);
            startActivity(intent);
        });

        // Mendapatkan data dari Firebase dan menampilkannya
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Kosongkan LinearLayout sebelum menambahkan item baru
                linearLayout.removeAllViews();

                // List untuk menyimpan data sementara
                ArrayList<DataSnapshot> snapshots = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshots.add(snapshot); // Simpan snapshot ke dalam list
                }

                // Balikkan urutan list untuk menampilkan yang terbaru di atas
                Collections.reverse(snapshots);

                for (DataSnapshot snapshot : snapshots) {
                    String photoURL = snapshot.child("photoURL").getValue(String.class);
                    String note = snapshot.child("note").getValue(String.class); // Mendapatkan keterangan

                    // Buat LinearLayout untuk setiap item
                    LinearLayout itemLayout = new LinearLayout(kejadian.this);
                    itemLayout.setOrientation(LinearLayout.VERTICAL);
                    itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));

                    // Menambahkan margin ke itemLayout
                    int marginInDp = 16; // Margin dalam dp
                    float scale = getResources().getDisplayMetrics().density;
                    int marginInPx = (int) (marginInDp * scale + 0.5f); // Mengkonversi dp ke px
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, marginInPx, 0, marginInPx); // Atur margin atas dan bawah
                    itemLayout.setLayoutParams(params);

                    // Memuat gambar di ImageView menggunakan Picasso
                    if (photoURL != null) {
                        ImageView imageView = new ImageView(kejadian.this);
                        imageView.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                600 // tinggi tetap 600dp
                        ));
                        imageView.setBackgroundColor(0xFFFFFFFF); // Atur latar belakang putih
                        Picasso.get().load(photoURL).resize(800, 600).centerCrop().into(imageView);

                        // Tambahkan ImageView ke itemLayout
                        itemLayout.addView(imageView);
                    }

                    // Tambahkan keterangan di TextView
                    if (note != null) {
                        TextView textView = new TextView(kejadian.this);
                        textView.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));
                        textView.setText(note);
                        textView.setTextColor(0xFF000000); // Warna teks hitam
                        textView.setBackgroundColor(0xFFFFFFFF); // Latar belakang putih
                        textView.setPadding(8, 8, 8, 8); // Tambahkan padding

                        // Tambahkan TextView ke itemLayout
                        itemLayout.addView(textView);

                        // Tambahkan View sebagai pembatas di bawah TextView
                        View divider = new View(kejadian.this);
                        divider.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                2 // Tinggi pembatas
                        ));
                        divider.setBackgroundColor(0xFF000000); // Warna pembatas hitam

                        // Tambahkan pembatas ke itemLayout
                        itemLayout.addView(divider);
                    }

                    // Tambahkan itemLayout ke LinearLayout utama
                    linearLayout.addView(itemLayout);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Tangani error
            }
        });
    }
}
