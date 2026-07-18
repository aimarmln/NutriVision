# NutriVision - Android Application

**NutriVision** adalah aplikasi mobile berbasis Android yang berfokus pada pengenalan makanan (Food Recognition), estimasi kalori, serta pelacakan gizi (Calorie Tracking). Aplikasi ini membantu pengguna untuk memantau asupan makanan mereka sehari-hari dengan lebih mudah dan akurat melalui kamera ponsel.

## Fitur Utama

- **Otentikasi Pengguna**: Login dan register pengguna secara aman menggunakan Token Auth.
- **Pengenalan Makanan**: Mengambil gambar makanan menggunakan kamera (CameraX) dan mendeteksi makanan tersebut.
- **Pelacakan Kalori & Gizi**: Mencatat asupan kalori harian dan histori log makanan.
- **Resep Makanan**: Menelusuri berbagai resep makanan sehat.
- **Fitur Chat**: Berkomunikasi atau berkonsultasi dalam aplikasi.
- **Komentar & Interaksi**: Memberikan komentar pada resep atau post terkait.

---

## Struktur Proyek (Project Structure)

Proyek ini dibangun menggunakan arsitektur **MVVM (Model-View-ViewModel)** dengan pemisahan *concern* yang jelas (Clean Architecture concept):

```
com.example.nutrivision/
├── data/                  # Layer Data (Single Source of Truth)
│   ├── local/             # Penyimpanan lokal (DataStore Preferences, TokenManager)
│   ├── remote/            # Sumber data jaringan (Retrofit API Services, Interceptors, Config)
│   └── repository/        # Repository pattern untuk abstraksi sumber data
├── di/                    # Dependency Injection (Modul Hilt seperti AppModule)
├── ui/                    # Layer Presentasi (Activity, Fragment, ViewModel, Adapter)
├── utils/                 # Kelas helper dan fungsi utilitas pendukung
└── views/                 # Custom Views jika diperlukan
```

---

## Library & Teknologi yang Digunakan

Aplikasi NutriVision memanfaatkan teknologi dan pustaka modern dari ekosistem Android (Jetpack):

- **Kotlin**: Bahasa pemrograman utama.
- **Coroutines**: Untuk manajemen *asynchronous programming* dan threading.
- **Dagger Hilt**: Dependency Injection untuk memudahkan manajemen dependencies.
- **Retrofit2 & OkHttp3**: HTTP Client untuk komunikasi API dengan Backend (lengkap dengan *Logging* dan *Auth Interceptor*).
- **Navigation Component**: Manajemen rute antar layar (Fragment-based navigation).
- **CameraX**: Pustaka kamera dari Android Jetpack untuk integrasi kamera yang mudah dan konsisten.
- **DataStore Preferences**: Penyimpanan data lokal yang modern dan asinkron (pengganti SharedPreferences).
- **Markwon**: Pustaka untuk me-render format Markdown di dalam TextView (digunakan untuk menampilkan artikel atau deskripsi yang berformat).
- **Facebook Shimmer**: Efek *loading skeleton* yang menarik pada saat memuat data.
- **Material Design**: Komponen antarmuka standar dari Google untuk Android.

---

## Cara Konfigurasi dan Start Backend (API)

Aplikasi NutriVision membutuhkan backend (server) agar dapat berfungsi sepenuhnya. Berikut adalah panduan menghubungkan aplikasi ke backend lokal Anda:

### 1. Menjalankan Server Backend
Pastikan Anda sudah menjalankan repositori backend NutriVision di komputer atau server Anda (Misalnya menggunakan Node.js, FastAPI, atau framework lainnya).
- Secara *default*, backend berjalan di port `8000`.

### 2. Konfigurasi `BASE_URL` di Android
Secara bawaan, aplikasi Android ini diatur untuk menunjuk ke IP lokal komputer (misalnya `http://10.21.187.109:8000`). Anda **wajib** mengubah IP ini sesuai dengan IP Localhost (IPv4) dari jaringan WiFi komputer Anda.

1. Buka file `ApiConfig.kt` yang berada di direktori:  
   `app/src/main/java/com/example/nutrivision/data/remote/network/ApiConfig.kt`
2. Cari variabel `BASE_URL`:
   ```kotlin
   private const val BASE_URL = "http://<IP_KOMPUTER_ANDA>:8000"
   ```
3. Ubah `<IP_KOMPUTER_ANDA>` dengan alamat IPv4 mesin tempat backend dijalankan (contoh: `192.168.1.5`).
   - *Catatan: Jangan gunakan `localhost` atau `127.0.0.1` jika Anda menjalankan aplikasi di emulator atau perangkat fisik (Device), karena IP tersebut akan menunjuk ke dalam mesin device/emulator itu sendiri, bukan ke komputer Anda.*

### 3. Build & Run Aplikasi
1. Buka proyek ini menggunakan **Android Studio**.
2. Tunggu proses **Gradle Sync** hingga selesai.
3. Hubungkan perangkat Android fisik Anda via USB/Wireless Debugging atau jalankan **Android Emulator**.
4. Klik tombol **Run (Shift + F10)**.

---

## Kontribusi

Bila Anda ingin berkontribusi dalam pengembangan aplikasi, pastikan untuk membuat *branch* baru dan mengajukan *Pull Request* agar kode dapat di-*review* bersama. Selalu ikuti pola arsitektur **MVVM** dan gunakan **Hilt** untuk menginjeksi *dependency* baru.
