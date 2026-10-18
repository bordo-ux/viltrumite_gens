# Viltrumite Gens

Viltrumite modunun (1.11.0) ek modu. Forge 1.20.1, Java 17.

## Özellikler
- **6 seviye** (her biri öncekinin üstüne): Asker -> Mark -> Conquest -> Nolan -> Thragg -> İmparator Argall
- **Özel genetik**: puan havuzundan (varsayılan 100) can, hasar, dayanıklılık, hız, iyileşme dağıtımı. Negatif puan = zayıflık, puan geri verir.
- **Adaptasyon**: ağır yara iyileşince kalıcı güçlenme. `kazanım(%) = 0.08 x n^2` (n = yenilenen yarım kalp). Sınırsız (config'ten sınır açılabilir).
- Seçim ekranı: ilk girişte otomatik, sonra `/gens menu`.

## Komutlar
- `/gens menu`  seçim ekranını açar
- `/gens info`  çarpanları ve adaptasyon bonuslarını gösterir
- `/gens reset` (op) seçimi sıfırlar

## Kurulum
1. Forge 1.20.1 MDK'sındaki `gradlew` / `gradle/wrapper` dosyalarını bu klasöre kopyala.
2. `./gradlew build` -> `build/libs/` altındaki jar'ı mods klasörüne at (Viltrumite ile birlikte).
3. `config/viltrumite_gens-common.toml` dosyasından dengeyi ayarla.

## Ana modla bağlama (yapılacak)
- `src/main/resources/META-INF/mods.toml` içindeki `modId="viltrumite"` satırını gerçek mod ID ile değiştir ve `mandatory=true` yap.
- `config` içinde `mainModId` değerini aynı ID yap.
- `logic/ViltrumiteBridge.java`: `isViltrumite()` şu an ana mod yüklüyse herkesi Viltrumlu sayar. Ana modun kendi kontrolüyle değiştir.
- Ana modun yetenekleri (uçuş, yumruk vb.) güç / etki alanı / tahrip için `GensData.powerMultiplier()`, `areaMultiplier()`, `destructionMultiplier()` değerlerini çarpan olarak kullanmalı.

## Seviye çarpanları
`data/GensLevel.java` içinde (can, hasar, dayanıklılık, hız, iyileşme, güç, alan, tahrip).
