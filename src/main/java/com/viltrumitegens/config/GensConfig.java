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
    public static final ForgeConfigSpec.DoubleValue ADAPT_HEALTH_SHARE;

    // --- Hareket / gorus acisi ---
    public static final ForgeConfigSpec.DoubleValue SPEED_MULTIPLIER_CAP;
    public static final ForgeConfigSpec.DoubleValue FOV_MAX_MODIFIER;

    // --- Sok dalgasi / tahrip alani ---
    public static final ForgeConfigSpec.BooleanValue SHOCK_ENABLED;
    public static final ForgeConfigSpec.IntValue SHOCK_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.DoubleValue SHOCK_ENTITY_RADIUS;
    public static final ForgeConfigSpec.DoubleValue SHOCK_BLOCK_RADIUS;
    public static final ForgeConfigSpec.DoubleValue SHOCK_MAX_RADIUS;
    public static final ForgeConfigSpec.DoubleValue SHOCK_MIN_AREA;
    public static final ForgeConfigSpec.DoubleValue SHOCK_BLOCK_THRESHOLD;
    public static final ForgeConfigSpec.DoubleValue SHOCK_HARDNESS_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue SHOCK_MAX_BLOCKS;
    public static final ForgeConfigSpec.BooleanValue SHOCK_DROPS;
    public static final ForgeConfigSpec.BooleanValue SHOCK_HURT_PLAYERS;
    public static final ForgeConfigSpec.BooleanValue CHARGE_EMPTY_HAND;
    public static final ForgeConfigSpec.IntValue CHARGE_MIN_TICKS;
    public static final ForgeConfigSpec.IntValue CHARGE_FULL_TICKS;
    public static final ForgeConfigSpec.DoubleValue CHARGE_REACH;
    public static final ForgeConfigSpec.DoubleValue CHARGE_DAMAGE_SCALE;
    public static final ForgeConfigSpec.BooleanValue SCALE_EXPLOSIONS;

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
        ADAPT_HEALTH_SHARE = b.comment("Her adaptasyon kazaniminin ne kadari hasar turune bakilmaksizin CAN'a gider (0-1).",
                "Kalani hasar turune gore dagitilir.")
                .defineInRange("healthShare", 0.4D, 0.0D, 1.0D);
        b.pop();

        b.push("movement");
        SPEED_MULTIPLIER_CAP = b.comment("Hiz carpaninin ust siniri (seviye + ozel genetik + adaptasyon toplami).",
                "Cok yuksek hiz ekran bozulmasina ve chunk yukleme sorunlarina yol acar.")
                .defineInRange("speedMultiplierCap", 2.0D, 1.0D, 20.0D);
        FOV_MAX_MODIFIER = b.comment("Hizdan dolayi gorus acisinin (FOV) genisleyebilecegi en yuksek carpan (istemci).",
                "Vanilla kosu yaklasik 1.1-1.15. Ekran uzuyorsa dusur (1.0 = hiz FOV'u hic degistirmez), kapatmak icin 5 yap.")
                .defineInRange("fovMaxModifier", 1.08D, 1.0D, 5.0D);
        b.pop();

        b.push("shockwave");
        SHOCK_ENABLED = b.comment("Sol tiki basili tutup birakinca yapilan sarjli sok dalgasi yumrugu (Conquest ve ustu).")
                .define("enabled", true);
        SHOCK_COOLDOWN_TICKS = b.comment("Iki sok dalgasi arasindaki en kisa sure (tick).")
                .defineInRange("cooldownTicks", 20, 0, 1200);
        SHOCK_ENTITY_RADIUS = b.comment("Canlilara etki yaricapi = bu deger x seviyenin alan (area) carpani.")
                .defineInRange("entityRadius", 2.0D, 0.0D, 64.0D);
        SHOCK_BLOCK_RADIUS = b.comment("Blok kirma yaricapi = bu deger x seviyenin tahrip (destruction) carpani.")
                .defineInRange("blockRadius", 1.5D, 0.0D, 64.0D);
        SHOCK_MAX_RADIUS = b.comment("Hicbir yaricap bunu gecemez (sunucuyu korur).")
                .defineInRange("maxRadius", 16.0D, 1.0D, 64.0D);
        SHOCK_MIN_AREA = b.comment("Canlilara sok dalgasi icin gereken en dusuk alan carpani (Conquest ve ustu).")
                .defineInRange("minAreaMultiplier", 1.2D, 0.0D, 100.0D);
        SHOCK_BLOCK_THRESHOLD = b.comment("Blok kirmak icin gereken en dusuk tahrip carpani (Nolan ve ustu). Kapatmak icin 1000 yap.")
                .defineInRange("blockDestructionThreshold", 1.2D, 0.0D, 1000.0D);
        SHOCK_HARDNESS_PER_LEVEL = b.comment("Kirilabilen en sert blok = bu deger x tahrip carpani. Tas 1.5, obsidyen 50, bedrock kirilmaz.")
                .defineInRange("hardnessPerDestruction", 1.5D, 0.0D, 100.0D);
        SHOCK_MAX_BLOCKS = b.comment("Tek bir dalgada kirilabilecek en cok blok (lag onleme).")
                .defineInRange("maxBlocks", 600, 0, 20000);
        SHOCK_DROPS = b.comment("Kirilan bloklar esya dusursun mu? Kapali olmasi lag'i azaltir.")
                .define("blockDrops", false);
        SHOCK_HURT_PLAYERS = b.comment("Sok dalgasi diger oyunculara da hasar versin mi (PvP).")
                .define("hurtPlayers", false);
        CHARGE_EMPTY_HAND = b.comment("Sarjli yumruk sadece bos elle calissin (alet tutarken kazi yaparken yanlislikla patlamasin).")
                .define("chargeRequiresEmptyHand", true);
        CHARGE_MIN_TICKS = b.comment("Sarjli yumruk icin sol tiki en az kac tick basili tutmak gerekir (20 = 1 sn).")
                .defineInRange("chargeMinTicks", 10, 1, 400);
        CHARGE_FULL_TICKS = b.comment("Tam sarj icin gereken sure (tick). Tam sarjda yaricap ve hasar en yuksektir.")
                .defineInRange("chargeFullTicks", 40, 2, 400);
        CHARGE_REACH = b.comment("Sarjli yumrugun ulasabildigi en uzak nokta (blok).")
                .defineInRange("chargeReach", 6.0D, 1.0D, 32.0D);
        CHARGE_DAMAGE_SCALE = b.comment("Sarjli yumrugun hasar carpani. 1 = varsayilan.")
                .defineInRange("chargeDamageScale", 1.0D, 0.0D, 100.0D);
        SCALE_EXPLOSIONS = b.comment("Oyuncunun yol actigi patlamalar (ana mod patlama kullaniyorsa) seviyeye gore buyusun.")
                .define("scaleExplosions", true);
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
                .define("requireMainMod", false);
        MAIN_MOD_ID = b.comment("Ana Viltrumite modunun mod ID'si. Gercek ID'yi mods.toml'dan kontrol et.")
                .define("mainModId", "viltrumite");
        b.pop();

        SPEC = b.build();
    }
}
