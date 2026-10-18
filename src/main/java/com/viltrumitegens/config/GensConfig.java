package com.viltrumitegens.config;

import net.minecraftforge.common.ForgeConfigSpec;

/** Tum denge ayarlari burada. Dosya: config/viltrumite_gens-common.toml */
public class GensConfig {
    public static final ForgeConfigSpec SPEC;

    // --- Adaptasyon (yara sonrasi guclenme) ---
    public static final ForgeConfigSpec.DoubleValue ADAPT_COEFFICIENT;
    public static final ForgeConfigSpec.DoubleValue ADAPT_EXPONENT;
    public static final ForgeConfigSpec.BooleanValue ADAPT_CAP_ENABLED;
    public static final ForgeConfigSpec.DoubleValue ADAPT_CAP_PERCENT;
    public static final ForgeConfigSpec.DoubleValue ENVIRONMENT_DAMAGE_FACTOR;
    public static final ForgeConfigSpec.DoubleValue DEATH_LOSS;
    public static final ForgeConfigSpec.IntValue GAIN_COOLDOWN_TICKS;

    // --- Ozel genetik ---
    public static final ForgeConfigSpec.IntValue CUSTOM_POINT_POOL;
    public static final ForgeConfigSpec.IntValue CUSTOM_MIN_PER_GENE;
    public static final ForgeConfigSpec.DoubleValue CUSTOM_POINT_PERCENT;

    // --- Menu / ana mod ---
    public static final ForgeConfigSpec.BooleanValue AUTO_OPEN_MENU;
    public static final ForgeConfigSpec.BooleanValue ALLOW_RESELECT;
    public static final ForgeConfigSpec.BooleanValue REQUIRE_MAIN_MOD;
    public static final ForgeConfigSpec.ConfigValue<String> MAIN_MOD_ID;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("adaptation");
        ADAPT_COEFFICIENT = b.comment("Kazanim = katsayi(%) x n^us. n = yenilenen yara (yarim kalp = 1).",
                "Varsayilan 0.08: yarim kalp %0.08, 1 kalp %0.32, 5 kalp %8, 10 kalp %32.")
                .defineInRange("coefficientPercent", 0.08D, 0.0D, 1000.0D);
        ADAPT_EXPONENT = b.comment("Yara buyudukce kazanimin ne kadar hizli arttigi. 2 = kare.")
                .defineInRange("exponent", 2.0D, 1.0D, 4.0D);
        ADAPT_CAP_ENABLED = b.comment("Her istatistik icin adaptasyon ust siniri. Varsayilan kapali (sinirsiz).")
                .define("capEnabled", false);
        ADAPT_CAP_PERCENT = b.comment("capEnabled=true ise bir istatistigin adaptasyondan alabilecegi en yuksek yuzde.")
                .defineInRange("capPercent", 1000.0D, 0.0D, 1000000.0D);
        ENVIRONMENT_DAMAGE_FACTOR = b.comment("Kendine/cevreye (dusme, lav, ates vb.) verilen hasarin sayilma orani. 1 = tam, 0 = hic.")
                .defineInRange("environmentDamageFactor", 0.5D, 0.0D, 1.0D);
        DEATH_LOSS = b.comment("Oyuncu olunce adaptasyon bonusunun kaybedilen orani. 0 = hic kayip yok.")
                .defineInRange("deathLoss", 0.25D, 0.0D, 1.0D);
        GAIN_COOLDOWN_TICKS = b.comment("Iki adaptasyon kazanimi arasindaki en kisa sure (tick, 20 = 1 sn).")
                .defineInRange("gainCooldownTicks", 100, 0, 72000);
        b.pop();

        b.push("custom_genetics");
        CUSTOM_POINT_POOL = b.comment("Ozel genetikte dagitilabilecek toplam puan.")
                .defineInRange("pointPool", 100, 1, 10000);
        CUSTOM_MIN_PER_GENE = b.comment("Bir ozellige verilebilecek en dusuk puan (negatif = zayiflik, puan geri kazandirir).")
                .defineInRange("minPointsPerGene", -20, -1000, 0);
        CUSTOM_POINT_PERCENT = b.comment("1 puanin verdigi bonus yuzdesi.")
                .defineInRange("percentPerPoint", 5.0D, 0.0D, 1000.0D);
        b.pop();

        b.push("menu");
        AUTO_OPEN_MENU = b.comment("Secim yapmamis oyuncuya girisinde menuyu otomatik ac.")
                .define("autoOpen", true);
        ALLOW_RESELECT = b.comment("Oyuncular secimden sonra menuyu tekrar acip degistirebilsin mi.")
                .define("allowReselect", false);
        REQUIRE_MAIN_MOD = b.comment("true ise menu sadece ana mod yukluyken otomatik acilir.")
                .define("requireMainMod", true);
        MAIN_MOD_ID = b.comment("Ana Viltrumite modunun mod ID'si. Gercek ID'yi mods.toml'dan kontrol et.")
                .define("mainModId", "viltrumite");
        b.pop();

        SPEC = b.build();
    }
}
