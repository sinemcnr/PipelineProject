# LogFlow
### Java Tabanlı Log İşleme Sistemi

LogFlow, log dosyalarını okumak, ayrıştırmak, filtrelemek ve işlenen verileri farklı çıktılara aktarmak amacıyla geliştirilmiş Java tabanlı bir uygulamadır.

Proje, Yazılım Mimarisi dersi kapsamında **artımlı geliştirme yaklaşımıyla** hazırlanmıştır.

Artım 4 ile uygulamaya YAML tabanlı yapılandırma desteği eklenmiş, işlem hatlarının Java kaynak kodu değiştirilmeden yapılandırılabilmesi sağlanmıştır.

---

## 1. Projenin Amacı

LogFlow projesinin temel amacı, log dosyalarının modüler, yeniden kullanılabilir ve genişletilebilir bir mimariyle işlenmesini sağlamaktır.

Uygulama sayesinde:

- Log dosyaları satır satır okunabilir.
- Log satırları yapılandırılmış nesnelere dönüştürülebilir.
- Geçersiz log kayıtları tespit edilebilir.
- Kayıtlar belirli koşullara göre filtrelenebilir.
- İstenen veri alanları seçilebilir.
- Sonuçlar konsola veya dosyaya aktarılabilir.
- Veri işleme aşamaları yeniden kullanılabilir.
- Pipeline yapıları YAML dosyalarıyla tanımlanabilir.
- Yapılandırma hataları işlem başlamadan tespit edilebilir.
- Aynı Java uygulaması farklı yapılandırmalarla çalıştırılabilir.

---

## 2. Kullanılan Teknolojiler

| Teknoloji | Kullanım Amacı |
|---|---|
| Java | Uygulama geliştirme |
| Java Records | Log verilerinin modellenmesi |
| Java Generics | Tür güvenliği |
| Java Streams ve Predicate | Veri filtreleme |
| SnakeYAML 2.2 | YAML dosyalarını ayrıştırma |
| JUnit 5 | Birim ve entegrasyon testleri |
| JaCoCo | Kod kapsama analizi |
| YAML | Pipeline yapılandırması |
| Git | Sürüm kontrolü |
| GitHub | Proje paylaşımı |
| Visual Studio | Geliştirme ortamı |
| PowerShell | Derleme ve çalıştırma |

Uygulama Java 17 veya üzeri sürümle çalıştırılacak şekilde hazırlanmıştır.

---

## 3. Mimari Yapı

LogFlow, **Source - Stage - Sink** mimarisini kullanır.

### 3.1. Temel Veri Akışı

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

### 3.2. Artım 4 Yapılandırma Mimarisi

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

### 3.3. Source

Verinin sisteme girişini sağlar.

**FileLineSource:** Log dosyasını satır satır okur.

### 3.4. Stage

Verinin işlenmesinden sorumludur.

**ParserStage:** Ham log satırlarını LogRecord nesnelerine dönüştürür.

**PredicateFilterStage:** Belirtilen koşullara uyan kayıtları sonraki aşamaya aktarır.

**ProjectionStage:** LogRecord nesnesinden yalnızca istenen alanları seçer.

### 3.5. Sink

İşlenen verilerin çıktı ortamına aktarılmasını sağlar.

**ConsoleSink:** Sonuçları konsolda gösterir.

**FileSink:** Sonuçları belirtilen dosyaya yazar.

### 3.6. Yapılandırma Bileşenleri

**StageFactory:** Yapılandırma parametrelerinden Stage nesnesi oluşturur.

**StageRegistry:** Aşama türlerini ilgili fabrika fonksiyonlarıyla eşleştirir.

**PipelineLoader:** YAML dosyasını okur, parametreleri doğrular ve Pipeline nesnesini oluşturur.

---

## 4. Proje Geliştirme Aşamaları

### Artım 1 - Temel Pipeline Mimarisi

İlk aşamada temel veri işleme altyapısı oluşturulmuştur.

Geliştirilen bileşenler:

- Source
- Stage
- Sink
- Emitter
- Pipeline
- FileLineSource
- ConsoleSink

Bu aşamada Source - Stage - Sink mimarisi uygulanmıştır.

### Artım 2 - Log Ayrıştırma ve Testler

İkinci aşamada log satırlarının yapılandırılmış verilere dönüştürülmesi sağlanmıştır.

Eklenen özellikler:

- LogRecord veri modeli
- ParserStage
- CLF formatında log ayrıştırma
- Geçersiz satır sayımı
- JUnit birim testleri
- Pipeline testleri
- JaCoCo kod kapsama analizi

Artım 2 çalışmasında JaCoCo satır kapsama oranı **%76,24** olarak ölçülmüştür.

### Artım 3 - Filtreleme ve Projeksiyon

Üçüncü aşamada pipeline mimarisi yeni bileşenlerle genişletilmiştir.

Eklenen özellikler:

- PredicateFilterStage
- ProjectionStage
- FileSink
- PipelineBuilder
- Komut satırı parametreleri
- Filtre sıralaması deneyi
- Yeni birim ve entegrasyon testleri
- ARCHITECTURE.md dokümantasyonu

### Artım 4 - YAML Tabanlı Yapılandırma Sistemi

Dördüncü aşamada işlem hattının tanımı Java kodundan ayrılarak YAML dosyalarına taşınmıştır.

Eklenen özellikler:

- YAML yapılandırma desteği
- SnakeYAML kütüphanesi
- StageFactory
- StageRegistry
- PipelineLoader
- `--config` komut satırı parametresi
- Dinamik pipeline oluşturma
- Parametre doğrulama
- Fail Fast hata yönetimi
- Üç farklı YAML yapılandırması
- Beş otomatik yapılandırma testi

Artım 4 sayesinde aynı derlenmiş Java uygulaması, farklı YAML dosyalarıyla çalıştırılabilir.

---

## 5. Proje Klasör Yapısı

```text
LogFlow/
|
|-- src/
|   |-- Main.java
|   |-- Source.java
|   |-- Stage.java
|   |-- Sink.java
|   |-- Emitter.java
|   |-- Pipeline.java
|   |-- PipelineBuilder.java
|   |-- FileLineSource.java
|   |-- ParserStage.java
|   |-- PredicateFilterStage.java
|   |-- ProjectionStage.java
|   |-- ConsoleSink.java
|   |-- FileSink.java
|   |-- LogRecord.java
|   |-- FilterOrderExperiment.java
|   |-- StageException.java
|   |-- StageFactory.java
|   |-- StageRegistry.java
|   |-- PipelineLoader.java
|
|-- test/
|   |-- ParserStageTest.java
|   |-- PipelineTest.java
|   |-- PredicateFilterStageTest.java
|   |-- ProjectionStageTest.java
|   |-- FileSinkTest.java
|   |-- PipelineIntegrationTest.java
|   |-- PipelineLoaderTest.java
|
|-- configs/
|   |-- error-report.yaml
|   |-- api-traffic.yaml
|   |-- bandwidth-by-client.yaml
|
|-- data/
|   |-- access-small.log
|   |-- filter-experiment.log
|
|-- lib/
|   |-- junit-platform-console-standalone-6.1.3.jar
|   |-- snakeyaml-2.2.jar
|
|-- output/
|   |-- errors.txt
|   |-- api-traffic.txt
|   |-- bandwidth-by-client.txt
|
|-- out/
|
|-- README.md
|-- CHANGELOG.md
|-- ARCHITECTURE.md
|-- .gitignore
```

**Not:** `out` klasörü derleme sırasında oluşturulur. `output` klasöründeki dosyalar ise uygulama çalıştırıldığında üretilir.

Test amacıyla oluşturulan `invalid-stage.yaml`, `missing-param.yaml`, `wrong-type.yaml` ve `empty-pipeline.yaml` dosyaları isteğe bağlı olarak saklanabilir.

---

## 6. PipelineBuilder Kullanımı

Artım 3 ile eklenen PipelineBuilder, işlem hattı aşamalarının Fluent API yaklaşımıyla oluşturulmasını sağlar.

Örnek Java kodu:

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

Bu yaklaşım sayesinde veri işleme aşamaları okunabilir ve genişletilebilir bir şekilde birleştirilir.

Artım 4 kapsamında PipelineBuilder, PipelineLoader tarafından dinamik olarak kullanılmaktadır.

---

## 7. Kurulum ve Gereksinimler

### 7.1. Gereksinimler

- Java JDK 17 veya üzeri
- PowerShell veya uyumlu terminal
- SnakeYAML 2.2
- JUnit testleri için JUnit Platform Console Standalone

Java sürümünü kontrol etmek için:

```powershell
java -version
javac -version
```

### 7.2. SnakeYAML Kurulumu

Projenin ana klasöründe terminal açılır.

Gerekli klasörler oluşturulur:

```powershell
New-Item -ItemType Directory -Force -Path lib,out,output
```

SnakeYAML kütüphanesi indirilir:

```powershell
Invoke-WebRequest -Uri "https://repo.maven.apache.org/maven2/org/yaml/snakeyaml/2.2/snakeyaml-2.2.jar" -OutFile "lib\snakeyaml-2.2.jar"
```

Bu işlemden sonra `lib` klasöründe `snakeyaml-2.2.jar` dosyası bulunmalıdır.

---

## 8. Uygulamanın Derlenmesi

Projenin ana klasöründe terminal açılır.

Artım 4 ile SnakeYAML bağımlılığı eklendiğinden derleme komutu güncellenmiştir.

```powershell
javac -cp "lib/*" -d out src\*.java
```

Bu komut Java kaynak dosyalarını derler ve `out` klasörüne kaydeder.

Derleme başarılı olduğunda uygulama çalıştırılabilir.

---

## 9. Uygulamanın Çalıştırılması

### 9.1. Temel Kullanım

```powershell
java -cp "out;lib/*" Main data\access-small.log
```

### 9.2. Durum Koduna Göre Filtreleme

```powershell
java -cp "out;lib/*" Main data\access-small.log --min-status 400
```

### 9.3. URL Yoluna Göre Filtreleme

```powershell
java -cp "out;lib/*" Main data\access-small.log --path-prefix /api
```

### 9.4. Alan Seçimi

```powershell
java -cp "out;lib/*" Main data\access-small.log --fields clientIp,status,path
```

### 9.5. Tüm Parametrelerle Kullanım

```powershell
java -cp "out;lib/*" Main data\access-small.log --min-status 400 --path-prefix /api --fields clientIp,status,path --output output.log
```

### 9.6. YAML Yapılandırmasıyla Çalıştırma

Artım 4 kapsamında eklenen `--config` parametresi kullanılır.

```powershell
java -cp "out;lib/*" Main --config configs/error-report.yaml
```

Bu komut belirtilen YAML dosyasını okuyarak işlem hattını oluşturur ve çalıştırır.

---

## 10. Komut Satırı Parametreleri

| Parametre | Açıklama |
|---|---|
| `--min-status` | Minimum HTTP durum kodu |
| `--path-prefix` | URL yolu ön eki |
| `--fields` | Çıktıya aktarılacak alanlar |
| `--output` | Çıktı dosyasının adı |
| `--config` | YAML yapılandırma dosyasının yolu |

### Örnek

```powershell
java -cp "out;lib/*" Main --config configs/error-report.yaml
```

YAML dosyası değiştirilerek farklı işlem hatları çalıştırılabilir.

---

## 11. YAML Yapılandırma Sistemi

Artım 4 kapsamında işlem hatları YAML dosyaları üzerinden tanımlanabilir.

### 11.1. YAML Yapılandırma Yapısı

Temel yapı:

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

### 11.2. Desteklenen Aşamalar

| Aşama | Görevi |
|---|---|
| parse | Log satırlarını ayrıştırma |
| filter | Belirli koşullara göre filtreleme |
| project | Seçilen alanları çıktı olarak üretme |

### 11.3. Desteklenen Filtreler

Mevcut YAML yükleyicisi aşağıdaki filtre işlemlerini destekler:

**Status filtresi:**

```yaml
field: status
op: ">="
value: 400
```

**Path filtresi:**

```yaml
field: path
op: startsWith
value: /api
```

### 11.4. Yapılandırma Doğrulama

PipelineLoader şu durumları kontrol eder:

- Eksik zorunlu parametre
- Yanlış parametre türü
- Bilinmeyen aşama türü
- Boş aşama listesi
- Desteklenmeyen kaynak türü
- Desteklenmeyen çıktı türü
- Desteklenmeyen filtre işlemi
- Geçersiz aşama sıralaması

Geçersiz yapılandırmalar, veri işleme başlamadan reddedilir.

Bu yaklaşım **Fail Fast** olarak adlandırılır.

---

## 12. Hazır YAML Yapılandırmaları

Artım 4 kapsamında üç farklı yapılandırma hazırlanmıştır.

### 12.1. Hata Raporu

Dosya:

`configs/error-report.yaml`

HTTP durum kodu 400 ve üzerindeki kayıtları seçer.

Çalıştırma:

```powershell
java -cp "out;lib/*" Main --config configs/error-report.yaml
```

Çıktıyı görüntüleme:

```powershell
Get-Content output/errors.txt
```

Doğrulanan örnek:

```text
{timestamp=2026-09-29T10:15:05Z, clientIp=10.0.0.5, status=403, path=/admin}
```

### 12.2. API Trafiği Raporu

Dosya:

`configs/api-traffic.yaml`

URL yolu `/api` ile başlayan kayıtları seçer.

Çalıştırma:

```powershell
java -cp "out;lib/*" Main --config configs/api-traffic.yaml
```

Çıktıyı görüntüleme:

```powershell
Get-Content output/api-traffic.txt
```

Mevcut küçük veri setinde `/api` ile başlayan kayıt bulunmadığından çıktı boş oluşmuştur.

### 12.3. İstemci Bazında Bant Genişliği Verileri

Dosya:

`configs/bandwidth-by-client.yaml`

Her log kaydından istemci IP adresini ve aktarılan bayt miktarını seçer.

Çalıştırma:

```powershell
java -cp "out;lib/*" Main --config configs/bandwidth-by-client.yaml
```

Çıktıyı görüntüleme:

```powershell
Get-Content output/bandwidth-by-client.txt
```

Doğrulanan çıktı:

```text
{clientIp=127.0.0.1, bytes=512}
{clientIp=192.168.1.10, bytes=245}
{clientIp=10.0.0.5, bytes=128}
{clientIp=172.16.0.8, bytes=1024}
{clientIp=192.168.1.15, bytes=768}
```

Bu yapılandırma IP ve bayt alanlarını çıkarır. IP adresine göre toplam bayt hesabı yapmaz.

### 12.4. Yapılandırmaların Karşılaştırılması

| Yapılandırma | İşlem | Çıktı kaydı |
|---|---|---:|
| error-report | Status filtreleme | 1 |
| api-traffic | Path filtreleme | 0 |
| bandwidth-by-client | Alan seçimi | 5 |

Üç yapılandırma da aynı derlenmiş Java uygulamasıyla çalıştırılmıştır.

---

## 13. Birim ve Entegrasyon Testleri

LogFlow projesinde Artım 1, 2 ve 3 kapsamında JUnit testleri geliştirilmiştir.

### 13.1. JUnit Testlerini Derleme

```powershell
javac -cp "lib/*;out" -d out test\*.java
```

### 13.2. JUnit Testlerini Çalıştırma

```powershell
java -jar lib\junit-platform-console-standalone-6.1.3.jar execute --class-path "out;lib/snakeyaml-2.2.jar" --scan-class-path
```

### 13.3. Test Dosyaları

| Test sınıfı | Test sayısı |
|---|---:|
| ParserStageTest | 8 |
| PipelineTest | 1 |
| PredicateFilterStageTest | 7 |
| ProjectionStageTest | 8 |
| FileSinkTest | 7 |
| PipelineIntegrationTest | 1 |
| Toplam | 32 |

Önceki JUnit çalıştırmasında:

- Bulunan test: 32
- Başlatılan test: 32
- Başarılı test: 32
- Başarısız test: 0

Bu sonuç Artım 3 kapsamında doğrulanmıştır.

---

## 14. Artım 4 - PipelineLoader Testleri

Artım 4 kapsamında `PipelineLoaderTest.java` sınıfı geliştirilmiştir.

Bu sınıf, YAML yapılandırma sisteminin doğru ve hatalı durumlarını test eder.

### 14.1. Test Senaryoları

| Test | Beklenen davranış |
|---|---|
| Geçerli yapılandırma | Pipeline oluşturulmalı ve çalışmalı |
| Bilinmeyen aşama | Yapılandırma reddedilmeli |
| Eksik parametre | Hata mesajı oluşturulmalı |
| Yanlış parametre türü | Geçersiz veri türü reddedilmeli |
| Boş pipeline | Boş aşama listesi reddedilmeli |

### 14.2. Testleri Derleme

```powershell
javac -cp "out;lib/*" -d out test\PipelineLoaderTest.java
```

### 14.3. Testleri Çalıştırma

```powershell
java -cp "out;lib/*" PipelineLoaderTest
```

### 14.4. Doğrulanan Test Sonucu

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

Beş testin tamamı başarıyla sonuçlanmıştır.

Bu testler bağımsız Java test çalıştırıcısıyla gerçekleştirilmiştir ve JUnit gerektirmemektedir.

---

## 15. Hata Yönetimi

Artım 4 kapsamında hatalı yapılandırmalar işlem başlamadan kontrol edilir.

### 15.1. Bilinmeyen Aşama Türü

```text
Yapilandirma/parametre hatasi: Bilinmeyen asama turu: unknown
```

### 15.2. Eksik Parametre

```text
Yapilandirma/parametre hatasi: Zorunlu parametre eksik: value
```

### 15.3. Yanlış Parametre Türü

```text
Yapilandirma/parametre hatasi: status filtresi icin value tam sayi olmali.
```

### 15.4. Boş Pipeline

```text
Yapilandirma/parametre hatasi: Pipeline bos olamaz.
```

Bu hata mesajları terminal üzerinde doğrulanmıştır.

Hatalı yapılandırmalar için işlem hattı başlatılmaz.

---

## 16. Filtre Sıralaması Deneyi

Artım 3 kapsamında filtre sıralamasının işlem maliyetine etkisi incelenmiştir.

Karşılaştırılan sıralamalar:

1. Status Filter → Path Filter
2. Path Filter → Status Filter

Deney için 200 kayıtlık log dosyası kullanılmıştır.

### 16.1. Deneyi Çalıştırma

```powershell
java -cp "out;lib/*" FilterOrderExperiment data\filter-experiment.log
```

### 16.2. Deney Sonuçları

| Ölçüm | Status → Path | Path → Status |
|---|---:|---:|
| Girdi kaydı | 200 | 200 |
| Çıktı kaydı | 20 | 20 |
| Status kontrolü | 200 | 100 |
| Path kontrolü | 40 | 200 |
| Toplam kontrol | 240 | 300 |
| Aynı çıktı | Evet | Evet |

### 16.3. Değerlendirme

İki filtre sıralaması da aynı 20 log kaydını üretmiştir.

Status filtresinin önce kullanıldığı sıralama 240 kontrol yapmıştır.

Path filtresinin önce kullanıldığı sıralama 300 kontrol yapmıştır.

Birinci sıralama toplam filtre kontrol sayısını %20 azaltmıştır.

Tek çalıştırmadaki süre ölçümleri kesin performans üstünlüğü göstermediği için filtre kontrol sayıları temel karşılaştırma ölçütü olarak değerlendirilmiştir.

Ayrıntılı deney sonuçları `ARCHITECTURE.md` dosyasında yer almaktadır.

---

## 17. Mimari Tasarım İlkeleri

### 17.1. Single Responsibility Principle

Her bileşen belirli bir işlemden sorumludur.

ParserStage ayrıştırma, PredicateFilterStage filtreleme, ProjectionStage alan seçimi ve FileSink dosyaya yazma işlemini gerçekleştirir.

StageRegistry kayıt yönetimini, PipelineLoader yapılandırma yüklemeyi gerçekleştirir.

### 17.2. Open/Closed Principle

Yeni Stage sınıfları ve fabrika fonksiyonları eklenerek sistem genişletilebilir.

Mevcut Pipeline sınıfının her yeni aşama için değiştirilmesi gerekmez.

### 17.3. Reusability

Filtreleme ve projeksiyon aşamaları farklı parametrelerle yeniden kullanılabilir.

### 17.4. Type Safety

PipelineBuilder, Java kaynak kodunda generic tür kontrolüne yardımcı olur.

YAML üzerinden oluşturulan dinamik pipeline yapısında tür güvenliği temel aşama sıralaması kontrolleriyle sınırlıdır.

### 17.5. Resource Management

FileSink, `open()` ve `close()` metotlarıyla dosya kaynaklarını yönetir.

Pipeline, aşamaların yaşam döngüsünü kontrol eder.

### 17.6. Separation of Concerns

YAML dosyaları işlem hattının tanımını içerir.

Java sınıfları veri işleme davranışını gerçekleştirir.

Bu sayede yapılandırma ve uygulama mantığı birbirinden ayrılmıştır.

---

## 18. Dokümantasyon

Proje dokümantasyonu:

| Dosya | Açıklama |
|---|---|
| README.md | Kurulum, kullanım ve genel bilgiler |
| CHANGELOG.md | Sürümlere göre değişiklikler |
| ARCHITECTURE.md | Mimari kararlar, yapılandırma sistemi ve deney sonuçları |

---

## 19. Sürümler

| Sürüm | Açıklama |
|---|---|
| v1 | Temel pipeline mimarisi |
| v2 | LogRecord, ParserStage ve testler |
| v3 | Filtreleme, projeksiyon, FileSink ve PipelineBuilder |
| v4 | YAML yapılandırması, StageRegistry, StageFactory, PipelineLoader ve testler |

Artım 4 geliştirmeleri tamamlanmış ve beş otomatik yapılandırma testi başarıyla çalıştırılmıştır.

GitHub üzerinde `v4` etiketi oluşturulması teslim sürecinde gerçekleştirilecektir.

---

## 20. Bilinen Sınırlamalar

Mevcut uygulamanın bazı sınırlamaları bulunmaktadır:

- YAML yapılandırmasında yalnızca kayıtlı aşama türleri kullanılabilir.
- İlk aşama parse olmak zorundadır.
- Project aşamasından sonra başka aşama eklenemez.
- Dinamik aşamalar için tam tür güvenliği kontrolü bulunmamaktadır.
- API filtreleme senaryosu eşleşen test verileriyle ayrıca doğrulanmalıdır.
- Bant genişliği yapılandırması toplam bayt hesabı yapmamaktadır.
- Yapılandırma doğrulama sistemi yeni parametre ve aşama türleriyle genişletilebilir.

---

## 21. Sonuç

LogFlow, artımlı geliştirme yaklaşımıyla oluşturulmuş modüler bir log işleme sistemidir.

Artım 1'de temel pipeline altyapısı geliştirilmiştir.

Artım 2'de log ayrıştırma ve test altyapısı eklenmiştir.

Artım 3'te filtreleme, projeksiyon, dosyaya yazma ve Fluent API özellikleri geliştirilmiştir.

Artım 4'te YAML tabanlı yapılandırma sistemi, StageFactory, StageRegistry ve PipelineLoader bileşenleri eklenmiştir.

Üç farklı YAML yapılandırması aynı derlenmiş Java uygulamasıyla çalıştırılmıştır.

Beş otomatik yapılandırma testi başarıyla tamamlanmıştır.

Böylece LogFlow, farklı işlem hatlarının kaynak kod değiştirilmeden tanımlanabildiği, modüler, yeniden kullanılabilir ve genişletilebilir bir log işleme sistemine dönüştürülmüştür.