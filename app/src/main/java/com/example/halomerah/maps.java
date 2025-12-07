package com.example.halomerah;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class maps extends AppCompatActivity implements OnMapReadyCallback {

    private DatabaseReference databaseReference;
    private GoogleMap mMap;
    private Marker selectedMarker;
    private Button btnConfirmLocation;
    private Button uploadfoto;
    private ImageView imagePreview;
    private static final float DEFAULT_ZOOM = 12f; // Nilai zoom default
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;


    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Uri selectedImageUri;
    private StorageReference storageReference;
    private String fotoUrl = null;
    private Spinner spinnerPilih;
    private AutoCompleteTextView titikKoordinat;
    private EditText editTextCatatan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("laporan");

        btnConfirmLocation = findViewById(R.id.btn_confirm_location);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        spinnerPilih = findViewById(R.id.pilih);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.pilihan_array, android.R.layout.simple_spinner_item);

// Menetapkan warna teks di dropdown
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Menambahkan adapter ke spinner
        spinnerPilih.setAdapter(adapter);

        // Menetapkan warna teks yang dipilih menjadi hitam
        TextView selectedItem = (TextView) spinnerPilih.getSelectedView();
        if (selectedItem != null) {
            selectedItem.setTextColor(Color.BLACK); // Ubah warna teks item yang dipilih
        }


        titikKoordinat = findViewById(R.id.titikkordinat);
        titikKoordinat.setFocusable(false); // Membuat AutoCompleteTextView tidak bisa diklik

        storageReference = FirebaseStorage.getInstance().getReference();



        editTextCatatan = findViewById(R.id.catatan);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        btnConfirmLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedMarker != null) {
                    LatLng selectedLocation = selectedMarker.getPosition();
                    String coordinates = "Latitude: " + selectedLocation.latitude + ", Longitude: " + selectedLocation.longitude;
                    titikKoordinat.setText(coordinates);

                    // Ambil data dari Spinner, AutoCompleteTextView, dan EditText
                    String jenisPermasalahan = spinnerPilih.getSelectedItem().toString();
                    String catatanPermasalahan = editTextCatatan.getText().toString();

                    long timestamp = System.currentTimeMillis();

                    // Simpan data ke Firebase Realtime Database
                    saveToFirebaseDatabase(selectedLocation, jenisPermasalahan, catatanPermasalahan);
                }
            }
        });
        uploadfoto = findViewById(R.id.uploadfoto);  // Mengambil referensi dari layout
        imagePreview = findViewById(R.id.imagePreview);

        uploadfoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence[] options = {"Ambil Foto", "Pilih dari Galeri"};
                AlertDialog.Builder builder = new AlertDialog.Builder(maps.this);
                builder.setTitle("Pilih Sumber Gambar");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // Ambil Foto langsung ke activity kamera
                            Intent cameraIntent = new Intent(maps.this, camera.class);
                            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                        } else if (which == 1) {
                            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(galleryIntent, REQUEST_IMAGE_GALLERY);
                        }
                    }
                });

                builder.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Ambil URI dari camera activity
                String photoUriString = data.getStringExtra("photoUri");
                selectedImageUri = Uri.parse(photoUriString);
                imagePreview.setImageURI(selectedImageUri);  // Tampilkan preview saja
                fotoUrl = selectedImageUri.toString();

            } else if (requestCode == REQUEST_IMAGE_GALLERY) {
                // Ambil URI dari galeri
                selectedImageUri = data.getData();
                imagePreview.setImageURI(selectedImageUri);  // Tampilkan preview saja
                fotoUrl = selectedImageUri.toString();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.getUiSettings().setZoomControlsEnabled(true); // Enable zoom controls

        // Cek izin lokasi
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates(); // Mulai pembaruan lokasi langsung
        }

        // Ketika peta di-klik, tambahkan marker di lokasi yang diklik
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (selectedMarker != null) {
                    selectedMarker.remove();
                }
                selectedMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Tandai Lokasi"));
                updateTitikKoordinat(selectedMarker.getPosition());
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng position = marker.getPosition();
                updateTitikKoordinat(position);
                return true;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) { // Kamera
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Izin Kamera diberikan", Toast.LENGTH_SHORT).show();
                // Di sini kamu bisa langsung jalankan intent kamera juga
            } else {
                Toast.makeText(this, "Izin Kamera ditolak", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) { // Lokasi
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Izin Lokasi ditolak", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000); // Update lokasi setiap 5 detik

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (selectedMarker != null) {
                        selectedMarker.remove();
                    }
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    selectedMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("Lokasi Terkini"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM)); // Zoom in to current location
                    updateTitikKoordinat(currentLocation);
                    stopLocationUpdates(); // Matikan pembaruan lokasi setelah mendapatkan satu lokasi
                    break;
                }
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void updateTitikKoordinat(LatLng latLng) {
        String coordinates = "Latitude: " + latLng.latitude + ", Longitude: " + latLng.longitude;
        titikKoordinat.setText(coordinates);
    }

    private void saveToFirebaseDatabase(LatLng location, String jenisPermasalahan, String catatanPermasalahan) {
        if (selectedImageUri == null) {
            Toast.makeText(maps.this, "Foto belum tersedia", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(maps.this);
        progressDialog.setMessage("Mengirim laporan...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StorageReference fotoRef = storageReference.child("laporan_foto/" + System.currentTimeMillis() + ".jpg");
        fotoRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fotoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("laporan");
                        String laporanId = databaseReference.push().getKey();

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user == null) {
                            progressDialog.dismiss();
                            Toast.makeText(maps.this, "Pengguna tidak ditemukan", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String userID = user.getUid();
                        String userEmail = user.getEmail();
                        String userName = user.getDisplayName();

                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userID);
                        userRef.child("telepon").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String userTelepon = snapshot.getValue(String.class);
                                if (userTelepon == null) {
                                    userTelepon = "Nomor telepon tidak tersedia";
                                }

                                Map<String, Object> laporan = new HashMap<>();
                                laporan.put("Location", location.latitude + ", " + location.longitude);
                                laporan.put("Jenis Permasalahan", jenisPermasalahan);
                                laporan.put("Catatan Permasalahan", catatanPermasalahan);
                                laporan.put("Status", "Laporan terbaru");
                                laporan.put("Email", userEmail);
                                laporan.put("Username", userName);
                                laporan.put("Telepon", userTelepon);
                                laporan.put("fotoK", downloadUrl);
                                laporan.put("timestamp", System.currentTimeMillis());

                                if (laporanId != null) {
                                    databaseReference.child(laporanId).setValue(laporan)
                                            .addOnSuccessListener(aVoid -> {
                                                progressDialog.dismiss();
                                                Toast.makeText(maps.this, "Data berhasil dikirim", Toast.LENGTH_SHORT).show();
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                progressDialog.dismiss();
                                                Toast.makeText(maps.this, "Gagal mengirim data", Toast.LENGTH_SHORT).show();
                                            });
                                } else {
                                    progressDialog.dismiss();
                                    Toast.makeText(maps.this, "ID laporan tidak valid", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                progressDialog.dismiss();
                                Toast.makeText(maps.this, "Gagal mengambil data pengguna", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(maps.this, "Gagal mendapatkan URL foto", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(maps.this, "Gagal upload foto", Toast.LENGTH_SHORT).show();
                });
    }

}
