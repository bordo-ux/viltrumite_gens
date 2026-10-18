package com.viltrumitegens.logic;

import com.viltrumitegens.ViltrumiteGens;
import com.viltrumitegens.data.GensData;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** Basarim tetikleyicileri. Basarimlar data/viltrumite_gens/advancements altinda; kodla verilenlerin kriteri "done". */
public final class GensAdvancements {
    private GensAdvancements() {}

    public static void grant(ServerPlayer player, String id) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        Advancement adv = server.getAdvancements().getAdvancement(new ResourceLocation(ViltrumiteGens.MODID, id));
        if (adv == null) return;
        player.getAdvancements().award(adv, "done");
    }

    /** Seviye ya da ozel genetik secilince. Ust seviyeyi secen, alttakilerin basarimini da alir. */
    public static void onChoice(ServerPlayer player, GensData data) {
        grant(player, "chosen");
        if (data.isCustom()) {
            grant(player, "custom");
            return;
        }
        String[] ids = {"", "", "mark", "conquest", "nolan", "thragg", "argall"};
        int lvl = data.level().id;
        for (int i = 2; i <= lvl && i < ids.length; i++) {
            grant(player, ids[i]);
        }
    }

    /** Bir adaptasyon kazanildiginda. n = iyilesen yara (yarim kalp = 1). */
    public static void onAdaptation(ServerPlayer player, GensData data, float n) {
        grant(player, "first_adapt");
        if (data.adaptCount() >= 5) grant(player, "five_adapt");
        if (n >= 20f) grant(player, "big_adapt");
        if (data.totalAdaptation() >= 100f) grant(player, "double");
    }
}
