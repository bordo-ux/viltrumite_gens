package com.viltrumitegens.network;

import com.viltrumitegens.client.ClientHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Sunucu -> istemci: secim menusunu ac. */
public class OpenMenuPacket {
    public final int pool;
    public final int minPerGene;
    public final float percentPerPoint;

    public OpenMenuPacket(int pool, int minPerGene, float percentPerPoint) {
        this.pool = pool;
        this.minPerGene = minPerGene;
        this.percentPerPoint = percentPerPoint;
    }

    public static void encode(OpenMenuPacket p, FriendlyByteBuf buf) {
        buf.writeInt(p.pool);
        buf.writeInt(p.minPerGene);
        buf.writeFloat(p.percentPerPoint);
    }

    public static OpenMenuPacket decode(FriendlyByteBuf buf) {
        return new OpenMenuPacket(buf.readInt(), buf.readInt(), buf.readFloat());
    }

    public static void handle(OpenMenuPacket p, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientHooks.openMenu(p.pool, p.minPerGene, p.percentPerPoint)));
        c.setPacketHandled(true);
    }
}
