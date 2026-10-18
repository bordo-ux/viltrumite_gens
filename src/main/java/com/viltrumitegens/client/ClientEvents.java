package com.viltrumitegens.client;

import com.viltrumitegens.ViltrumiteGens;
import com.viltrumitegens.config.GensConfig;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Yuksek hiz oyuncunun gorus acisini (FOV) genisletir ve ekran uzamis/bozuk gorunur.
 * Burada FOV artisini bir ust sinirla kisitliyoruz (sadece genisleme, yakinlastirma/durbun etkilenmez).
 */
@Mod.EventBusSubscriber(modid = ViltrumiteGens.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

    @SubscribeEvent
    public static void onFovModifier(ComputeFovModifierEvent event) {
        float max = GensConfig.FOV_MAX_MODIFIER.get().floatValue();
        if (event.getNewFovModifier() > max) {
            event.setNewFovModifier(Mth.clamp(event.getNewFovModifier(), 0.0F, max));
        }
    }
}
