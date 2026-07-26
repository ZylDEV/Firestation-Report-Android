# Firestation Report Android App

Aplikasi Android untuk pelaporan dan penanganan insiden kebakaran. Dirancang untuk membantu petugas pemadam kebakaran dalam mendokumentasikan dan merespon laporan secara cepat dan efisien.

---

## Fitur

- Autentikasi pengguna (Login dan Registrasi)
- Pelaporan insiden kebakaran
- Manajemen data insiden
- Riwayat laporan
- Antarmuka yang sederhana dan mudah digunakan

---

## Tech Stack

- Android (Java)
- Gradle
- Firebase Authentication
- Firebase Realtime Database
- Google Maps API
- Google Sign-In

---

## Persyaratan

- Android Studio
- Java Development Kit (JDK)
- Koneksi internet (Firebase)

---

## Instalasi

1. Clone repositori ini:

   ```bash
   git clone https://github.com/ZylDEV/Firestation-Report-Android.git
   ```

2. Buka project di Android Studio.

3. Siapkan file konfigurasi Firebase:
   - Download file `google-services.json` dari Firebase Console
   - Letakkan di folder `app/`

4. Atur API Key di `strings.xml`:
   - `YOUR_GOOGLE_MAPS_API_KEY` -> Ganti dengan Google Maps API Key-mu
   - `YOUR_WEB_CLIENT_ID` -> Ganti dengan Web Client ID dari Google Cloud Console
   - `YOUR_FIREBASE_DATABASE_URL` -> Ganti dengan URL Firebase Realtime Database-mu

5. Jalankan aplikasi melalui Android Studio.

---

## Lisensi

Distributed under the MIT License. Lihat file `LICENSE` untuk informasi lebih lanjut.
