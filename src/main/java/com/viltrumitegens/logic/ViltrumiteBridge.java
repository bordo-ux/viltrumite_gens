package com.viltrumitegens.logic;

import com.viltrumitegens.config.GensConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;

/**
 * Ana Viltrumite modu ile baglanti noktasi.
 *
 * TODO (ana modun jar'i / kaynagi gelince):
 *  - isViltrumite(): oyuncunun gercekten Viltrumlu olup olmadigini ana modun kendi API'siyle kontrol et.
 *  - Ana modun yetenekleri (ucus, yumruk, vb.) gucunu/etki alanini/tahrip gucunu
 *    GensData.powerMultiplier(), areaMultiplier(), destructionMultiplier() ile carpsin.
 */
public final class ViltrumiteBridge {
    private ViltrumiteBridge() {}

    public static boolean isMainModLoaded() {
        return ModList.get().isLoaded(GensConfig.MAIN_MOD_ID.get());
    }

    /** Su an: ana mod yukluyse her oyuncu Viltrumlu sayilir (gecici). */
    public static boolean isViltrumite(Player player) {
        return isMainModLoaded();
    }

    /** Girişte seçim menüsü açılmalı mı? */
    public static boolean shouldOfferMenu(Player player) {
        if (GensConfig.REQUIRE_MAIN_MOD.get() && !isMainModLoaded()) return false;
        return isViltrumite(player);
    }
}
