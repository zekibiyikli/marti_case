# MartiCase

Bu proje, Martı Technologies için hazırlanmış bir case çalışmasıdır.

Kullanıcının konumunu Google Harita üzerinde gösteren ve takip sırasında her 100 metrede bir marker bırakan Android uygulaması.

## Özellikler

- Kullanıcının konumunu haritada gösterir, açılışta konuma yakınlaşır
- Konum izni ve konum servisi kapalıysa ekranın üstünde uyarı kartı gösterir; karta dokununca uygulama içi izin / "konumu aç" diyaloğu açılır (izin kalıcı reddedilmişse ayarlara yönlendirir)
- Başlat / duraklat ile takip: takip sırasında her 100 m'de bir marker eklenir ve harita kullanıcıyı izler
- Takip foreground service ile çalışır, uygulama arka plandayken de sürer (bildirimden durdurulabilir)
- Sistem takibi sonlandırırsa (ör. uygulama süreci öldürülürse) servis yeniden başlatılmayı dener; yine de başlamazsa uygulama tekrar açıldığında takip kaldığı yerden devam eder
- Pil %10'un altındaysa (şarj olmuyorsa) ekranın üstünde uyarı kartı görünür ve başlat butonu pasif olur; takipteyken pil düşerse takip otomatik duraklatılır ve bildirimle haber verilir
- Markerlar Room'da saklanır, uygulama kapanıp açılınca aynı yerlerinde kalır
- Markera dokununca başlık, adres (ters coğrafi kodlama) ve koordinatları gösteren bottom sheet açılır
- Rotayı silmeden önce onay bottom sheet'i
- Harita türü (normal / hybrid) DataStore'da saklanır

### Pil optimizasyonu

- **Konum isteği:** Yüksek doğrulukta, 2 saniye aralıkla ve en az **10 metre** hareket olduğunda güncelleme alınır. Kullanıcı durağanken gereksiz güncelleme gelmez. Marker aralığı 100 m olduğu için bu filtre markerları etkilemez. 10 m'de bıraktım: daha büyük bir değer (örn. 25-50 m) markerların 100 m'den daha fazla kaymasına ve kameranın daha seyrek güncellenmesine yol açar.
- **Düşük pil koruması:** Pil %10'un altındaysa ve cihaz şarj olmuyorsa uyarı kartı görünür ve takip başlatılamaz (başlat butonu pasif). Takipteyken pil bu seviyenin altına düşerse takip otomatik duraklatılır ve bildirimle haber verilir. Şarj edilince ya da pil yükselince kart kaybolur ve takip elle yeniden başlatılabilir.
- **Foreground service:** Takip yalnızca kullanıcı başlattığında çalışır; durdurulunca servis ve konum dinleme kapanır.

## Ekran görüntüleri

<table>
  <tr>
    <td align="center"><img src="docs/image/img_tracking.png" width="240" alt="Takip"><br>Takip ve markerlar</td>
    <td align="center"><img src="docs/image/img_marker_detail.png" width="240" alt="Marker detayı"><br>Marker detayı</td>
    <td align="center"><img src="docs/image/img_delete_route.png" width="240" alt="Rotayı sil"><br>Rotayı silme onayı</td>
  </tr>
</table>

## Kullanılan teknolojiler

| Alan | Kütüphane |
|---|---|
| UI | Jetpack Compose, Material 3 |
| Harita | Google Maps SDK, maps-compose |
| Konum | Fused Location Provider (Play Services) |
| Arka plan | Foreground service (`location` tipi) |
| DI | Koin |
| Mimari | MVVM, Clean Architecture, Use Case'ler |
| Asenkron | Kotlin Coroutines, Flow |
| Veritabanı | Room (KSP) |
| Ayarlar | Preferences DataStore |
| Loglama | Timber |

`minSdk 29`, `compileSdk / targetSdk 37`. Bağımlılık sürümleri `gradle/libs.versions.toml` içinde.

**Trade-off'lar**

- **Koin (Hilt yerine):** Küçük, tek modüllü projede daha sade ve kod üretimi gerektirmiyor; karşılığında bağımlılık hataları derleme zamanında değil çalışma zamanında ortaya çıkıyor.
- **MVVM + tek `UiState` (MVI yerine):** Az sayıda eylem için MVI'ın Intent/Reducer katmanları gereksiz kod olurdu (proje tam MVI değil, tek yönlü bir `UiState` kullanıyor); karşılığında durum değişiklikleri MVI kadar izlenebilir değil.
- **Foreground service (WorkManager yerine):** Sürekli konum takibi için gerekli, WorkManager periyodik işleri en az 15 dakikada bir çalıştırıyor; karşılığında kalıcı bildirim ve daha fazla pil tüketimi var.
- **Google Maps / maps-compose (OpenStreetMap yerine):** Compose entegrasyonu olgun ve cihazda iyi çalışıyor; karşılığında API key ve kota/faturalandırma gerekiyor.

## Mimari

Clean Architecture, tek modül (`:app`):

```
com.zekibiyikli.marticase
├── di/                     Koin modülleri
├── domain/                 Android bağımlılığı yok
│   ├── model/              LocationPoint, MapStyle
│   ├── repository/         Repository arayüzleri
│   └── usecase/            Tek işlik use case'ler
├── data/
│   ├── local/              Room (dao, entity), DataStore
│   ├── location/           Fused Location ve Geocoder kaynakları
│   ├── mapper/             Entity <-> domain dönüşümleri
│   └── repository/         Repository implementasyonları
├── presentation/
│   ├── map/                MapScreen, MapViewModel, UiState, components, dialogs
│   ├── permission/         İzin ve konum durumu yardımcıları
│   ├── battery/            Düşük pil durumu
│   └── theme/              Renk, tipografi, tema
├── service/                LocationTrackingService, RouteTracker
└── util/                   Mesafe ve pil hesapları
```

Takip akışı: `RouteTracker` (uygulama genelinde tek örnek) konum akışını dinler, 100 m kuralını uygular ve markerları Room'a yazar. `LocationTrackingService` onu foreground service olarak çalışır tutar. `MapViewModel` yalnızca `RouteTracker` ve Room'dan gelen durumu gözlemleyip ekranın tek bir `StateFlow`'unu oluşturur.

## Kurulum

Gereksinimler: Android Studio (AGP 9 destekli sürüm), JDK 17, Maps SDK for Android etkin bir Google Cloud API key'i.

1. Repoyu klonla ve Android Studio'da aç.
2. Proje kökündeki `local.properties` dosyasına key'ini ekle (bu dosya git'e girmez):

   ```
   MAPS_API_KEY=senin_api_key_in
   ```

   Key yoksa `local.defaults.properties` içindeki `DEFAULT_API_KEY` placeholder'ı kullanılır ve harita boş gelir.
3. Gerçek cihazda ya da Google Play destekli emülatörde çalıştır.

## Komutlar

```
./gradlew :app:assembleDebug        # derle
./gradlew :app:lintDebug            # lint
./gradlew :app:testDebugUnitTest    # birim testler
```

## Testler

### Birim testler

```
./gradlew :app:testDebugUnitTest
```

- `DistanceCalculatorTest`: mesafe hesabı (aynı nokta, bilinen mesafeler, simetri)
- `BatteryLevelTest`: pil düşük kararı (eşik, şarj durumu, geçersiz değerler)
- `RouteTrackerTest`: ilk konumun sadece referans olması, 100 m'den kısa/uzun adımlar, sonraki markerın son markerdan ölçülmesi, rota silme, takip başlat/durdur ve takibin hatırlanması

UI ve servis için otomatik test yok; bu kısımlar aşağıdaki gibi elle test edilir.

### Emülatörde test (GPX rotası ile)

Gerçekten yürümeden takibi tekrar edilebilir şekilde denemek için `docs/marti_test_rota.gpx` dosyası kullanılabilir. Rota 3 saniye arayla 11 noktadan oluşur.

1. Google Play destekli bir emülatör başlat ve uygulamayı çalıştır (konum iznini ver, emülatörde konumu aç).
2. Emülatör penceresindeki `...` (Extended controls) → **Location** → **Routes** sekmesinde **Load GPX/KML** ile `docs/marti_test_rota.gpx` dosyasını seç.
3. Uygulamada **play** butonuna bas (takip başlar), ardından emülatörde rotayı oynat (**Play route**; hızı artırabilirsin).
4. Beklenen sonuç, toplam **7 marker**:
   - İlk nokta sadece referanstır, marker eklenmez.
   - Küçük (30 m) adımlar marker eklemez; toplam 120 m'ye ulaşınca ilk marker eklenir.
   - Sonraki her ~100 m'lik adım (kuzeye ve doğuya) bir marker ekler.

### Elle test senaryoları

| Senaryo | Beklenen |
|---|---|
| İzin yokken uygulamayı aç | Üstte "Konum izni verilmedi" kartı çıkar; karta dokununca sistem izin diyaloğu açılır |
| İzin verilmiş, konum kapalı | "Konum kapalı" kartı çıkar; dokununca uygulama içi "konumu aç" diyaloğu açılır |
| İzin verilmiş, konum açık, pil yeterli iken play'e bas | Durum kartı yeşil "Tracking" olur, foreground service bildirimi görünür |
| Takipte haritayı kaydır | Kamera kullanıcıyı izlemeyi bırakır; konum butonuna basınca tekrar izler |
| Markera dokun | Kamera markerı ortalar, ardından başlık, adres ve koordinatlar bottom sheet'te açılır |
| Pause'a bas | Kart kırmızı "Not tracking" olur, bildirim kalkar |
| Uygulamayı kapatıp aç | Markerlar aynı yerlerinde kalır |
| Takipteyken uygulamayı arka plana al | Takip ve marker ekleme devam eder (bildirimden durdurulabilir) |
| Pil %10'un altında ve şarjda değil | Üstte "Battery is low" kartı çıkar, başlat butonu pasif olur; takipteyken düşerse takip durur ve "Tracking paused" bildirimi gelir |
| Çöp kutusuna bas → **Reset** | Onay bottom sheet'i açılır; onaylayınca tüm markerlar silinir |
| Harita ikonuna bas | Harita türü normal ↔ hybrid arasında değişir ve uygulama yeniden açılınca korunur |

## AI Assistance

Bu projenin geliştirilmesinde yapay zekâ desteği olarak Claude Code kullanıldı. 

- [`docs/ai/AI_USAGE.md`](docs/ai/AI_USAGE.md): AI'ın hangi alanlarda kullanıldığı, benim kararlarım ve doğrulama süreci
- [`docs/ai/CLAUDE.md`](docs/ai/CLAUDE.md): AI'a verilen proje bağlamı (komutlar, mimari özeti)
