package com.viltrumitegens.logic;

import com.viltrumitegens.ViltrumiteGens;
import com.viltrumitegens.config.GensConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;

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

    /**
     * Ana mod yuklu mu? Once config'teki ID'ye bakar; bulamazsa ID'sinde "viltrum" gecen
     * (kendimiz haric) herhangi bir modu ana mod sayar. Boylece ID'yi bilmesek de calisir.
     */
    public static boolean isMainModLoaded() {
        ModList ml = ModList.get();
        if (ml.isLoaded(GensConfig.MAIN_MOD_ID.get())) return true;
        return ml.getMods().stream()
                .map(IModInfo::getModId)
                .anyMatch(id -> id.toLowerCase(java.util.Locale.ROOT).contains("viltrum")
                        && !id.equals(ViltrumiteGens.MODID));
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
