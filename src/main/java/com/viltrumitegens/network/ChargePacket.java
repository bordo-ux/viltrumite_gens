package com.viltrumitegens.network;

import com.viltrumitegens.logic.Charge;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Istemci -> sunucu: sol tik (saldiri tusu) basili mi? Sadece degisince gonderilir. */
public class ChargePacket {
    public final boolean holding;

    public ChargePacket(boolean holding) {
        this.holding = holding;
    }

    public static void encode(ChargePacket p, FriendlyByteBuf buf) {
        buf.writeBoolean(p.holding);
    }

    public static ChargePacket decode(FriendlyByteBuf buf) {
        return new ChargePacket(buf.readBoolean());
    }

    public static void handle(ChargePacket p, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            if (c.getSender() != null) {
                Charge.setHolding(c.getSender(), p.holding);
            }
        });
        c.setPacketHandled(true);
    }
}
