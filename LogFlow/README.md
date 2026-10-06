# LogFlow – Artım 2

Bu sürümde LogFlow projesine yapılandırılmış log kayıtları ve Common Log Format (CLF) ayrıştırma desteği eklenmiştir. İşlem hattı artık yalnızca String verileri taşımak yerine LogRecord alan modelini kullanabilmektedir.

## Yapılan Değişiklikler

- Değişmez (immutable) LogRecord alan modeli eklendi.
- Common Log Format kayıtlarını ayrıştırmak için ParserStage geliştirildi.
- LogRecord nesnelerini okunabilir tek satırlık biçimde göstermek için ConsoleSink güncellendi.
- Pipeline, String girdilerini ParserStage üzerinden LogRecord nesnelerine dönüştürecek şekilde güncellendi.
- ParserStage için birim testleri oluşturuldu.
- Testlerde dosya sistemine bağımlı olmamak için CollectingEmitter test double kullanıldı.
- Pipeline için ek bir birim testi oluşturuldu.

## Testler

JUnit kullanılarak toplam 9 birim testi çalıştırılmıştır.

- ParserStageTest: 8 test
- PipelineTest: 1 test
- Başarılı test: 9
- Başarısız test: 0

ParserStage testlerinde geçerli satır, eksik alan, hatalı zaman damgası, hatalı durum kodu, boş satır, fazladan boşluk, tırnaklı user-agent ve query string içeren satır senaryoları kontrol edilmiştir.

## Kod Kapsama

Kod kapsama ölçümü JaCoCo kullanılarak gerçekleştirilmiştir.

- Line Coverage: %76,24
- Instruction Coverage: %78
- Branch Coverage: %62

## Çalıştırma

Program örnek log dosyası ile çalıştırılabilir:

```text
logflow data/access-small.log