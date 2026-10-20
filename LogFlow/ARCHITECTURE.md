# LogFlow - Mimari Dokümantasyon

## 1. Proje Genel Bakışı

LogFlow, Java programlama dili kullanılarak geliştirilen, log dosyalarını modüler ve aşamalı olarak işleyen bir yazılım uygulamasıdır.

Proje, **Source - Stage - Sink** mimari yaklaşımını temel alır.

Sistem, log kayıtlarını okumak, ayrıştırmak, filtrelemek, belirli alanlarını seçmek ve sonuçları dosyaya yazmak amacıyla geliştirilmiştir.

### 1.1. Artım 3 Kapsamında Geliştirilen Özellikler

- Predicate tabanlı filtreleme
- Alan seçme (Projection)
- Dosyaya çıktı yazma
- Fluent PipelineBuilder API
- Komut satırı parametreleri
- Birim ve entegrasyon testleri
- Filtre sıralaması ve performans deneyi

### 1.2. Artım 4 Kapsamında Geliştirilen Özellikler

- YAML tabanlı yapılandırma sistemi
- StageFactory arayüzü
- StageRegistry kayıt sistemi
- PipelineLoader yapılandırma yükleyicisi
- Yapılandırma parametrelerinin doğrulanması
- Üç farklı YAML yapılandırması
- Hatalı yapılandırmaların erken tespiti (Fail Fast)
- Otomatik yapılandırma testleri
- Java kodunu yeniden derlemeden farklı pipeline davranışları elde edilmesi

Artım 4 ile işlem hattının tanımı, Java kaynak kodundan ayrılarak yapılandırma dosyalarına taşınmıştır.

---

## 2. Sistemin Mimari Yapısı

### 2.1. Artım 3 Veri Akışı

```text
FileLineSource
      |
      v
ParserStage
      |
      v
PredicateFilterStage
      |
      v
ProjectionStage
      |
      v
FileSink
```

### 2.2. Artım 4 Yapılandırma Odaklı Mimari

```text
YAML Configuration
        |
        v
PipelineLoader
        |
        v
StageRegistry
        |
        v
StageFactory
        |
        v
PipelineBuilder
        |
        v
Pipeline
        |
        v
Source -> Stages -> Sink
        |
        v
Output File
```

Bu mimaride YAML dosyası işlem hattının tanımını içerir.

PipelineLoader yapılandırmayı okur ve doğrular.

StageRegistry, aşama türü ile ilgili fabrika fonksiyonunu eşleştirir.

StageFactory, aşama nesnelerinin oluşturulmasını sağlar.

PipelineBuilder, oluşturulan aşamaları birleştirir.

Pipeline ise veri işleme sürecini yürütür.

---

## 3. Temel Mimari Bileşenler

### 3.1. FileLineSource

Log dosyasını satır satır okuyarak işlem hattına aktarır.

Kaynak bileşenidir ve ham metin satırları üretir.

### 3.2. ParserStage

Ham log satırlarını LogRecord nesnelerine dönüştürür.

Geçersiz log satırlarını sayar.

Hata sayısı `getErrorCount()` metodu ile elde edilebilir.

### 3.3. PredicateFilterStage

`Predicate<LogRecord>` kullanarak kayıtları belirli koşullara göre filtreler.

Desteklenen örnek koşullar:

- HTTP durum kodu 400 ve üzeri
- URL yolunun `/api` ile başlaması
- Belirli bir zaman aralığında bulunma

Artım 4 yapılandırma yükleyicisinde şu filtre türleri uygulanmıştır:

- `status >= değer`
- `path startsWith değer`

Koşulu sağlayan kayıtlar sonraki aşamaya aktarılır.

### 3.4. ProjectionStage

LogRecord nesnesinden yalnızca seçilen alanları aktarır.

Örnek alanlar:

- timestamp
- clientIp
- method
- path
- status
- bytes
- userAgent
- attributes
- raw

Seçilen alanlar LinkedHashMap kullanılarak belirtilen sırada tutulur.

Bu aşama veri minimizasyonuna katkı sağlar.

### 3.5. FileSink

İşlem hattından gelen kayıtları çıktı dosyasına yazar.

Temel metotları:

- `open()`: Çıktı dosyasını açar.
- `consume()`: Gelen kaydı dosyaya yazar.
- `close()`: Dosya kaynağını kapatır.

Dosya yazma işlemlerinde BufferedWriter kullanılır.

### 3.6. Pipeline

Pipeline, Source, Stage ve Sink bileşenlerini çalıştıran ana sınıftır.

Temel sorumlulukları:

- Aşamaları sırayla açmak
- Sink bileşenini açmak
- Kaynaktan verileri almak
- Kayıtları aşamalardan geçirmek
- İşlem sonunda kaynakları kapatmak

Pipeline sınıfı, `run()` metodu ile çalıştırılır.

Aşamalar arasında Emitter yapısı kullanılır.

---

## 4. PipelineBuilder Tasarımı

PipelineBuilder, aşamaların Fluent API yaklaşımıyla birleştirilmesini sağlar.

Örnek:

```java
Pipeline pipeline = PipelineBuilder
        .from(new FileLineSource(inputFile))
        .via(new ParserStage())
        .via(new PredicateFilterStage(
                r -> r.status() >= 400
        ))
        .via(new ProjectionStage(
                "clientIp", "status", "path"
        ))
        .to(new FileSink<>(outputFile));

pipeline.run();
```

### 4.1. Avantajları

- Okunabilir kod yapısı
- Yeni aşamaların kolay eklenmesi
- Aşamaların yeniden kullanılabilmesi
- Generic tür denetimi
- Pipeline oluşturma işleminin sadeleştirilmesi

Artım 4 kapsamında PipelineBuilder, PipelineLoader tarafından dinamik olarak kullanılmaktadır.

YAML dosyasında tanımlanan aşamalar çalışma sırasında oluşturulup işlem hattına eklenir.

---

## 5. Artım 3 - Komut Satırı Arayüzü

Artım 3 kapsamında kullanıcılar komut satırı parametreleriyle filtreleme işlemlerini belirleyebilir.

### 5.1. Desteklenen Parametreler

| Parametre | Açıklama |
|---|---|
| `--min-status` | Minimum HTTP durum kodu |
| `--path-prefix` | URL başlangıç filtresi |
| `--fields` | Çıktıya aktarılacak alanlar |
| `--output` | Çıktı dosyası |

Örnek:

```powershell
java -cp out Main data\access-small.log --min-status 400 --path-prefix /api --fields clientIp,status,path --output output.log
```

Artım 4 ile mevcut komut satırı desteği korunmuş ve `--config` parametresi eklenmiştir.

---

## 6. Artım 4 - YAML Yapılandırma Sistemi

Artım 4 kapsamında işlem hattının tanımı YAML dosyalarına taşınmıştır.

Bu yaklaşım, işlem hattının Java kodundan bağımsız şekilde yapılandırılmasını sağlar.

### 6.1. YAML Yapılandırma Şeması

Bir yapılandırma dosyası aşağıdaki temel alanları içerir:

| Alan | Açıklama |
|---|---|
| `pipeline` | İşlem hattının kök nesnesi |
| `name` | İşlem hattının adı |
| `source` | Veri kaynağı |
| `source.type` | Kaynak türü |
| `source.params.path` | Girdi dosyası |
| `stages` | İşlem aşamaları |
| `stages[].type` | Aşama türü |
| `stages[].params` | Aşama parametreleri |
| `sink` | Çıktı hedefi |
| `sink.params.path` | Çıktı dosyası |

### 6.2. Örnek YAML Yapılandırması

```yaml
pipeline:
  name: error-report

  source:
    type: file
    params:
      path: data/access-small.log

  stages:
    - type: parse
      params:
        format: clf

    - type: filter
      params:
        field: status
        op: ">="
        value: 400

    - type: project
      params:
        fields:
          - timestamp
          - clientIp
          - status
          - path

  sink:
    type: file
    params:
      path: output/errors.txt
```

Bu yapılandırma, HTTP durum kodu 400 ve üzerindeki kayıtları seçer ve yalnızca belirtilen alanları çıktı dosyasına yazar.

### 6.3. Yapılandırmanın Avantajları

- İşlem hattı tanımı kaynak koddan ayrılır.
- Filtre değerleri YAML üzerinden değiştirilebilir.
- Aynı Java uygulaması farklı yapılandırmalarla çalışabilir.
- Yeni pipeline tanımları için yeniden derleme gerekmez.
- Yapılandırmalar ayrı dosyalarda saklanabilir.

---

## 7. StageFactory Tasarımı

StageFactory, yapılandırma parametrelerinden Stage nesnesi oluşturan fonksiyonel arayüzdür.

```java
@FunctionalInterface
public interface StageFactory {

    Stage<?, ?> create(
        Map<String, Object> params
    );
}
```

Her aşama türü kendi fabrika fonksiyonu aracılığıyla oluşturulur.

Bu tasarım, nesne oluşturma sorumluluğunu yapılandırma yükleme işleminden ayırır.

---

## 8. StageRegistry Tasarımı

StageRegistry, aşama türlerini fabrika fonksiyonlarıyla eşleştiren kayıt sistemidir.

Temel veri yapısı:

```java
Map<String, StageFactory> factories;
```

Örnek kayıt:

```java
registry.register(
    "parse",
    params -> new ParserStage()
);
```

Sistem aşağıdaki aşama türlerini destekler:

| Aşama türü | Java sınıfı |
|---|---|
| parse | ParserStage |
| filter | PredicateFilterStage |
| project | ProjectionStage |

### 8.1. Veri Odaklı Kayıt Sistemi

StageRegistry, aşama türlerini sabit bir switch yapısı yerine Map veri yapısında saklar.

Bu sayede yeni bir aşama türü, ilgili fabrika fonksiyonunun kayıt sistemine eklenmesiyle kullanılabilir.

Yeni aşamalar için Pipeline sınıfının değiştirilmesi gerekmez.

### 8.2. Bilinmeyen Aşama Türü

Kayıt sisteminde bulunmayan bir aşama istendiğinde açık hata mesajı oluşturulur.

Örnek:

```text
Bilinmeyen asama turu: unknown
```

Bu davranış hatalı yapılandırmanın işlenmesini engeller.

---

## 9. PipelineLoader Tasarımı

PipelineLoader, YAML yapılandırmasını okuyarak Pipeline nesnesi oluşturan bileşendir.

### 9.1. Temel Sorumlulukları

1. YAML dosyasını okumak
2. Yapılandırmanın kök nesnesini doğrulamak
3. Source tanımını kontrol etmek
4. Sink tanımını kontrol etmek
5. Stage listesini okumak
6. Aşama parametrelerini doğrulamak
7. StageRegistry üzerinden aşama nesneleri oluşturmak
8. PipelineBuilder kullanarak işlem hattını hazırlamak
9. Oluşturulan Pipeline nesnesini döndürmek

### 9.2. YAML Ayrıştırma

YAML dosyalarının okunması için SnakeYAML kütüphanesi kullanılmaktadır.

```java
Yaml yaml = new Yaml();
```

YAML içeriği Java Map ve List veri yapılarına dönüştürülür.

### 9.3. Parametre Doğrulama

PipelineLoader şu durumları kontrol eder:

- Zorunlu parametrenin eksik olması
- Yanlış veri türü kullanılması
- Bilinmeyen aşama türü
- Boş aşama listesi
- Desteklenmeyen kaynak veya çıktı türü
- Desteklenmeyen filtre işlemi
- Geçersiz aşama sıralaması

Mevcut uygulamada ilk aşamanın `parse` olması zorunludur.

`project` aşamasından sonra yeni bir aşama eklenmesine izin verilmemektedir.

Bu sınırlama, mevcut veri türleri arasındaki uyumluluğu korumak amacıyla uygulanmıştır.

### 9.4. Tür Güvenliği

PipelineBuilder, Java kodunda generic tür denetimi sağlar.

Ancak YAML üzerinden oluşturulan aşamalar dinamik olduğu için PipelineLoader içerisinde raw type dönüşümleri kullanılmaktadır.

Bu nedenle dinamik işlem hattında tür güvenliği yalnızca derleme zamanında garanti edilemez.

Mevcut yükleyici, aşama sıralaması üzerinden temel uyumluluk denetimleri gerçekleştirir.

Daha genel bir aşama sistemi için giriş ve çıkış türlerinin ayrıca doğrulanması gerekir.

---

## 10. Üç Farklı YAML Yapılandırması

Artım 4 kapsamında aynı Java uygulamasını değiştirmeden çalıştıran üç farklı yapılandırma hazırlanmıştır.

### 10.1. Hata Raporu

Dosya:

`configs/error-report.yaml`

Amaç:

HTTP durum kodu 400 ve üzerindeki kayıtları filtrelemek.

Çalıştırma:

```powershell
java -cp "out;lib/*" Main --config configs/error-report.yaml
```

Çıktı:

`output/errors.txt`

Doğrulanan örnek kayıt:

```text
{timestamp=2026-09-29T10:15:05Z, clientIp=10.0.0.5, status=403, path=/admin}
```

### 10.2. API Trafiği Raporu

Dosya:

`configs/api-traffic.yaml`

Amaç:

URL yolu `/api` ile başlayan kayıtları filtrelemek.

Çalıştırma:

```powershell
java -cp "out;lib/*" Main --config configs/api-traffic.yaml
```

Çıktı:

`output/api-traffic.txt`

Mevcut küçük veri dosyasında `/api` ile başlayan kayıt bulunmadığından çıktı boş oluşmuştur.

Bu durum, ilgili yapılandırmanın çalıştırılabildiğini gösterir; filtrenin eşleşen veriler üzerindeki davranışı ayrıca test edilmelidir.

### 10.3. İstemci Bazında Bant Genişliği Verileri

Dosya:

`configs/bandwidth-by-client.yaml`

Amaç:

Her log kaydındaki istemci IP adresini ve aktarılan bayt miktarını çıkarmak.

Çalıştırma:

```powershell
java -cp "out;lib/*" Main --config configs/bandwidth-by-client.yaml
```

Çıktı:

`output/bandwidth-by-client.txt`

Doğrulanan örnek:

```text
{clientIp=127.0.0.1, bytes=512}
{clientIp=192.168.1.10, bytes=245}
{clientIp=10.0.0.5, bytes=128}
{clientIp=172.16.0.8, bytes=1024}
{clientIp=192.168.1.15, bytes=768}
```

Bu yapılandırma istemci IP adresi ve bayt alanlarını çıkarır.

IP bazında toplama veya gruplama işlemi gerçekleştirilmemektedir.

### 10.4. Yapılandırmaların Karşılaştırılması

| Yapılandırma | İşlem | Doğrulanan sonuç |
|---|---|---|
| error-report | Status filtreleme | 1 kayıt |
| api-traffic | Path filtreleme | 0 kayıt |
| bandwidth-by-client | Alan seçme | 5 kayıt |

Üç yapılandırma da aynı derlenmiş Java uygulaması kullanılarak çalıştırılmıştır.

Bu sonuç, yapılandırma değişiklikleriyle farklı işlem hattı davranışlarının elde edilebildiğini göstermektedir.

---

## 11. Hata Yönetimi ve Fail Fast Yaklaşımı

Artım 4 kapsamında yapılandırma zamanı hataları ile çalışma zamanı hataları birbirinden ayrılmıştır.

### 11.1. Yapılandırma Zamanı Hataları

YAML dosyasındaki geçersiz tanımlar, Pipeline çalıştırılmadan önce kontrol edilir.

Örnek hatalar:

- Bilinmeyen aşama türü
- Eksik parametre
- Yanlış parametre türü
- Boş pipeline

### 11.2. Fail Fast Yaklaşımı

Fail Fast yaklaşımında hatalı yapılandırma tespit edildiğinde işlem hattı başlatılmaz.

Bu sayede geçersiz yapılandırmalarla veri işlenmesi önlenir.

### 11.3. Doğrulanan Hata Mesajları

**Bilinmeyen aşama:**

```text
Yapilandirma/parametre hatasi: Bilinmeyen asama turu: unknown
```

**Eksik parametre:**

```text
Yapilandirma/parametre hatasi: Zorunlu parametre eksik: value
```

**Yanlış veri türü:**

```text
Yapilandirma/parametre hatasi: status filtresi icin value tam sayi olmali.
```

**Boş pipeline:**

```text
Yapilandirma/parametre hatasi: Pipeline bos olamaz.
```

Bu hataların tamamı terminal üzerinde test edilmiştir.

### 11.4. Çalışma Zamanı Hataları

Çalışma zamanı hataları, veri işlenirken meydana gelebilir.

Örnekler:

- Dosyanın okunamaması
- Geçersiz log satırları
- Dosyaya yazma hataları
- Stage işlem hataları

Bu hatalar yapılandırma hatalarından farklıdır.

Artım 4 kapsamında öncelik, yapılandırma hatalarının veri işleme başlamadan tespit edilmesidir.

---

## 12. Artım 3 - Filtre Sıralaması Deneyi

### 12.1. Deneyin Amacı

Filtrelerin uygulanma sırasının sonuç doğruluğuna ve işlem maliyetine etkisini incelemek.

### 12.2. Kullanılan Veri Seti

Deney için 200 adet CLF formatında log kaydı oluşturulmuştur.

Veri setinde:

- HTTP 200 durum kodu
- HTTP 500 durum kodu
- `/api/users` URL yolu
- `/home` URL yolu

bulunmaktadır.

Dosya:

`data/filter-experiment.log`

### 12.3. Birinci Sıralama

```text
FileLineSource
      |
      v
ParserStage
      |
      v
Status Filter
      |
      v
Path Filter
      |
      v
ProjectionStage
      |
      v
Sink
```

Önce HTTP durum kodu, ardından URL yolu kontrol edilir.

### 12.4. İkinci Sıralama

```text
FileLineSource
      |
      v
ParserStage
      |
      v
Path Filter
      |
      v
Status Filter
      |
      v
ProjectionStage
      |
      v
Sink
```

Önce URL yolu, ardından HTTP durum kodu kontrol edilir.

### 12.5. Deney Sonuçları

| Ölçüm | Status → Path | Path → Status |
|---|---:|---:|
| Girdi kaydı | 200 | 200 |
| Çıktı kaydı | 20 | 20 |
| Status kontrolü | 200 | 100 |
| Path kontrolü | 40 | 200 |
| Toplam kontrol | 240 | 300 |
| Aynı çıktı | Evet | Evet |

Paylaşılan deney çalıştırmasında ölçülen süreler:

| Sıralama | Süre |
|---|---:|
| Status → Path | 90.222 ms |
| Path → Status | 11.791 ms |

Bu süreler tek bir çalıştırmadan elde edilmiştir.

JVM ısınması, sistem yükü ve diğer etkenler süreleri değiştirebilir.

Bu nedenle süre sonuçları kesin bir performans üstünlüğü olarak yorumlanmamalıdır.

### 12.6. Deneyin Değerlendirilmesi

İki filtre sıralaması da aynı 20 kaydı üretmiştir.

Status filtresinin önce kullanıldığı sıralama toplam 240 kontrol yapmıştır.

Path filtresinin önce kullanıldığı sıralama toplam 300 kontrol yapmıştır.

Birinci sıralamada 60 daha az kontrol gerçekleştirilmiştir.

Bu, toplam filtre kontrol sayısında %20 azalma anlamına gelmektedir.

Deney, filtre sıralamasının sonuç doğruluğunu değiştirmeden işlem maliyetini etkileyebildiğini göstermiştir.

---

## 13. Test Stratejisi

### 13.1. Artım 3 Testleri

Artım 3 kapsamında birim ve entegrasyon testleri geliştirilmiştir.

**PredicateFilterStageTest**

7 test bulunmaktadır.

Status, path ve timestamp filtreleme davranışları kontrol edilmektedir.

**ProjectionStageTest**

8 test bulunmaktadır.

Alan seçimi, alan sırası, geçersiz alanlar ve veri doğruluğu test edilmektedir.

**FileSinkTest**

7 test bulunmaktadır.

Dosya açma, veri yazma, dosya kapatma ve hatalı kullanım durumları test edilmektedir.

**PipelineIntegrationTest**

1 entegrasyon testi bulunmaktadır.

ParserStage, PredicateFilterStage ve ProjectionStage birlikte çalıştırılarak FileSink dosya çıktısı kontrol edilmektedir.

Önceki JUnit çalıştırmasında:

- Toplam test: 32
- Başarılı test: 32
- Başarısız test: 0

### 13.2. Artım 4 PipelineLoader Testleri

Artım 4 kapsamında `PipelineLoaderTest.java` sınıfı oluşturulmuştur.

Bu sınıf beş farklı yapılandırma senaryosunu test eder.

| Test | Amaç | Sonuç |
|---|---|---|
| Geçerli yapılandırma | Pipeline oluşturma ve çalıştırma | PASS |
| Bilinmeyen aşama | Kayıtsız aşamanın reddedilmesi | PASS |
| Eksik parametre | Zorunlu alan doğrulaması | PASS |
| Yanlış parametre türü | Veri türü doğrulaması | PASS |
| Boş pipeline | Boş aşama listesinin reddedilmesi | PASS |

Terminal çıktısı:

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

### 13.3. Testlerin Çalıştırılması

Derleme:

```powershell
javac -cp "out;lib/*" -d out test\PipelineLoaderTest.java
```

Çalıştırma:

```powershell
java -cp "out;lib/*" PipelineLoaderTest
```

Artım 4 testleri bağımsız Java test çalıştırıcısı kullanılarak gerçekleştirilmiştir.

Bu test sınıfı JUnit gerektirmemektedir.

---

## 14. Mimari Tasarım İlkeleri

### 14.1. Single Responsibility Principle

Her bileşen belirli bir görevden sorumludur.

- ParserStage: Log ayrıştırma
- PredicateFilterStage: Filtreleme
- ProjectionStage: Alan seçme
- FileSink: Dosyaya yazma
- StageRegistry: Aşama kayıt yönetimi
- StageFactory: Aşama nesnesi oluşturma
- PipelineLoader: Yapılandırma yükleme

### 14.2. Open/Closed Principle

Yeni Stage sınıfları sisteme eklenebilir.

Kayıt sistemi sayesinde yeni aşamalar mevcut Pipeline sınıfı değiştirilmeden kullanılabilir.

Yeni aşama türlerinin kullanılabilmesi için ilgili fabrika fonksiyonunun kayıt sistemine eklenmesi gerekir.

### 14.3. Yeniden Kullanılabilirlik

PredicateFilterStage farklı Predicate koşullarıyla yeniden kullanılabilir.

ProjectionStage farklı alan seçimleriyle çalıştırılabilir.

Aynı Java aşamaları farklı YAML yapılandırmalarında tekrar kullanılabilir.

### 14.4. Separation of Concerns

İşlem hattının tanımı ile gerçekleştirilmesi birbirinden ayrılmıştır.

YAML dosyaları işlem hattının nasıl yapılandırılacağını belirtir.

Java sınıfları ise verilerin nasıl işleneceğini gerçekleştirir.

### 14.5. Tür Güvenliği

PipelineBuilder, Java kaynak kodunda generic tür kontrolü sağlar.

Dinamik yapılandırma sisteminde ise yükleyici temel aşama sıralaması denetimleri gerçekleştirir.

Tam dinamik tür doğrulaması mevcut sürümde sınırlıdır.

### 14.6. Kaynak Yönetimi

FileSink, open() ve close() metotlarıyla dosya kaynaklarını yönetir.

Pipeline, aşamaların yaşam döngüsü metotlarını çalıştırır.

### 14.7. Genişletilebilirlik

StageRegistry ve StageFactory sayesinde yeni aşamalar kayıt sistemine eklenebilir.

YAML yapılandırmaları kullanılarak aynı aşamalar farklı işlem hatlarında birleştirilebilir.

---

## 15. Artım 3 ve Artım 4 Karşılaştırması

| Özellik | Artım 3 | Artım 4 |
|---|---|---|
| Pipeline tanımı | Java kodu | YAML |
| Aşama oluşturma | Doğrudan | StageRegistry |
| Parametre yönetimi | Komut satırı | YAML ve komut satırı |
| Yeni yapılandırma | Kod/parametre değişikliği | YAML değişikliği |
| Yeniden derleme | Kod değişince gerekli | YAML değişince gerekli değil |
| Hata doğrulama | Komut satırı parametreleri | Yapılandırma şeması ve parametreler |
| Testler | Birim ve entegrasyon | Yapılandırma yükleyici testleri |
| Mimari odak | Modülerlik | Genişletilebilirlik |

---

## 16. Bilinen Sınırlamalar ve Gelecek Geliştirmeler

Mevcut uygulamada aşağıdaki sınırlamalar bulunmaktadır:

1. YAML yapılandırmasıyla yalnızca desteklenen aşama türleri kullanılabilir.
2. İlk aşamanın parse olması zorunludur.
3. Project aşamasından sonra başka bir aşama eklenemez.
4. Dinamik aşamalar arasında tam tür güvenliği doğrulaması bulunmamaktadır.
5. API filtresi, eşleşen örnek kayıtlar üzerinde ayrıca test edilmelidir.
6. Bant genişliği yapılandırması IP bazında toplama işlemi yapmamaktadır.
7. Yapılandırma doğrulaması tüm olası hatalı alanları kapsayacak şekilde genişletilebilir.
8. Yeni aşama türü eklenmesi, mevcut kayıt mekanizmasına yeni fabrika fonksiyonu kaydedilmesini gerektirir.

Gelecek sürümlerde daha esnek aşama sıralaması, gelişmiş tür doğrulaması ve yeni veri işleme aşamaları eklenebilir.

---

## 17. Sonuç

LogFlow projesinin Artım 3 aşamasında modüler log işleme sistemi geliştirilmiştir.

PredicateFilterStage, ProjectionStage, FileSink ve PipelineBuilder bileşenleriyle veri işleme aşamaları birbirinden ayrılmıştır.

Filtre sıralaması deneyinde aynı çıktıyı üreten farklı aşama sıralamalarının farklı işlem maliyetlerine sahip olabildiği gösterilmiştir.

Artım 4 kapsamında yapılandırma odaklı mimariye geçilmiştir.

YAML yapılandırma dosyaları, StageFactory, StageRegistry ve PipelineLoader bileşenleri eklenmiştir.

Üç farklı YAML dosyası, aynı derlenmiş Java uygulaması kullanılarak çalıştırılmıştır.

Yapılandırma hatalarının erken tespit edilmesi sağlanmış ve beş otomatik yükleyici testi başarıyla tamamlanmıştır.

Böylece LogFlow, kaynak kod değiştirilmeden farklı yapılandırmalarla çalıştırılabilen, modüler ve genişletilebilir bir log işleme sistemine dönüştürülmüştür.