# TulisApp

Aplikasi mobile Android sederhana yang memungkinkan pengguna untuk memposting teks, melihat postingan pengguna lain dalam bentuk _feed_, serta berinteraksi dengan postingan tersebut melalui fitur _like_. Dibangun dengan Firebase untuk otentikasi dan penyimpanan data _real-time_.

---

## Tentang Proyek

TulisApp adalah proyek aplikasi mobile Android yang dikembangkan sebagai platform berbagi postingan teks singkat. Tujuan utama proyek ini adalah untuk membangun pemahaman yang lebih dalam tentang pengembangan aplikasi Android modern, khususnya integrasi dengan layanan _backend-as-a-service_ (BaaS) seperti Firebase Authentication untuk manajemen pengguna dan Firebase Firestore untuk database NoSQL _real-time_.

Proyek ini menunjukkan implementasi fitur-fitur penting seperti:

- Autentikasi pengguna dengan dan Google Sign-In.
- Tampilan feed postingan yang dinamis dan diperbarui secara _real-time_.
- Interaksi pengguna melalui fitur "like" pada postingan.
- Pengelolaan status sesi pengguna, termasuk logout yang aman.

## Struktur Database (Firestore)

Koleksi utama yang digunakan adalah `posts`. Setiap dokumen dalam koleksi `posts` merepresentasikan satu postingan dengan struktur berikut:

- **`id`** (String): ID dokumen Firestore (dihasilkan secara otomatis).
- **`userId`** (String): UID (User ID) dari pengguna yang membuat postingan.
- **`userName`** (String): Nama tampilan pengguna yang memposting.
- **`userProfileImageUrl`** (String): URL gambar profil pengguna yang memposting.
- **`text`** (String): Konten teks dari postingan.
- **`timestamp`** (Timestamp): Waktu postingan dibuat (menggunakan `FieldValue.serverTimestamp()` untuk akurasi waktu server).
- **`likesCount`** (Number): Jumlah total "suka" yang diterima postingan ini.
- **`likedBy`** (Array<String>): Daftar UID pengguna yang telah menyukai postingan ini.

## Instalasi dan Setup Proyek

Untuk menjalankan proyek ini secara lokal, ikuti langkah-langkah berikut:

1.  **Clone Repositori:**

    ```bash
    git clone [https://github.com/nama_pengguna_anda/TulisApp.git](https://github.com/nama_pengguna_anda/TulisApp.git)
    cd TulisApp
    ```

    _(Ganti `nama_pengguna_anda` dengan username GitHub Anda)_

2.  **Buka di Android Studio:**

    - Buka Android Studio.
    - Pilih `File > Open` dan navigasikan ke folder `TulisApp` yang baru Anda clone.

3.  **Setup Firebase Project:**

    - Buat proyek baru di [Firebase Console](https://console.firebase.google.com/).
    - Tambahkan aplikasi Android baru ke proyek Firebase Anda. Ikuti petunjuk untuk menambahkan `google-services.json` ke direktori `app/` proyek Android Anda.
    - **Aktifkan Firebase Authentication:**
      - Di Firebase Console, navigasikan ke **Authentication**.
      - Buka tab **Sign-in method**.
      - Aktifkan provider **Email/Password**.
      - Aktifkan provider **Google** (pastikan SHA-1 Anda dikonfigurasi dengan benar).
    - **Aktifkan Cloud Firestore:**
      - Di Firebase Console, navigasikan ke **Firestore Database**.
      - Klik "Create database" dan pilih mode produksi atau mode uji (sesuai kebutuhan, namun disarankan mode produksi untuk belajar).
      - Jika Anda mengalami error `PERMISSION_DENIED: Cloud Firestore API has not been used...`, kunjungi URL yang diberikan di error tersebut untuk mengaktifkan API di Google Cloud Console.

4.  **Konfigurasi `default_web_client_id`:**

    - Pastikan `google-services.json` Anda berisi `oauth_client` dengan `client_type` "web". ID client-nya adalah `default_web_client_id` Anda.
    - Tambahkan string ini ke `res/values/strings.xml`:
      ```xml
      <string name="default_web_client_id">YOUR_WEB_CLIENT_ID_HERE</string>
      ```
      _(Ganti `YOUR_WEB_CLIENT_ID_HERE` dengan nilai yang sesuai dari `google-services.json` Anda.)_

5.  **Sinkronkan Proyek Gradle:**

    - Pastikan Anda memiliki semua dependensi yang diperlukan di `app/build.gradle`:
      ```gradle
      dependencies {
          // ...
          implementation 'com.google.firebase:firebase-auth'
          implementation 'com.google.android.gms:play-services-auth:21.0.0' // Untuk Google Sign-In
          implementation 'com.google.firebase:firebase-firestore'
          implementation 'com.github.bumptech.glide:glide:4.16.0' // Versi terbaru Glide
          annotationProcessor 'com.github.bumptech.glide:compiler:4.16.0'
          // ...
      }
      ```
    - Klik `Sync Project with Gradle Files` di Android Studio.

6.  **Buat Vector Assets (jika belum ada):**
    - Untuk ikon seperti `ic_logout_white_24dp`, `baseline_favorite_24`, `baseline_favorite_border_24`:
      - Di Android Studio, klik kanan `res/drawable > New > Vector Asset`.
      - Pilih `Clip Art` dan cari ikon yang dibutuhkan (misalnya `exit_to_app` untuk logout, `favorite` dan `favorite_border` untuk like).
      - Pastikan namanya sesuai dengan yang digunakan di kode (`R.drawable.ic_logout_white_24dp`, dll.).

## Firebase Security Rules

**PENTING:** Untuk pengujian awal, Anda bisa menggunakan aturan yang lebih longgar. Namun, untuk aplikasi produksi, sangat disarankan untuk menerapkan aturan yang lebih ketat untuk keamanan data.

Contoh aturan **untuk pengembangan (kurang aman)**:

```firestore
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Izinkan semua pengguna yang terautentikasi untuk membaca dan menulis ke koleksi 'posts'
    match /posts/{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```
