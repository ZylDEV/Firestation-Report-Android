package com.example.halomerah;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class home extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private Button btnMenu;
    private Button btnMomen;
    private Button btnTambahLokasi;
    private Button btnHapusLokasi;
    private TextView riwayatTextView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private ImageView penjelasan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Views
        btnMenu = findViewById(R.id.btnmenu);
        btnMomen = findViewById(R.id.btnmomen);
        btnTambahLokasi = findViewById(R.id.btntambahlokasi);
        btnHapusLokasi = findViewById(R.id.btnhapuslokasi);
        riwayatTextView = findViewById(R.id.riwayat);
        penjelasan = findViewById(R.id.penjelasan); // Add this line

        // Set Click Listeners

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(home.this, akun.class));
            }
        });

        btnMomen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(home.this, kejadian.class));
            }
        });

        btnTambahLokasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(home.this, maps.class));
            }
        });

        btnHapusLokasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hapusLaporanTerbaru();
            }
        });

        // Add OnClickListener for the ImageView
        penjelasan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGoogleMaps();
            }
        });

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize MapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Fetch and display data in TextView
        fetchAndDisplayRiwayat();
    }

    private void fetchAndDisplayRiwayat() {
        // Get reference to your Firebase database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("laporan");

        // Add a listener to read the data from Firebase Realtime Database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                StringBuilder laporanBuilder = new StringBuilder();
                // Iterate through each data entry
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Get jenis permasalahan, catatan, dan jenis
                    String userName = snapshot.child("Username").getValue(String.class);
                    String locationString = snapshot.child("Location").getValue(String.class);
                    String status = snapshot.child("Status").getValue(String.class);
                    String jenisPermasalahan = snapshot.child("Jenis Permasalahan").getValue(String.class);
                    String catatanPermasalahan = snapshot.child("Catatan Permasalahan").getValue(String.class);

                    Long timestamp = snapshot.child("timestamp").getValue(Long.class);
                    String waktuLaporan = "";
                    if (timestamp != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                        waktuLaporan = sdf.format(new Date(timestamp));
                    }

                    String laporan = "Username : " + userName + "\n" +
                            "Jenis Permasalahan : " + jenisPermasalahan + "\n" +
                            "Catatan : " + catatanPermasalahan + "\n" +
                            "Location : " + locationString + "\n" +
                            "Status : " + status + "\n" +
                            "Waktu Laporan : " + waktuLaporan + "\n\n";

                    laporanBuilder.insert(0, laporan);
                }

                // Set the updated text to the TextView
                riwayatTextView.setText(laporanBuilder.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    private void hapusLaporanTerbaru() {
        // Ambil email pengguna yang sedang login
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        if (userEmail != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("laporan");

            // Add a listener to read the data from Firebase Realtime Database
            databaseReference.orderByChild("Email").equalTo(userEmail).limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Jika ada laporan dari pengguna yang sedang login
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String laporanKey = snapshot.getKey();
                            if (laporanKey != null) {
                                // Update status to "laporan dibatalkan"
                                databaseReference.child(laporanKey).child("Status").setValue("Laporan dibatalkan");
                                Toast.makeText(getApplicationContext(), "Laporan dibatalkan", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        // Tidak ada laporan yang ditemukan dari pengguna ini
                        Toast.makeText(getApplicationContext(), "Tidak ada laporan untuk dibatalkan", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors
                    Toast.makeText(getApplicationContext(), "Gagal menghapus laporan", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "Pengguna tidak terautentikasi", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        enableMyLocation();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Lokasi Saat Ini"));
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                            } else {
                                requestLocationUpdates();
                            }
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // 10 detik
        locationRequest.setFastestInterval(5000); // 5 det
        //...
        // Request location updates
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Lokasi Saat Ini"));
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("lokasi");
                            databaseReference.push().setValue(location);
                            fusedLocationClient.removeLocationUpdates(this);
                        }
                    }
                }
            }, null);
        }
    }

    // Handle location permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            }
        }
    }

    // Fetch current location and move camera
    private void fetchCurrentLocationAndMoveCamera() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                moveCameraToLocation(currentLocation);
                            } else {
                                requestLocationUpdates();
                            }
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // Move camera to a specific location
    private void moveCameraToLocation(LatLng location) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
    }

    // Method to open Google Maps
    private void openGoogleMaps() {
        // URI untuk mencari pemadam kebakaran terdekat
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=fire+station+near+me");

        // Buat Intent untuk membuka Google Maps dengan URI di atas
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        // Periksa apakah ada aplikasi yang bisa menangani intent ini
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }
}
