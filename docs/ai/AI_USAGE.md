# AI Kullanımı

Bu case sürecinde yapay zekâdan destek alındı. Bu dosya neyin, nerede ve nasıl kullanıldığını özetler.

## 1. Kullanılan araçlar

- **Claude Code** (Anthropic, model: Claude Sonnet 5.5) — kod yazımı, refactor, hata ayıklama
- **Android Studio** — geliştirme, derleme, cihazda çalıştırma

## 2. AI'ın yardım ettiği alanlar

- **Proje kurulumu:** Gradle version catalog, KSP, Secrets Gradle Plugin, Kotlin sürüm yükseltme, bağımlılık sürümlerinin Maven'dan doğrulanması
- **Mimari iskelet:** Clean Architecture paket yapısı (`data`, `domain`, `presentation`, `di`, `service`, `util`), Koin modülleri
- **Harita ve konum:** Google Maps (maps-compose), konum izni akışı, konum servisinin açılması, kullanıcı konumuna odaklanma
- **Takip ve marker:** Her 100 m'de marker ekleme mantığı, marker detay bottom sheet'i, ters coğrafi kodlama (adres)
- **Veri katmanı:** Room (marker kalıcılığı), DataStore (harita türü ve takibin açık olup olmadığı)
- **Arka plan takibi:** Foreground service (`location` tipi), bildirim, `RouteTracker`
- **UI bileşenleri:** Durum kartı, uyarı kartları, harita kontrol paneli, silme onayı bottom sheet'i
- **Yardımcı işler:** Kod yapısı hakkında tavsiyeler, `CLAUDE.md` oluşturma (`/init`), son bir kod incelemesi (`/code-review`)

## 3. Benim yaptıklarım ve kararlarım

### Mimari ve teknik kararlar
- **Kütüphane seçimi:** Hilt yerine **Koin**, kapt yerine **KSP**, tüm bağımlılıklar tek bir `libs.versions.toml` (version catalog) üzerinden.
- **Paket yapısı:** Clean Architecture klasör yapısını (`di`, `domain`, `data`, `presentation`, `service`, `util`) belirledim. Şablondan gelen `ui/theme` dosyalarını `presentation/theme` altına taşıdım.
- **API key güvenliği:** Maps key'inin repoya girmemesi için `local.properties` + Secrets Gradle Plugin yaklaşımı, commit edilebilir bir `local.defaults.properties` placeholder'ı. `.idea` klasörünü `.gitignore`'a ekledim.
- **Katmanlama:** Her özelliğin domain (arayüz + use case), data (implementasyon) ve presentation (ViewModel/UI) katmanlarına ayrılmasını ve Koin ile bağlanmasını istedim. UI modellerinin ve sabitlerin ViewModel dosyasında kalmaması gerektiğini fark edip ayrı dosyalara taşıdım.
- **Veri saklama:** Markerlar için **Room**, ayarlar için **DataStore** kullanılmasına karar verdim. DataStore'da harita türünü ve takibin açık olup olmadığını tutuyorum. Takip bilgisini, sistem uygulamayı öldürürse takibin uygulama tekrar açıldığında kaldığı yerden devam edebilmesi için sakladım.
- **Arka plan takibi:** Takibin uygulama arka plandayken de sürmesi için foreground service (`location` tipi) istedim. Takip mantığı UI'dan bağımsız kalsın diye ViewModel'den ayrı bir `RouteTracker`'a taşındı. Servisin sistem tarafından öldürülmesine karşı `START_STICKY` kullanıldı ve uygulama açılışında takip kaldığı yerden başlatılıyor.

### Davranış kararları
- **İzin akışı:** Kullanıcı ayarlara gitmekle uğraşmasın diye izin ve "konum kapalı" durumlarında önce uygulama içi sistem diyaloglarının çıkmasını istedim. Yalnızca izin kalıcı reddedilirse ayarlara yönlendiriliyor.
- **Marker kuralı:** Takip başlar başlamaz marker eklenmesin; marker, referans noktasından 100 m uzaklaşınca eklensin. Rota silinince ekrandaki tüm markerlar gitsin ve takip açıkken hemen yeni marker düşmesin.
- **Marker detayı:** Markera tıklayınca başlık, adres ve konum gösteren bir bottom sheet açılsın.
- **Silme onayı:** Silmeden önce Cancel / Reset butonlu, başlık ve açıklamalı, animasyonla kapanan bir bottom sheet.
- **Pil yönetimi:** Pil %10'un altındayken (şarjda değilken) ekranda uyarı kartı görünsün ve başlat butonu pasif olsun; takipteyken pil düşerse takip otomatik duraklasın ve kullanıcıya bildirimle söylensin. Konum güncellemesi için de mesafe filtresi kullanılsın.
- **Konum odağı:** Takip kapalıyken de harita açılışta kullanıcının konumuna ortalansın.

### Tasarım kararları
- **Harita kontrolleri:** Varsayılan zoom (+/-) butonlarını ve sağ üstteki konum butonunu kaldırdım. Yerine sağ altta, seçtiğim ikon tasarımına uygun iki panel ekledim: harita türü + konuma git, silme + başlat/duraklat.
- **Durum kartı:** Üstte ortada, küçük ve tam yuvarlak; takipte yeşil, takip yokken kırmızı olacak şekilde kullanıcıyı bildiren card ekledim.
- **Uyarı kartları:** Kullanıcının lokasyon izni olmadığında ve konumu açık olmadığında uyarı gösterilmesi için üstte ortada card oluşturdum.
- **Tam ekran deneyimi:** Alttaki sistem navigasyon butonları gizlenecek şekilde ayarladım.
- **Marker görseli:** Öncesinde kendi markerımı ekledim ama görsel olarak google mapsin default markerının daha uygun olacağını düşündüğüm için onu kullandım.
- **Metinler:** Hiçbir metin koda gömülü kalmasın, hepsi `strings.xml`'de olsun; metinleri İngilizceye çevirttim.

### İnceleme ve hata ayıklama
- **"Konum izni verdim ama yakınlaşmıyor":** Nedenini sorguladım; cihazın konum servisi kapalı çıktı ve bu durum için uygulama içi açma diyaloğu istedim.
- **Harita geç yükleniyor:** Cihazdan mı yoksa uygulamadan mı kaynaklandığını araştırdım; debug build ve ilk açılış etkileri üzerinde durduk.
- **Marker eklenmedi:** 100 m yürüdüğümde marker eklenmediğini gözlemledim ve haritanın kullanıcıyı takip etmediğini farkettim; bu iki bulgu üzerine kamera takibi ve konum güncellemesi düzeltildi.
- **Manifest uyarısı:** Android Studio'nun foreground service ipucunu inceledim; lint temiz çıktı ve bunun Play Console beyanı olduğunu öğrendim.
- **Kod incelemesi:** `/code-review` çalıştırdım. Çıkan bulgulardan `CLAUDE.md`'deki yanlış dosya bilgisi düzeltildi; kalan küçük öneriler (sabit isimleri, import sırası, model dosyalarının yeri) üzerinde karar bana kaldı.
- **Elle temizlik:** Gereksiz kodları sildim ve dosya yerleşimini düzenledim.

## 4. Doğrulama

### Otomatik kontroller
- `./gradlew :app:assembleDebug` — derleme
- `./gradlew :app:lintDebug` — lint (hata yok)
- `./gradlew :app:testDebugUnitTest` — birim testler: `DistanceCalculatorTest` (mesafe hesabı) ve `RouteTrackerTest` (100 m marker kuralı, rota silme, takip başlat/durdur, takibin hatırlanması)

### Emülatör ile test (`docs/marti_test_rota.gpx`)
Konum hareketini gerçek yürüyüş olmadan tekrar edilebilir biçimde denemek için küçük bir GPX rotası hazırladım ve emülatörde (Extended Controls → Location) oynattım. Rota 3 saniye arayla 11 noktadan oluşuyor:

- ilk nokta takibin başladığı referans noktası
- 30 m'lik üç adım (toplam 90 m): **marker eklenmemeli**
- ilk noktadan 120 m: marker eklenmeli
- kuzeye üç ve doğuya üç adet ~130 m'lik adım: her biri marker eklemeli
- arkaplanda çalışmada herhangi bir sorun olup olmadığına baktım.

Aynı rotada şunları da denedim: markera tıklayınca kamerasının ortalanması ve adres bottom sheet'inin açılması, rotayı silme onayı ve markerların temizlenmesi, uygulamayı kapatıp açınca markerların yerinde kalması, takibi durdurup tekrar başlatma.

### Fiziksel cihazda test
Emülatörün gerçek GPS, pil yönetimi ve izin davranışlarını taklit edemediği kısımları fiziksel cihazda denedim: konum izni ve konum servisi kapalı/açık akışları, foreground service bildirimi, takip sırasında kameranın kullanıcıyı izlemesi ve haritayı kaydırınca takibin bırakılması, Google Maps'in gerçek cihazda yüklenmesi.

## 5. Repoda AI ile ilgili dosyalar

- `docs/ai/AI_USAGE.md` (bu dosya)
- `docs/ai/CLAUDE.md` (`/init` ile oluşturuldu: proje komutları ve mimari özeti)
