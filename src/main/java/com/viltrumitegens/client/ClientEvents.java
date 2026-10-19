package com.viltrumitegens.client;

import com.viltrumitegens.ViltrumiteGens;
import com.viltrumitegens.config.GensConfig;
import com.viltrumitegens.network.ChargePacket;
import com.viltrumitegens.network.GensNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 1) Yuksek hiz oyuncunun gorus acisini (FOV) genisletir ve ekran uzamis/bozuk gorunur.
 *    Burada FOV artisini bir ust sinirla kisitliyoruz (sadece genisleme, yakinlastirma/durbun etkilenmez).
 * 2) Sol tik (saldiri tusu) basili mi degil mi, degistiginde sunucuya bildirir (sarjli yumruk icin).
 */
@Mod.EventBusSubscriber(modid = ViltrumiteGens.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {
    private static boolean lastHold = false;

    @SubscribeEvent
    public static void onFovModifier(ComputeFovModifierEvent event) {
        float max = GensConfig.FOV_MAX_MODIFIER.get().floatValue();
        if (event.getNewFovModifier() > max) {
            event.setNewFovModifier(Mth.clamp(event.getNewFovModifier(), 0.0F, max));
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();

        boolean hold = mc.player != null && mc.level != null && mc.screen == null
                && mc.options.keyAttack.isDown();

        if (hold != lastHold) {
            if (mc.getConnection() == null) {
                lastHold = false;
                return;
            }
            lastHold = hold;
            GensNetwork.CHANNEL.sendToServer(new ChargePacket(hold));
        }
    }
}
