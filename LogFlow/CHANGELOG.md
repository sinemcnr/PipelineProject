# LogFlow - CHANGELOG

Bu dosya, LogFlow projesinde sürümlere göre yapılan değişiklikleri, eklenen özellikleri, mimari geliştirmeleri ve test sonuçlarını belgelemektedir.

Proje, Yazılım Mimarisi dersi kapsamında artımlı geliştirme yaklaşımıyla geliştirilmiştir.

---

## [v4] - Artım 4: YAML Tabanlı Yapılandırma Sistemi

### Eklenen Özellikler

- YAML tabanlı pipeline yapılandırma desteği eklendi.
- SnakeYAML 2.2 kütüphanesi projeye dahil edildi.
- StageFactory arayüzü oluşturuldu.
- StageRegistry sınıfı geliştirildi.
- PipelineLoader sınıfı eklendi.
- Pipeline aşamalarının YAML dosyalarından oluşturulması sağlandı.
- `--config` komut satırı parametresi eklendi.
- Yapılandırma parametreleri için doğrulama mekanizması geliştirildi.
- Bilinmeyen aşama türlerinin tespit edilmesi sağlandı.
- Eksik ve yanlış türdeki parametrelerin kontrolü eklendi.
- Boş pipeline yapılandırmalarının reddedilmesi sağlandı.
- Geçersiz aşama sıralamaları için kontroller eklendi.
- Fail Fast hata yönetimi yaklaşımı uygulandı.
- Üç farklı YAML yapılandırması hazırlandı.
- PipelineLoader için beş otomatik test geliştirildi.
- README.md ve ARCHITECTURE.md dokümantasyonları Artım 4'e göre güncellendi.

### Eklenen Java Bileşenleri

**StageFactory.java**

Yapılandırma parametrelerini kullanarak Stage nesnelerinin oluşturulmasını sağlayan fonksiyonel arayüzdür.

**StageRegistry.java**

Aşama türlerini ilgili StageFactory fonksiyonlarıyla eşleştiren kayıt sistemidir.

Map tabanlı yapı sayesinde aşama türleri dinamik olarak bulunabilir.

**PipelineLoader.java**

YAML yapılandırma dosyalarını okuyarak Source, Stage ve Sink bileşenlerini oluşturur.

PipelineBuilder kullanarak çalıştırılabilir Pipeline nesnesini hazırlar.

### Mimari Değişiklikler

- Pipeline tanımı Java kaynak kodundan ayrılarak YAML dosyalarına taşındı.
- Aşama oluşturma sorumluluğu StageFactory ve StageRegistry bileşenlerine aktarıldı.
- PipelineLoader ile yapılandırma okuma ve doğrulama işlemleri merkezileştirildi.
- Mevcut Pipeline ve PipelineBuilder yapıları yeni sistemle birlikte kullanılmaya devam edildi.
- Aynı derlenmiş Java uygulamasının farklı YAML yapılandırmalarıyla çalıştırılması sağlandı.
- Yapılandırma hatalarının veri işleme başlamadan tespit edilmesi sağlandı.

### Desteklenen YAML Aşamaları

| Aşama Türü | Java Sınıfı | Görevi |
|---|---|---|
| parse | ParserStage | Log ayrıştırma |
| filter | PredicateFilterStage | Koşullu filtreleme |
| project | ProjectionStage | Alan seçimi |

YAML yapılandırmasında desteklenen filtre işlemleri:

- HTTP durum kodu için `>=`
- URL yolu için `startsWith`

Mevcut yükleyicide ilk aşamanın parse olması zorunludur.

Project aşamasından sonra yeni aşama eklenmesine izin verilmemektedir.

### Eklenen YAML Yapılandırmaları

**1. error-report.yaml**

- HTTP durum kodu 400 ve üzerindeki kayıtları filtreler.
- timestamp, clientIp, status ve path alanlarını seçer.
- Sonuçları `output/errors.txt` dosyasına yazar.

**2. api-traffic.yaml**

- URL yolu `/api` ile başlayan kayıtları filtreler.
- timestamp, clientIp, method, path ve status alanlarını seçer.
- Sonuçları `output/api-traffic.txt` dosyasına yazar.

**3. bandwidth-by-client.yaml**

- İstemci IP adresi ve aktarılan bayt miktarını seçer.
- Sonuçları `output/bandwidth-by-client.txt` dosyasına yazar.
- IP bazında toplam bayt hesaplaması yapmaz.

### Komut Satırı Desteği

Artım 4 ile aşağıdaki parametre eklendi:

`--config`

Örnek kullanım:

```powershell
java -cp "out;lib/*" Main --config configs/error-report.yaml
```

Diğer yapılandırmalar:

```powershell
java -cp "out;lib/*" Main --config configs/api-traffic.yaml
```

```powershell
java -cp "out;lib/*" Main --config configs/bandwidth-by-client.yaml
```

### Yapılandırma Doğrulama ve Hata Yönetimi

Artım 4 kapsamında yapılandırma hatalarının erken tespit edilmesi sağlandı.

Kontrol edilen durumlar:

- Bilinmeyen aşama türü
- Eksik zorunlu parametre
- Yanlış parametre türü
- Boş aşama listesi
- Desteklenmeyen kaynak türü
- Desteklenmeyen çıktı türü
- Desteklenmeyen filtre işlemi
- Geçersiz aşama sıralaması

Fail Fast yaklaşımı sayesinde geçersiz yapılandırmalarla veri işlenmesi önlenir.

### Doğrulanan Hata Mesajları

**Bilinmeyen aşama türü:**

```text
Yapilandirma/parametre hatasi: Bilinmeyen asama turu: unknown
```

**Eksik zorunlu parametre:**

```text
Yapilandirma/parametre hatasi: Zorunlu parametre eksik: value
```

**Yanlış parametre türü:**

```text
Yapilandirma/parametre hatasi: status filtresi icin value tam sayi olmali.
```

**Boş pipeline:**

```text
Yapilandirma/parametre hatasi: Pipeline bos olamaz.
```

Bu dört hata senaryosu terminal üzerinde doğrulanmıştır.

### Eklenen Testler

`PipelineLoaderTest.java` dosyası oluşturuldu.

Beş otomatik test geliştirildi:

1. Geçerli YAML yapılandırmasının yüklenmesi ve çalıştırılması
2. Bilinmeyen aşama türünün reddedilmesi
3. Eksik zorunlu parametrenin tespit edilmesi
4. Yanlış parametre türünün reddedilmesi
5. Boş pipeline yapılandırmasının reddedilmesi

Bu testler bağımsız Java test çalıştırıcısı kullanılarak gerçekleştirilmiştir.

JUnit bağımlılığı gerektirmemektedir.

### Test Sonuçları

Doğrulanan terminal çıktısı:

```text
=== LogFlow PipelineLoader Testleri ===
[PASS] Gecerli yapilandirma
[PASS] Bilinmeyen asama turu
[PASS] Eksik parametre
[PASS] Yanlis parametre turu
[PASS] Bos pipeline

Basarili test: 5
Basarisiz test: 0
```

Artım 4 kapsamında:

- Toplam otomatik test: 5
- Başarılı test: 5
- Başarısız test: 0
- Başarı oranı: %100

### Yapılandırma Çalıştırma Sonuçları

| Yapılandırma | Çıktı Kaydı | Durum |
|---|---:|---|
| error-report.yaml | 1 | Başarılı |
| api-traffic.yaml | 0 | Çalıştı, eşleşen kayıt yok |
| bandwidth-by-client.yaml | 5 | Başarılı |

Üç YAML yapılandırması da aynı derlenmiş Java uygulamasıyla çalıştırılmıştır.

API yapılandırmasının filtreleme davranışı, eşleşen örnek verilerle ayrıca test edilmelidir.

### Bilinen Sınırlamalar

- Dinamik aşamalar arasında tam tür güvenliği doğrulaması bulunmamaktadır.
- YAML üzerinden yalnızca kayıtlı aşama türleri kullanılabilir.
- İlk aşama parse olmak zorundadır.
- Project aşamasından sonra başka aşama eklenemez.
- Bant genişliği yapılandırması IP bazında toplama yapmamaktadır.
- Yeni aşama türlerinin kullanılması için ilgili fabrika fonksiyonunun kayıt sistemine eklenmesi gerekmektedir.

---

## [v3] - Artım 3: Filtreleme, Projeksiyon ve PipelineBuilder

### Eklenen Özellikler

- PredicateFilterStage sınıfı eklendi.
- Predicate<LogRecord> ile koşullu filtreleme sağlandı.
- HTTP durum koduna göre filtreleme eklendi.
- URL yolu ön ekine göre filtreleme eklendi.
- Zaman aralığına göre filtreleme desteklendi.
- ProjectionStage sınıfı eklendi.
- LogRecord alanlarının seçilerek aktarılması sağlandı.
- FileSink sınıfı eklendi.
- PipelineBuilder sınıfı eklendi.
- Fluent API ile pipeline oluşturma desteklendi.
- Komut satırı parametreleri eklendi.
- Filtre sıralaması deneyi gerçekleştirildi.
- ARCHITECTURE.md dokümantasyonu hazırlandı.

### Komut Satırı Parametreleri

- `--min-status`
- `--path-prefix`
- `--fields`
- `--output`

### Mimari Değişiklikler

- Pipeline sınıfı birden fazla Stage destekleyecek şekilde yeniden düzenlendi.
- Main.java, PipelineBuilder kullanacak şekilde güncellendi.
- Sink arayüzüne open() ve close() metotları eklendi.
- Veri işleme aşamaları Fluent API ile birleştirilebilir hâle getirildi.

### Eklenen Testler

- PredicateFilterStageTest: 7 test
- ProjectionStageTest: 8 test
- FileSinkTest: 7 test
- PipelineIntegrationTest: 1 test

Artım 3 kapsamında 23 yeni test eklendi.

### Test Sonuçları

Önceki doğrulanan JUnit çalıştırmasında:

- Toplam test: 32
- Başarılı test: 32
- Başarısız test: 0

### Filtre Sıralaması Deneyi

200 kayıtlık test veri seti kullanıldı.

Karşılaştırılan sıralamalar:

1. Status → Path
2. Path → Status

Sonuçlar:

| Ölçüm | Status → Path | Path → Status |
|---|---:|---:|
| Çıktı kaydı | 20 | 20 |
| Status kontrolü | 200 | 100 |
| Path kontrolü | 40 | 200 |
| Toplam kontrol | 240 | 300 |

Her iki sıralama aynı çıktıyı üretmiştir.

Status filtresinin önce kullanılması, bu veri setinde toplam filtre kontrol sayısını %20 azaltmıştır.

Tek çalıştırmadaki süre farkları kesin performans üstünlüğü olarak değerlendirilmemiştir.

---

## [v2] - Artım 2: LogRecord ve ParserStage

### Eklenen Özellikler

- LogRecord veri modeli eklendi.
- Java record yapısı kullanıldı.
- ParserStage sınıfı geliştirildi.
- CLF formatındaki log satırları ayrıştırıldı.
- HTTP method, path, status ve bytes alanları yapılandırılmış hâle getirildi.
- Geçersiz log satırlarını sayma özelliği eklendi.
- `getErrorCount()` metodu eklendi.

### Testler

- ParserStage için 8 birim testi yazıldı.
- Pipeline için 1 test yazıldı.
- JUnit test altyapısı kullanıldı.
- JaCoCo ile kod kapsama analizi yapıldı.

### Kod Kapsama Sonucu

JaCoCo satır kapsama oranı: %76,24

### İyileştirmeler

- Log satırlarının yapılandırılmış nesnelere dönüştürülmesi sağlandı.
- Parser hatalarının izlenebilirliği artırıldı.
- Pipeline davranışları testlerle kontrol edildi.

---

## [v1] - Artım 1: Temel Pipeline Mimarisi

### Eklenen Özellikler

- Source arayüzü oluşturuldu.
- Stage arayüzü oluşturuldu.
- Sink arayüzü oluşturuldu.
- Emitter arayüzü oluşturuldu.
- Pipeline sınıfı geliştirildi.
- FileLineSource sınıfı eklendi.
- ConsoleSink sınıfı eklendi.

### Mimari Yapı

Source - Stage - Sink yaklaşımı kullanıldı.

Temel veri akışı:

```text
FileLineSource
      |
      v
    Stage
      |
      v
  ConsoleSink
```

### Kazanımlar

- Modüler veri işleme altyapısı oluşturuldu.
- Bileşenler arasında görev ayrımı sağlandı.
- Pipeline mimarisinin temeli atıldı.
- Sonraki artımlar için genişletilebilir bir yapı hazırlandı.

---

## Sürüm Özeti

| Sürüm | Temel Geliştirmeler |
|---|---|
| v1 | Source, Stage, Sink ve temel Pipeline |
| v2 | LogRecord, ParserStage, JUnit ve JaCoCo |
| v3 | Filtreleme, projeksiyon, FileSink ve PipelineBuilder |
| v4 | YAML, StageFactory, StageRegistry, PipelineLoader ve yapılandırma testleri |

**Sürüm durumu:** Artım 4 geliştirmeleri ve beş otomatik yükleyici testi tamamlanmıştır. `v4` Git etiketi henüz oluşturulmamıştır; GitHub'a gönderme ve etiketleme işlemleri teslim aşamasında yapılacaktır.